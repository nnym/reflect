package net.auoeke.reflect;

import java.lang.reflect.Field;
import net.gudenau.lib.unsafe.Unsafe;

/**
 An immutable reference to a datum at an offset from a type or an object.
 <p>
 A pointer can be constructed by one of {@link #of the factory methods} or the constructor.
 Pointers acquired directly from the constructor require configuration before use.
 All pointers can be configured by the methods {@link #bind}, {@link #offset(long)}, {@link #type(int)}, {@link #field}, {@link #instanceField}, {@link #staticField}.

 @since 0.15.0
 */
@SuppressWarnings("unused")
public class Pointer implements Cloneable {
    /** @since 4.10.0 */
    public static final int
        UNKNOWN = -1,
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

    /**
     An address; a {@code long} that is 4 or 8 bytes long.

     @since 5.0.0
     */
    public static final int ADDRESS = 9;

    /**
     The object that will be used as the default base in lookups and mutations.
     */
    public final Object base;

    /**
     The offset of the target from the {@link #base}.
     */
    public final long offset;

    /**
     The type of the this pointer's target for {@link #get} and {@link #put}. May be one of
     {@link #UNKNOWN},
     {@link #BOOLEAN},
     {@link #BYTE},
     {@link #CHAR},
     {@link #SHORT},
     {@link #INT},
     {@link #LONG},
     {@link #FLOAT},
     {@link #DOUBLE} and
     {@link #REFERENCE}.

     @see #type(int)
     @since 4.10.0
     */
    public final int type;

    protected Pointer(Object base, long offset, int type) {
        this.base = base;
        this.offset = offset;
        this.type = type;
    }

    /**
     Construct a pointer to a target of a known type located at an offset from a base.

     @param base the {@link #base} of the pointer's target
     @param offset the target's {@link #offset} from its {@code base}
     @param type the target's {@link #type}
     @return a new pointer with the provided {@code base}, {@code offset} and {@code type}
     @throws IllegalArgumentException if {@code type} is not valid
     @since 5.0.0
     */
    public static Pointer of(Object base, long offset, int type) {
        if (type < UNKNOWN || type > ADDRESS) {
            throw new IllegalArgumentException(Integer.toString(type));
        }

        return new Pointer(base, offset, type);
    }

    /**
     Construct a pointer to a target of a known type located at an offset from a base.

     @param base the {@link #base} of the pointer's target
     @param offset the target's {@link #offset} from its {@code base}
     @param type the target's {@link #type}
     @return a new pointer with the provided {@code base}, {@code offset} and {@code type}
     @since 5.0.0
     */
    public static Pointer of(Object base, long offset, Class<?> type) {
        return new Pointer(base, offset, typeID(type));
    }

    /**
     Construct a pointer to a target of an unknown type located at an offset from a base.

     @param base the {@link #base} of the pointer's target
     @param offset the target's {@link #offset} from its {@code base}
     @return a new pointer with the provided {@code base} and {@code offset}
     @since 5.0.0
     */
    public static Pointer of(Object base, long offset) {
        return new Pointer(base, offset, UNKNOWN);
    }

    /**
     Construct a pointer to a target of an unknown type with an offset.

     @param offset the target's {@link #offset}
     @return a new pointer with the provided {@code offset}
     @since 5.0.0
     */
    public static Pointer of(long offset) {
        return new Pointer(null, offset, UNKNOWN);
    }

    /**
     Construct a pointer to a target of a known type with an offset.

     @param offset the target's {@link #offset}
     @param type the target's {@link #type}
     @return a new pointer with the provided {@code offset} and {@code type}
     @since 5.0.0
     */
    public static Pointer of(long offset, int type) {
        return of(null, offset, type);
    }

    /**
     Construct a pointer to a target of a known type with an offset.

     @param offset the target's {@link #offset}
     @param type the target's {@link #type}
     @return a new pointer with the provided {@code offset} and {@code type}
     @since 5.0.0
     */
    public static Pointer of(long offset, Class<?> type) {
        return of(null, offset, type);
    }

