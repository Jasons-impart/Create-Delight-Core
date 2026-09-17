package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

/** No Minecraft references: this runs inside the Mixin plugin's class loader. */
public final class DamagePipelineTransformer {
    private DamagePipelineTransformer() {}

    public record Result(int roots, int methods, int probes, List<String> sites) {}

    public static Result instrument(ClassNode target, boolean combatRules) {
        return instrument(target, combatRules, DamageArithmeticTransformer.OBSERVER);
    }

    public static Result instrument(ClassNode target, boolean combatRules, String observer) {
        Map<String, MethodNode> local = new LinkedHashMap<>();
        for (MethodNode method : target.methods) local.put(method.name + method.desc, method);
        Set<MethodNode> selected = new LinkedHashSet<>();
        Deque<MethodNode> pending = new ArrayDeque<>();
        for (MethodNode method : target.methods) {
            // Forge names do not remap; this also finds renamed originals from earlier wrappers.
            boolean root = combatRules && !method.name.startsWith("<");
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call
                        && call.owner.equals("net/minecraftforge/common/ForgeHooks")
                        && (call.name.equals("onLivingHurt") || call.name.equals("onLivingDamage"))) root = true;
            }
            if (root) pending.add(method);
        }
        int roots = pending.size();
        while (!pending.isEmpty()) {
            MethodNode method = pending.removeFirst();
            if (!selected.add(method)) continue;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(target.name)) {
                    MethodNode callee = local.get(call.name + call.desc);
                    if (callee != null) pending.add(callee);
                } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                    for (Object argument : dynamic.bsmArgs) {
                        if (argument instanceof Handle handle && handle.getOwner().equals(target.name)) {
                            MethodNode callee = local.get(handle.getName() + handle.getDesc());
                            if (callee != null) pending.add(callee);
                        }
                    }
                }
            }
        }
        int probes = 0;
        List<String> sites = new ArrayList<>();
        for (MethodNode method : selected) {
            int count = instrumentMethod(target, method, observer);
            probes += count;
            if (count > 0) sites.add(method.name + method.desc + " probes=" + count + origin(method));
        }
        return new Result(roots, selected.size(), probes, List.copyOf(sites));
    }

    private static int instrumentMethod(ClassNode target, MethodNode method, String observer) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(observer)
                    && call.name.startsWith("pipeline")) return 0;
        }
        int count = 0, line = -1, index = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof LineNumberNode location) line = location.line;
            int opcode = instruction.getOpcode();
            if (opcode < 0) continue;
            String site = target.name.replace('/', '.') + "#" + method.name + method.desc
                    + "(" + target.sourceFile + ":" + line + ") insn=" + (++index) + origin(method);
            int operation = switch (opcode) {
                case Opcodes.FADD, Opcodes.DADD -> FloatArithmetic.ADD;
                case Opcodes.FSUB, Opcodes.DSUB -> FloatArithmetic.SUBTRACT;
                case Opcodes.FMUL, Opcodes.DMUL -> FloatArithmetic.MULTIPLY;
                case Opcodes.FDIV, Opcodes.DDIV -> FloatArithmetic.DIVIDE;
                case Opcodes.FREM, Opcodes.DREM -> FloatArithmetic.REMAINDER;
                default -> -1;
            };
            InsnList replacement = new InsnList();
            if (operation >= 0) {
                boolean wide = opcode == Opcodes.DADD || opcode == Opcodes.DSUB || opcode == Opcodes.DMUL
                        || opcode == Opcodes.DDIV || opcode == Opcodes.DREM;
                replacement.add(new LdcInsnNode(operation));
                replacement.add(new LdcInsnNode(site));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, observer, "pipelineArithmetic",
                        wide ? "(DDILjava/lang/String;)D" : "(FFILjava/lang/String;)F", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                count++;
            } else if (opcode == Opcodes.D2F) {
                replacement.add(new LdcInsnNode(site));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, observer, "pipelineNarrow",
                        "(DLjava/lang/String;)F", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                count++;
            } else {
                Type result = null;
                String label = site;
                if (instruction instanceof MethodInsnNode call && !call.owner.equals(observer)) {
                    result = Type.getReturnType(call.desc);
                    label += " callReturn=" + call.owner.replace('/', '.') + "#" + call.name + call.desc;
                } else if (opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN) {
                    result = opcode == Opcodes.FRETURN ? Type.FLOAT_TYPE : Type.DOUBLE_TYPE;
                    label += " methodReturn";
                }
                if (result == null || (result.getSort() != Type.FLOAT && result.getSort() != Type.DOUBLE)) continue;
                replacement.add(new LdcInsnNode(label));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, observer, "pipelineValue",
                        result.getSort() == Type.FLOAT ? "(FLjava/lang/String;)F" : "(DLjava/lang/String;)D", false));
                if (opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN) {
                    method.instructions.insertBefore(instruction, replacement);
                } else method.instructions.insert(instruction, replacement);
                count++;
            }
        }
        // Both category-1 and category-2 arithmetic add at most two operand-stack words.
        if (count > 0) method.maxStack += 2;
        return count;
    }

    private static String origin(MethodNode method) {
        if (method.visibleAnnotations != null) for (AnnotationNode annotation : method.visibleAnnotations) {
            if (!annotation.desc.endsWith("/MixinMerged;") || annotation.values == null) continue;
            for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
                if (annotation.values.get(i).equals("mixin")) return " mixin=" + annotation.values.get(i + 1);
            }
        }
        return "";
    }
}
