package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

/** The same single-precision operations as the observed JVM instructions. */
public final class FloatArithmetic {
    public static final int ADD = 0, SUBTRACT = 1, MULTIPLY = 2, DIVIDE = 3, REMAINDER = 4;

    private FloatArithmetic() {}

    public static float apply(float left, float right, int operation) {
        return switch (operation) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
            case REMAINDER -> left % right;
            default -> throw new IllegalArgumentException("Unknown float operation " + operation);
        };
    }

    public static String symbol(int operation) {
        return switch (operation) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            case REMAINDER -> "%";
            default -> "?";
        };
    }

    public static String format(float value) {
        return Float.toString(value) + " [0x" + Integer.toHexString(Float.floatToRawIntBits(value)) + "]";
    }
}
