package net.auoeke.reflect;

import java.lang.reflect.Field;
import net.gudenau.lib.unsafe.Unsafe;

/**
 A reference to a datum at an offset from a type or an object.
 <p>
 A pointer can be constructed by one of {@linkplain #of the factory methods} or the constructor.
 Pointers acquired directly from the constructor require configuration before use.
 All pointers can be configured by the methods {@link #bind}, {@link #offset(long)}, {@link #type(int)}, {@link #field}, {@link #instanceField}, {@link #staticField}.

 @since 0.15.0 */
@SuppressWarnings("unused")
public class Pointer implements Cloneable {
    /** @since 4.10.0 */
    public static final int
        BOOLEAN = 0,
        BYTE = 1,
        CHAR = 2,
        SHORT = 3,
        INT = 4,
        LONG = 5,
        FLOAT = 6,
        DOUBLE = 7;

    /**
     Any reference type.

     @since 4.10.0
     */
    public static final int REFERENCE = 8;

    public Object object;
    public long offset;

    protected int type = -1;

    /**
     Construct a pointer to a field.

     @return the pointer
     @since 4.6.0
     */
    public static Pointer of(Field field) {
        return new Pointer().field(field);
    }

    /**
     Construct a pointer to a named field in a given type.

     @param type the field's declaring type
     @param field the field's name
     @return the pointer
     @since 4.6.0
     */
    public static Pointer of(Class<?> type, String field) {
        return new Pointer().field(type, field);
    }

    /**
     Construct a pointer to a named field bound to an object. The

     @param object this pointer's new base
     @param field the name of a field of {@code object}
     @return a new pointer bound to {@code object} and pointing at the first field {@code field} therein
     @since 4.10.0
     */
    public static Pointer of(Object object, String field) {
        return new Pointer().field(object, field);
    }

    /**
     Construct an identical copy of {@code this}.

     @return a new pointer with the same properties as {@code this}
     */
    @Override public Pointer clone() {
        var clone = new Pointer();
        clone.object = this.object;
        clone.offset = this.offset;
        clone.type = this.type;

        return clone;
    }

    /**
     Store a default object that will be used as the base of future lookups and mutations.

     @param object the object that will be used as the base of future lookups and mutations
     @return {@code this}
     */
    public Pointer bind(Object object) {
        this.object = object;

        return this;
    }

    /**
     Set the offset of the this pointer's target from its {@link #bind base object}.

     @param offset the offset of the this pointer's target from its base
     @return {@code this}
     */
    public Pointer offset(long offset) {
        this.offset = offset;

        return this;
    }

    /**
     Set the type of the this pointer's target for {@link #get} and {@link #put}.

     @param type the type of the this pointer's target (see below for valid values)
     @return {@code this}
     @throws IllegalArgumentException if {@code type} is not valid (@see below)
     @see #BOOLEAN
     @see #BYTE
     @see #CHAR
     @see #SHORT
     @see #INT
     @see #LONG
     @see #FLOAT
     @see #DOUBLE
     @see #REFERENCE
     @since 4.10.0
     */
    public Pointer type(int type) {
        if (type < BOOLEAN || type > REFERENCE) {
            throw new IllegalArgumentException(Integer.toString(type));
        }

        this.type = type;

        return this;
    }

    /**
     Set the type of the this pointer's target for {@link #get} and {@link #put}.

     @param type the type of the this pointer's target
     @return {@code this}
     @see #type(int)
     @since 4.10.0
     */
    public Pointer type(Class<?> type) {
        // @formatter:off
        return this.type(
            type == boolean.class ? BOOLEAN
            : type == byte.class ? BYTE
            : type == char.class ? CHAR
            : type == short.class ? SHORT
            : type == int.class ? INT
            : type == long.class ? LONG
            : type == float.class ? FLOAT
            : type == double.class ? DOUBLE
            : REFERENCE
        );
        // @formatter:on
    }

    /**
     Get the type of the this pointer's target.

     @return the type of this pointer's target if intitialized or else -1
     @see #type(int)
     @since 4.10.0
     */
    public int type() {
        return this.type;
    }

    /**
     Derive an offset for this pointer from a field.

     @param field a field wherefrom to derive this pointer's offset
     @return {@code this}
     @since 4.10.0
     */
    public Pointer field(Field field) {
        this.offset(Fields.offset(field));

        if (Flags.isStatic(field)) {
            this.bind(field.getDeclaringClass());
        }

        return this.type(field.getType());
    }

