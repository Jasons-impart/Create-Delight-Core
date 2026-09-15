package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Load-time observer for original float instructions; deliberately independent of Minecraft classes. */
public final class DamageArithmeticTransformer {
    public static final String OBSERVER = "io/github/jasonsimpart/createdelightcore/compat/combat/diagnostics/DamageDiagnostics";

    private DamageArithmeticTransformer() {}

    public static int instrument(ClassNode target, String methodName) {
        return instrument(target, methodName, OBSERVER);
    }

    public static int instrument(ClassNode target, String methodName, String observer) {
        int count = 0;
        for (MethodNode method : target.methods) {
            if (!method.name.equals(methodName)) continue;
            int line = -1;
            int index = 0;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof LineNumberNode location) line = location.line;
                int operation = switch (instruction.getOpcode()) {
                    case Opcodes.FADD -> FloatArithmetic.ADD;
                    case Opcodes.FSUB -> FloatArithmetic.SUBTRACT;
                    case Opcodes.FMUL -> FloatArithmetic.MULTIPLY;
                    case Opcodes.FDIV -> FloatArithmetic.DIVIDE;
                    case Opcodes.FREM -> FloatArithmetic.REMAINDER;
                    default -> -1;
                };
                if (operation < 0) continue;
                InsnList replacement = new InsnList();
                replacement.add(new LdcInsnNode(operation));
                replacement.add(new LdcInsnNode(target.name.replace('/', '.') + "#" + method.name
                        + "(" + target.sourceFile + ":" + line + ") op=" + (++index)));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, observer, "arithmetic",
                        "(FFILjava/lang/String;)F", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                count++;
            }
            // Operand stack is two words taller while passing operation and source location.
            if (index > 0) method.maxStack += 2;
        }
        return count;
    }
}
