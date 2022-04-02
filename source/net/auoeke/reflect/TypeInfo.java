package net.auoeke.reflect;

import net.gudenau.lib.unsafe.Unsafe;

/**
 @since 4.0.0
 */
enum TypeInfo {
    VOID(void.class, Void.class, 0, 0),
    BOOLEAN(boolean.class, Boolean.class, Byte.SIZE, 0),
    BYTE(byte.class, Byte.class, Byte.SIZE, NumericTypes.INTEGER),
    CHAR(char.class, Character.class, Character.SIZE, NumericTypes.INTEGER),
    SHORT(short.class, Short.class, Short.SIZE, NumericTypes.INTEGER),
    INT(int.class, Integer.class, Integer.SIZE, NumericTypes.INTEGER),
    LONG(long.class, Long.class, Long.SIZE, NumericTypes.INTEGER),
    FLOAT(float.class, Float.class, Float.SIZE, NumericTypes.FLOAT),
    DOUBLE(double.class, Double.class, Double.SIZE, NumericTypes.FLOAT),
    REFERENCE(null, Object.class, Unsafe.ADDRESS_SIZE * Byte.SIZE, 0);

    public final Class<?> primitive;
    public final Class<?> reference;
    public final int size;

    private final int numericType;

    TypeInfo(Class<?> primitive, Class<?> reference, int size, int numericType) {
        this.primitive = primitive;
        this.reference = reference;
        this.size = size;
        this.numericType = numericType;
    }

    public static TypeInfo of(Class<?> type) {
        if (type == int.class) return INT;
        if (type == float.class) return FLOAT;
        if (type == boolean.class) return BOOLEAN;
        if (type == byte.class) return BYTE;
        if (type == long.class) return LONG;
        if (type == double.class) return DOUBLE;
        if (type == char.class) return CHAR;
        if (type == void.class) return VOID;
        if (type == short.class) return SHORT;
        if (type == Integer.class) return INT;
        if (type == Float.class) return FLOAT;
        if (type == Boolean.class) return BOOLEAN;
        if (type == Byte.class) return BYTE;
        if (type == Long.class) return LONG;
        if (type == Double.class) return DOUBLE;
        if (type == Character.class) return CHAR;
        if (type == Void.class) return VOID;
        if (type == Short.class) return SHORT;

        return REFERENCE;
    }

    public boolean isVoid() {
        return this == VOID;
    }

    public boolean isPrimitive() {
        return this.primitive != null && !this.isVoid();
    }

    public boolean isBoolean() {
        return this == BOOLEAN;
    }

    public boolean isInteger() {
        return this.numericType == NumericTypes.INTEGER;
    }

    public boolean isFloat() {
        return this.numericType == NumericTypes.FLOAT;
    }

    public boolean isReference() {
        return this == REFERENCE;
    }

    public boolean isNumeric() {
        return this.numericType != 0;
    }

    public boolean canWiden(TypeInfo from) {
        return this.isNumeric() && from.isNumeric() && Flags.any(this.numericType, NumericTypes.FLOAT | from.numericType) && this.size >= from.size;
    }

    private static class NumericTypes {
        static final int
            INTEGER = 1,
            FLOAT = 1 << 1;
    }
}