    /**
     Derive an offset for this pointer from a named field in a given type.
     If the field is static, then also bind the type.

     @param type a type
     @param name the name of a field declared by {@code type}
     @return {@code this}
     @since 4.10.0
     */
    public Pointer field(Class<?> type, String name) {
        return this.field(Fields.of(type, name));
    }

    /**
     Derive an offset for this pointer from a named field in an object and bind the object.
     If multiple fields with the same name are found, then the field of the most derived type is used.

     @param object an object
     @param name the name of a field in {@code object}
     @return {@code this}
     @since 4.10.0
     */
    public Pointer field(Object object, String name) {
        return this.bind(object).instanceField(name);
    }

    /**
     Derive an offset for this pointer from a field declared by this pointer's bound type.

     @param name the name of a field declared by this pointer's bound type
     @return {@code this}
     */
    public Pointer staticField(String name) {
        return this.field(Fields.of((Class<?>) this.object, name));
    }

    /**
     Derive an offset for this pointer from a field in this pointer's bound object.

     @param name the name of a field in this pointer's bound object
     @return {@code this}
     */
    public Pointer instanceField(String name) {
        return this.field(Fields.of(this.object, name));
    }

    /** @deprecated Use {@link #field(Class, String)}. */
    @Deprecated(since = "4.10.0", forRemoval = true)
    public Pointer staticField(Class<?> type, String name) {
        return this.field(type, name);
    }

    /** @deprecated Use {@link #field(Field)}. */
    @Deprecated(since = "4.10.0", forRemoval = true)
    public Pointer staticField(Field field) {
        return this.field(field);
    }

    /** @deprecated Use {@link #field(Class, String)}. */
    @Deprecated(since = "4.10.0", forRemoval = true)
    public Pointer instanceField(Class<?> type, String name) {
        return this.field(type, name);
    }

    /** @deprecated Use {@link #field(Field)}. */
    @Deprecated(since = "4.10.0", forRemoval = true)
    public Pointer instanceField(Field field) {
        return this.field(field);
    }

    /**
     Get the target's value regardless of type in this pointer's object.

     @return the value of this pointer's target in its object
     @throws IllegalStateException if {@link #type} is not valid (@see below)
     @since 4.10.0
     */
    public Object get() {
        return switch (this.type) {
            case BOOLEAN -> this.getBoolean();
            case BYTE -> this.getByte();
            case CHAR -> this.getChar();
            case SHORT -> this.getShort();
            case INT -> this.getInt();
            case LONG -> this.getLong();
            case FLOAT -> this.getFloat();
            case DOUBLE -> this.getDouble();
            case REFERENCE -> this.getReference();
            default -> throw new IllegalStateException("type is not set");
        };
    }

    /**
     Get the target's value regardless of type in an object.

     @param object an object wherefrom to get this pointer's target's value
     @return the value of this pointer's target in the object
     @throws IllegalStateException if {@link #type} is not valid (@see below)
     @since 4.10.0
     */
    public Object get(Object object) {
        return switch (this.type) {
            case BOOLEAN -> this.getBoolean(object);
            case BYTE -> this.getByte(object);
            case CHAR -> this.getChar(object);
            case SHORT -> this.getShort(object);
            case INT -> this.getInt(object);
            case LONG -> this.getLong(object);
            case FLOAT -> this.getFloat(object);
            case DOUBLE -> this.getDouble(object);
            case REFERENCE -> this.getReference(object);
            default -> throw new IllegalStateException("type is not set");
        };
    }

    /**
     Set the target's value regardless of type in this pointer's object.

     @param value the new value of the target
     @throws IllegalStateException if {@link #type} is not valid (@see below)
     @since 4.10.0
     */
    public void put(Object value) {
        switch (this.type) {
            case BOOLEAN -> this.putBoolean(this.object, (boolean) value);
            case BYTE -> this.putByte(this.object, (byte) value);
            case CHAR -> this.putChar(this.object, (char) value);
            case SHORT -> this.putShort(this.object, (short) value);
            case INT -> this.putInt(this.object, (int) value);
            case LONG -> this.putLong(this.object, (long) value);
            case FLOAT -> this.putFloat(this.object, (float) value);
            case DOUBLE -> this.putDouble(this.object, (double) value);
            case REFERENCE -> this.putReference(this.object, value);
            default -> throw new IllegalStateException("type is not set");
        } ;
    }