    private static Pointer of(Object instance, Field field) {
        return of(Flags.isStatic(field) ? field.getDeclaringClass() : instance, Fields.offset(field), field.getType());
    }

    /**
     Construct a pointer to a field.

     @param field a field
     @return a new pointer to the field
     @since 4.6.0
     */
    public static Pointer of(Field field) {
        return of(null, field);
    }

    /**
     Construct a pointer to a named field in a given type. If the field is static, then also {@link #bind} its declaring type.

     @param type the field's declaring type
     @param field the field's name
     @return a new pointer to the field
     @since 4.6.0
     */
    public static Pointer of(Class<?> type, String field) {
        return of(null, Fields.of(type, field));
    }

    /**
     Construct a pointer to a named field in an object.

     @param object the pointer's base
     @param field the name of a field of {@code object}
     @return a new pointer bound to {@code object} and pointing at the first field {@code field} therein
     @since 4.10.0
     */
    public static Pointer of(Object object, String field) {
        return of(object, Fields.of(object, field));
    }

    /**
     Clone the pointer.

     @return an identical copy of this pointer
     */
    @Override public Pointer clone() {
        return new Pointer(this.base, this.offset, this.type);
    }

    /**
     Fork the pointer with a custom {@link #base}.

     @param base the base of the new pointer
     @return a copy of this pointer with the specified {@code base}
     */
    public Pointer bind(Object base) {
        return new Pointer(base, this.offset, this.type);
    }

    /**
     Fork the pointer with a custom {@link #offset}.

     @param offset the offset of the new pointer's target from its base
     @return a copy of this pointer with the specified {@code offset}
     */
    public Pointer offset(long offset) {
        return new Pointer(this.base, offset, this.type);
    }

    /**
     Fork this pointer with a custom {@link #type}.

     @param type the type of the new pointer's target
     @return a copy of this pointer with the specified {@code type}
     @throws IllegalArgumentException if {@code type} is not valid
     @see #type
     @since 4.10.0
     */
    public Pointer type(int type) {
        return of(this.base, this.offset, type);
    }

    /**
     Fork this pointer with a custom {@link #type}.

     @param type the type of the this pointer's target
     @return a copy of this pointer with the specified {@code type}
     @since 4.10.0
     */
    public Pointer type(Class<?> type) {
        return of(this.base, this.offset, type);
    }

    /**
     Fork the pointer with {@link #offset(long) offset} and {@link #type(int) type} derived from a field.

     @param field a field wherefrom to derive the pointer's offset
     @return a new pointer with the same {@link #base} and {@link #offset} and {@link #type} derived from the field
     @since 4.10.0
     */
    public Pointer field(Field field) {
        return of(this.base, field);
    }

    /**
     Fork the pointer with {@link #offset(long) offset} and {@link #type(int) type} derived from a field.

     @param type the field's declaring type
     @param name the field's name
     @return a new pointer with the same {@link #base} and {@link #offset} and {@link #type} derived from the field
     @since 4.10.0
     */
    public Pointer field(Class<?> type, String name) {
        return of(this.base, Fields.of(type, name));
    }

    /**
     Fork the pointer with {@link #offset(long) offset} and {@link #type(int) type} derived from a static field declared by its {@link #base} type.

     @param name the name of a field declared by this pointer's bound type
     @return a new pointer with the same {@link #base} and {@link #offset} and {@link #type} derived from the field
     @throws ClassCastException if {@link #base} is not a {@link Class}
     */
    public Pointer staticField(String name) {
        return of(null, Fields.of((Class<?>) this.base, name));
    }

    /**
     Fork the pointer with {@link #offset(long) offset} and {@link #type(int) type} derived from a field in its {@link #base}.

     @param name the name of a field in this pointer's base object
     @return a new pointer with the same {@link #base} and {@link #offset} and {@link #type} derived from the field
     */
    public Pointer instanceField(String name) {
        return of(this.base, Fields.of(this.base, name));
    }

