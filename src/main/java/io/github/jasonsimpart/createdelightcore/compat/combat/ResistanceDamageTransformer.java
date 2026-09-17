package io.github.jasonsimpart.createdelightcore.compat.combat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/** Reorder only the vanilla resistance product/quotient, before diagnostic instrumentation. */
public final class ResistanceDamageTransformer {
    private ResistanceDamageTransformer() {}

    public static void apply(ClassNode target) {
        List<Match> matches = new ArrayList<>();
        for (MethodNode method : target.methods) {
            if (!(method.name.equals("getDamageAfterMagicAbsorb") || method.name.equals("m_6515_"))
                    || !method.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;F)F")) continue;
            List<AbstractInsnNode> code = new ArrayList<>();
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction.getOpcode() >= 0) code.add(instruction);
            }
            for (int i = 0; i + 9 < code.size(); i++) {
                // damage * (float)factor -> temporary; originalDamage = damage; temporary / 25F
                if (code.get(i).getOpcode() != Opcodes.FLOAD
                        || code.get(i + 1).getOpcode() != Opcodes.ILOAD
                        || code.get(i + 2).getOpcode() != Opcodes.I2F
                        || code.get(i + 3).getOpcode() != Opcodes.FMUL
                        || code.get(i + 4).getOpcode() != Opcodes.FSTORE
                        || code.get(i + 5).getOpcode() != Opcodes.FLOAD
                        || code.get(i + 6).getOpcode() != Opcodes.FSTORE
                        || code.get(i + 7).getOpcode() != Opcodes.FLOAD
                        || !(code.get(i + 8) instanceof LdcInsnNode constant)
                        || !Float.valueOf(25F).equals(constant.cst)
                        || code.get(i + 9).getOpcode() != Opcodes.FDIV) continue;
                if (((VarInsnNode) code.get(i)).var != ((VarInsnNode) code.get(i + 5)).var
                        || ((VarInsnNode) code.get(i + 4)).var != ((VarInsnNode) code.get(i + 7)).var) continue;
                matches.add(new Match(method, code.get(i + 3), constant, code.get(i + 9)));
            }
        }
        // Refuse partial or ambiguous rewrites after a game/mod upgrade.
        if (matches.size() != 1) {
            throw new IllegalStateException("Expected exactly one resistance damage*factor/25 sequence in "
                    + target.name + ", found " + matches.size());
        }
        Match match = matches.get(0);
        InsnList ratio = new InsnList();
        ratio.add(new LdcInsnNode(25F));
        ratio.add(new InsnNode(Opcodes.FDIV));
        match.method.instructions.insertBefore(match.multiply, ratio);
        match.method.instructions.remove(match.divisor);
        match.method.instructions.remove(match.divide);
        // At the insertion point the stack is [damage, factor]; loading 25 adds one word.
        match.method.maxStack++;
    }

    private record Match(MethodNode method, AbstractInsnNode multiply,
                         AbstractInsnNode divisor, AbstractInsnNode divide) {}
}