    /**
     Set the target's value regardless of type in an object.

     @param object an object wherein to set the target's value
     @param value the new value of the target
     @throws IllegalStateException if {@link #type} is not valid (@see below)
     @since 4.10.0
     */
    public void put(Object object, Object value) {
        switch (this.type) {
            case BOOLEAN -> this.putBoolean(object, (boolean) value);
            case BYTE -> this.putByte(object, (byte) value);
            case CHAR -> this.putChar(object, (char) value);
            case SHORT -> this.putShort(object, (short) value);
            case INT -> this.putInt(object, (int) value);
            case LONG -> this.putLong(object, (long) value);
            case FLOAT -> this.putFloat(object, (float) value);
            case DOUBLE -> this.putDouble(object, (double) value);
            case REFERENCE -> this.putReference(object, value);
            default -> throw new IllegalStateException("type is not set");
        } ;
    }

    public <T> T getT() {
        return Unsafe.getReference(this.object, this.offset);
    }

    public <T> T getT(Object object) {
        return Unsafe.getReference(object, this.offset);
    }

    public Object getReference() {
        return Unsafe.getReference(this.object, this.offset);
    }

    public Object getReference(Object object) {
        return Unsafe.getReference(object, this.offset);
    }

    public void putReference(Object object, Object value) {
        Unsafe.putReference(object, this.offset, value);
    }

    public void putReference(Object value) {
        Unsafe.putReference(this.object, this.offset, value);
    }

    public boolean getBoolean() {
        return Unsafe.getBoolean(this.object, this.offset);
    }

    public boolean getBoolean(Object object) {
        return Unsafe.getBoolean(object, this.offset);
    }

    public void putBoolean(boolean value) {
        Unsafe.putBoolean(this.object, this.offset, value);
    }

    public void putBoolean(Object object, boolean value) {
        Unsafe.putBoolean(object, this.offset, value);
    }

    public byte getByte() {
        return Unsafe.getByte(this.object, this.offset);
    }

    public byte getByte(Object object) {
        return Unsafe.getByte(object, this.offset);
    }

    public void putByte(Object object, byte value) {
        Unsafe.putByte(object, this.offset, value);
    }

    public void putByte(byte value) {
        Unsafe.putByte(this.object, this.offset, value);
    }

    public short getShort() {
        return Unsafe.getShort(this.object, this.offset);
    }

    public short getShort(Object object) {
        return Unsafe.getShort(object, this.offset);
    }

    public void putShort(Object object, short value) {
        Unsafe.putShort(object, this.offset, value);
    }

    public void putShort(short value) {
        Unsafe.putShort(this.object, this.offset, value);
    }

    public char getChar() {
        return Unsafe.getChar(this.object, this.offset);
    }

    public char getChar(Object object) {
        return Unsafe.getChar(object, this.offset);
    }

    public void putChar(Object object, char value) {
        Unsafe.putChar(object, this.offset, value);
    }

    public void putChar(char value) {
        Unsafe.putChar(this.object, this.offset, value);
    }

    public int getInt() {
        return Unsafe.getInt(this.object, this.offset);
    }

    public int getInt(Object object) {
        return Unsafe.getInt(object, this.offset);
    }

    public void putInt(Object object, int value) {
        Unsafe.putInt(object, this.offset, value);
    }

    public void putInt(int value) {
        Unsafe.putInt(this.object, this.offset, value);
    }

    public long getLong() {
        return Unsafe.getLong(this.object, this.offset);
    }

    public long getLong(Object object) {
        return Unsafe.getLong(object, this.offset);
    }

    public void putLong(Object object, long value) {
        Unsafe.putLong(object, this.offset, value);
    }

    public void putLong(long value) {
        Unsafe.putLong(this.object, this.offset, value);
    }

    public float getFloat() {
        return Unsafe.getFloat(this.object, this.offset);
    }

    public float getFloat(Object object) {
        return Unsafe.getFloat(object, this.offset);
    }

    public void putFloat(Object object, float value) {
        Unsafe.putFloat(object, this.offset, value);
    }

    public void putFloat(float value) {
        Unsafe.putFloat(this.object, this.offset, value);
    }

    public double getDouble() {
        return Unsafe.getDouble(this.object, this.offset);
    }

    public double getDouble(Object object) {
        return Unsafe.getDouble(object, this.offset);
    }