    /**
     Get the target's value regardless of type in this pointer's object.

     @return the value of this pointer's target in its object
     @throws IllegalStateException if {@link #type} is not valid
     @see #type
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
            case ADDRESS -> this.getAddress();
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
            case ADDRESS -> this.getAddress(object);
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
            case BOOLEAN -> this.putBoolean(this.base, (boolean) value);
            case BYTE -> this.putByte(this.base, (byte) value);
            case CHAR -> this.putChar(this.base, (char) value);
            case SHORT -> this.putShort(this.base, (short) value);
            case INT -> this.putInt(this.base, (int) value);
            case LONG -> this.putLong(this.base, (long) value);
            case FLOAT -> this.putFloat(this.base, (float) value);
            case DOUBLE -> this.putDouble(this.base, (double) value);
            case REFERENCE -> this.putReference(this.base, value);
            case ADDRESS -> this.putAddress(this.base, (long) value);
            default -> throw new IllegalStateException("type is not set");
        }
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
            case ADDRESS -> this.putAddress(object, (long) value);
            default -> throw new IllegalStateException("type is not set");
        }
    }

    public void copy(Object destination) {
        this.put(destination, this.get());
    }

    public void copy(Object source, Object destination) {
        this.put(destination, this.get(source));
    }

    public <T> T getT() {
        return Unsafe.getReference(this.base, this.offset);
    }

    public <T> T getT(Object object) {
        return Unsafe.getReference(object, this.offset);
    }

    public Object getReference() {
        return Unsafe.getReference(this.base, this.offset);
    }

    public Object getReference(Object object) {
        return Unsafe.getReference(object, this.offset);
    }

    public void putReference(Object object, Object value) {
        Unsafe.putReference(object, this.offset, value);
    }

    public void putReference(Object value) {
        Unsafe.putReference(this.base, this.offset, value);
    }

    public void copyReference(Object destination) {
        this.putReference(destination, this.getReference());
    }

    public void copyReference(Object source, Object destination) {
        this.putReference(destination, this.getReference(source));
    }

    public boolean getBoolean() {
        return Unsafe.getBoolean(this.base, this.offset);
    }

    public boolean getBoolean(Object object) {
        return Unsafe.getBoolean(object, this.offset);
    }

    public void putBoolean(boolean value) {
        Unsafe.putBoolean(this.base, this.offset, value);
    }

    public void putBoolean(Object object, boolean value) {
        Unsafe.putBoolean(object, this.offset, value);
    }

    public void copyBoolean(Object destination) {
        this.putBoolean(destination, this.getBoolean());
    }

    public void copyBoolean(Object source, Object destination) {
        this.putBoolean(destination, this.getBoolean(source));
    }

    public byte getByte() {
        return Unsafe.getByte(this.base, this.offset);
    }

    public byte getByte(Object object) {
        return Unsafe.getByte(object, this.offset);
    }

    public void putByte(Object object, byte value) {
        Unsafe.putByte(object, this.offset, value);
    }

    public void putByte(byte value) {
        Unsafe.putByte(this.base, this.offset, value);
    }

    public void copyByte(Object destination) {
        this.putByte(destination, this.getByte());
    }

    public void copyByte(Object source, Object destination) {
        this.putByte(destination, this.getByte(source));
    }

    public short getShort() {
        return Unsafe.getShort(this.base, this.offset);
    }

    public short getShort(Object object) {
        return Unsafe.getShort(object, this.offset);
    }

    public void putShort(Object object, short value) {
        Unsafe.putShort(object, this.offset, value);
    }

    public void putShort(short value) {
        Unsafe.putShort(this.base, this.offset, value);
    }

    public void copyShort(Object destination) {
        this.putShort(destination, this.getShort());
    }

    public void copyShort(Object source, Object destination) {
        this.putShort(destination, this.getShort(source));
    }

    public char getChar() {
        return Unsafe.getChar(this.base, this.offset);
    }

    public char getChar(Object object) {
        return Unsafe.getChar(object, this.offset);
    }

    public void putChar(Object object, char value) {
        Unsafe.putChar(object, this.offset, value);
    }

    public void putChar(char value) {
        Unsafe.putChar(this.base, this.offset, value);
    }

    public void copyChar(Object destination) {
        this.putChar(destination, this.getChar());
    }

    public void copyChar(Object source, Object destination) {
        this.putChar(destination, this.getChar(source));
    }

    public int getInt() {
        return Unsafe.getInt(this.base, this.offset);
    }

    public int getInt(Object object) {
        return Unsafe.getInt(object, this.offset);
    }

    public void putInt(Object object, int value) {
        Unsafe.putInt(object, this.offset, value);
    }

    public void putInt(int value) {
        Unsafe.putInt(this.base, this.offset, value);
    }

    public void copyInt(Object destination) {
        this.putInt(destination, this.getInt());
    }

    public void copyInt(Object source, Object destination) {
        this.putInt(destination, this.getInt(source));
    }

    public long getLong() {
        return Unsafe.getLong(this.base, this.offset);
    }

    public long getLong(Object object) {
        return Unsafe.getLong(object, this.offset);
    }

    public void putLong(Object object, long value) {
        Unsafe.putLong(object, this.offset, value);
    }

    public void putLong(long value) {
        Unsafe.putLong(this.base, this.offset, value);
    }

    public void copyLong(Object destination) {
        this.putLong(destination, this.getLong());
    }

    public void copyLong(Object source, Object destination) {
        this.putLong(destination, this.getLong(source));
    }

    public float getFloat() {
        return Unsafe.getFloat(this.base, this.offset);
    }

    public float getFloat(Object object) {
        return Unsafe.getFloat(object, this.offset);
    }

    public void putFloat(Object object, float value) {
        Unsafe.putFloat(object, this.offset, value);
    }

    public void putFloat(float value) {
        Unsafe.putFloat(this.base, this.offset, value);
    }

    public void copyFloat(Object destination) {
        this.putFloat(destination, this.getFloat());
    }

    public void copyFloat(Object source, Object destination) {
        this.putFloat(destination, this.getFloat(source));
    }

    public double getDouble() {
        return Unsafe.getDouble(this.base, this.offset);
    }

    public double getDouble(Object object) {
        return Unsafe.getDouble(object, this.offset);
    }

    public void putDouble(Object object, double value) {
        Unsafe.putDouble(object, this.offset, value);
    }

    public void putDouble(double value) {
        Unsafe.putDouble(this.base, this.offset, value);
    }

    public void copyDouble(Object destination) {
        this.putDouble(destination, this.getDouble());
    }

    public void copyDouble(Object source, Object destination) {
        this.putDouble(destination, this.getDouble(source));
    }

    public long getAddress() {
        return Unsafe.getAddress(this.base, this.offset);
    }

    public long getAddress(Object base) {
        return Unsafe.getAddress(base, this.offset);
    }

    public void putAddress(long address) {
        Unsafe.putAddress(this.base, this.offset, address);
    }

    public void putAddress(Object base, long address) {
        Unsafe.putAddress(base, this.offset, address);
    }

    public void copyAddress(Object destination) {
        this.putAddress(destination, this.getAddress());
    }

    public void copyAddress(Object source, Object destination) {
        this.putAddress(destination, this.getAddress(source));
    }

    public <T> T getReferenceVolatile() {
        return Unsafe.getReferenceVolatile(this.base, this.offset);
    }

    public <T> T getReferenceVolatile(Object object) {
        return Unsafe.getReferenceVolatile(object, this.offset);
    }

    public void putReferenceVolatile(Object object, Object value) {
        Unsafe.putReferenceVolatile(object, this.offset, value);
    }

    public void putReferenceVolatile(Object value) {
        Unsafe.putReferenceVolatile(this.base, this.offset, value);
    }

    public boolean getBooleanVolatile() {
        return Unsafe.getBooleanVolatile(this.base, this.offset);
    }

    public boolean getBooleanVolatile(Object object) {
        return Unsafe.getBooleanVolatile(object, this.offset);
    }

    public void putBooleanVolatile(Object object, boolean value) {
        Unsafe.putBooleanVolatile(object, this.offset, value);
    }

    public void putBooleanVolatile(boolean value) {
        Unsafe.putBooleanVolatile(this.base, this.offset, value);
    }

    public byte getByteVolatile() {
        return Unsafe.getByteVolatile(this.base, this.offset);
    }

    public byte getByteVolatile(Object object) {
        return Unsafe.getByteVolatile(object, this.offset);
    }

    public void putByteVolatile(Object object, byte value) {
        Unsafe.putByteVolatile(object, this.offset, value);
    }

    public void putByteVolatile(byte value) {
        Unsafe.putByteVolatile(this.base, this.offset, value);
    }

    public short getShortVolatile() {
        return Unsafe.getShortVolatile(this.base, this.offset);
    }

    public short getShortVolatile(Object object) {
        return Unsafe.getShortVolatile(object, this.offset);
    }

    public void putShortVolatile(Object object, short value) {
        Unsafe.putShortVolatile(object, this.offset, value);
    }

    public void putShortVolatile(short value) {
        Unsafe.putShortVolatile(this.base, this.offset, value);
    }

    public char getCharVolatile() {
        return Unsafe.getCharVolatile(this.base, this.offset);
    }

    public char getCharVolatile(Object object) {
        return Unsafe.getCharVolatile(object, this.offset);
    }

    public void putCharVolatile(Object object, char value) {
        Unsafe.putCharVolatile(object, this.offset, value);
    }

    public void putCharVolatile(char value) {
        Unsafe.putCharVolatile(this.base, this.offset, value);
    }

    public int getIntVolatile() {
        return Unsafe.getIntVolatile(this.base, this.offset);
    }

    public int getIntVolatile(Object object) {
        return Unsafe.getIntVolatile(object, this.offset);
    }

    public void putIntVolatile(Object object, int value) {
        Unsafe.putIntVolatile(object, this.offset, value);
    }

    public void putIntVolatile(int value) {
        Unsafe.putIntVolatile(this.base, this.offset, value);
    }

    public long getLongVolatile() {
        return Unsafe.getLongVolatile(this.base, this.offset);
    }

    public long getLongVolatile(Object object) {
        return Unsafe.getLongVolatile(object, this.offset);
    }

    public void putLongVolatile(Object object, long value) {
        Unsafe.putLongVolatile(object, this.offset, value);
    }

    public void putLongVolatile(long value) {
        Unsafe.putLongVolatile(this.base, this.offset, value);
    }

    public float getFloatVolatile() {
        return Unsafe.getFloatVolatile(this.base, this.offset);
    }

    public float getFloatVolatile(Object object) {
        return Unsafe.getFloatVolatile(object, this.offset);
    }

    public void putFloatVolatile(Object object, float value) {
        Unsafe.putFloatVolatile(object, this.offset, value);
    }

    public void putFloatVolatile(float value) {
        Unsafe.putFloatVolatile(this.base, this.offset, value);
    }

    public double getDoubleVolatile() {
        return Unsafe.getDoubleVolatile(this.base, this.offset);
    }

    public double getDoubleVolatile(Object object) {
        return Unsafe.getDoubleVolatile(object, this.offset);
    }

    public void putDoubleVolatile(Object object, double value) {
        Unsafe.putDoubleVolatile(object, this.offset, value);
    }

    public void putDoubleVolatile(double value) {
        Unsafe.putDoubleVolatile(this.base, this.offset, value);
    }

    private static int typeID(Class<?> type) {
        return type == boolean.class ? BOOLEAN
            : type == byte.class ? BYTE
            : type == char.class ? CHAR
            : type == short.class ? SHORT
            : type == int.class ? INT
            : type == long.class ? LONG
            : type == float.class ? FLOAT
            : type == double.class ? DOUBLE
            : REFERENCE;
    }
}