    public void putDouble(Object object, double value) {
        Unsafe.putDouble(object, this.offset, value);
    }

    public void putDouble(double value) {
        Unsafe.putDouble(this.object, this.offset, value);
    }

    public <T> T getVolatile() {
        return Unsafe.getReferenceVolatile(this.object, this.offset);
    }

    public <T> T getVolatile(Object object) {
        return Unsafe.getReferenceVolatile(object, this.offset);
    }

    public void putVolatile(Object object, Object value) {
        Unsafe.putReferenceVolatile(object, this.offset, value);
    }

    public void putVolatile(Object value) {
        Unsafe.putReferenceVolatile(this.object, this.offset, value);
    }

    public boolean getBooleanVolatile() {
        return Unsafe.getBooleanVolatile(this.object, this.offset);
    }

    public boolean getBooleanVolatile(Object object) {
        return Unsafe.getBooleanVolatile(object, this.offset);
    }

    public void putBooleanVolatile(Object object, boolean value) {
        Unsafe.putBooleanVolatile(object, this.offset, value);
    }

    public void putBooleanVolatile(boolean value) {
        Unsafe.putBooleanVolatile(this.object, this.offset, value);
    }

    public byte getByteVolatile() {
        return Unsafe.getByteVolatile(this.object, this.offset);
    }

    public byte getByteVolatile(Object object) {
        return Unsafe.getByteVolatile(object, this.offset);
    }

    public void putByteVolatile(Object object, byte value) {
        Unsafe.putByteVolatile(object, this.offset, value);
    }

    public void putByteVolatile(byte value) {
        Unsafe.putByteVolatile(this.object, this.offset, value);
    }

    public short getShortVolatile() {
        return Unsafe.getShortVolatile(this.object, this.offset);
    }

    public short getShortVolatile(Object object) {
        return Unsafe.getShortVolatile(object, this.offset);
    }

    public void putShortVolatile(Object object, short value) {
        Unsafe.putShortVolatile(object, this.offset, value);
    }

    public void putShortVolatile(short value) {
        Unsafe.putShortVolatile(this.object, this.offset, value);
    }

    public char getCharVolatile() {
        return Unsafe.getCharVolatile(this.object, this.offset);
    }

    public char getCharVolatile(Object object) {
        return Unsafe.getCharVolatile(object, this.offset);
    }

    public void putCharVolatile(Object object, char value) {
        Unsafe.putCharVolatile(object, this.offset, value);
    }

    public void putCharVolatile(char value) {
        Unsafe.putCharVolatile(this.object, this.offset, value);
    }

    public int getIntVolatile() {
        return Unsafe.getIntVolatile(this.object, this.offset);
    }

    public int getIntVolatile(Object object) {
        return Unsafe.getIntVolatile(object, this.offset);
    }

    public void putIntVolatile(Object object, int value) {
        Unsafe.putIntVolatile(object, this.offset, value);
    }

    public void putIntVolatile(int value) {
        Unsafe.putIntVolatile(this.object, this.offset, value);
    }

    public long getLongVolatile() {
        return Unsafe.getLongVolatile(this.object, this.offset);
    }

    public long getLongVolatile(Object object) {
        return Unsafe.getLongVolatile(object, this.offset);
    }

    public void putLongVolatile(Object object, long value) {
        Unsafe.putLongVolatile(object, this.offset, value);
    }

    public void putLongVolatile(long value) {
        Unsafe.putLongVolatile(this.object, this.offset, value);
    }

    public float getFloatVolatile() {
        return Unsafe.getFloatVolatile(this.object, this.offset);
    }

    public float getFloatVolatile(Object object) {
        return Unsafe.getFloatVolatile(object, this.offset);
    }

    public void putFloatVolatile(Object object, float value) {
        Unsafe.putFloatVolatile(object, this.offset, value);
    }

    public void putFloatVolatile(float value) {
        Unsafe.putFloatVolatile(this.object, this.offset, value);
    }

    public double getDoubleVolatile() {
        return Unsafe.getDoubleVolatile(this.object, this.offset);
    }

    public double getDoubleVolatile(Object object) {
        return Unsafe.getDoubleVolatile(object, this.offset);
    }

    public void putDoubleVolatile(Object object, double value) {
        Unsafe.putDoubleVolatile(object, this.offset, value);
    }

    public void putDoubleVolatile(double value) {
        Unsafe.putDoubleVolatile(this.object, this.offset, value);
    }
}
