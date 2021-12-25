package net.auoeke.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.auoeke.reflect.Reflect.nul;

@SuppressWarnings("unused")
public class Types {
    /**
     Widen primitives according to the automatic widening in Java.
     */
    public static long WIDEN = 1;

    /**
     Unbox primitive wrappers if they match the target primitive type.
     */
    public static long UNBOX = 1 << 1;

    /**
     {@linkplain #WIDEN Widen primitives} and {@linkplain #UNBOX unbox wrappers}.
     */
    public static long DEFAULT_CONVERSION = WIDEN | UNBOX;

    /**
     Get a class hierarchy starting at a class and ending at one of its superclasses.

     @param begin the initial class
     @param end   the highest superclass; may be null (exclusive)
     @param <T>   the type of the initial class
     @return the hierarchy as a {@link Stream}
     */
    public static <T> Stream<Class<? super T>> classes(Class<T> begin, Class<?> end) {
        return Stream.iterate(begin, type -> type != end, Class::getSuperclass);
    }

    /**
     Get the hierarchy of a class.

     @param begin the bottom of the hierarchy
     @param <T>   the type of the lowest class
     @return the hierarchy as a {@link Stream}
     */
    public static <T> Stream<Class<? super T>> classes(Class<T> begin) {
        return classes(begin, null);
    }

    /**
     Get the size of a value of a type. Primitive wrapper types are treated as ordinary reference types.

     @param type a type
     @return the size of its values
     */
    public static int size(Class<?> type) {
        return (type.isPrimitive() ? TypeInfo.of(type) : TypeInfo.REFERENCE).size;
    }

    /**
     Convert a primitive wrapper type to its primitive counterpart.

     @param type a type
     @return the type if it is primitive; the primitive counterpart of the type if it is a primitive wrapper; otherwise null
     */
    public static Class<?> unbox(Class<?> type) {
        return type.isPrimitive() ? type : TypeInfo.of(type).primitive;
    }

    /**
     Convert a primitive type to its corresponding wrapper type.

     @param type a type
     @return the type's wrapper type if it is primitive; otherwise the type
     */
    public static Class<?> box(Class<?> type) {
        return type.isPrimitive() ? TypeInfo.of(type).reference : type;
    }

    /**
     Check whether 2 types are the same or if one of them is a primitive and the other is its wrapper type.

     @param type  a type
     @param other another type
     @return whether they are the some or one of them is a wrapper of the other
     */
    public static boolean equals(Class<?> type, Class<?> other) {
        return type == other || type != null && other != null && (unbox(type) == other || unbox(other) == type);
    }

    /**
     Check whether a type is a primitive wrapper type.

     @param type a type
     @return whether it is a primitive wrapper type
     */
    public static boolean isWrapper(Class<?> type) {
        return !type.isPrimitive() && unbox(type) != null;
    }

    /**
     Check whether an assignment is legal according to the given conversion flags.
     <p>
     {@code left variable = right;}

     @param flags the conversion flags
     @param left  the type to which to assign
     @param right the type to assign
     @see #WIDEN
     @see #UNBOX
     */
    public static boolean canCast(long flags, Class<?> left, Class<?> right) {
        if (left.isAssignableFrom(right)) {
            return true;
        }

        if (left.isPrimitive()) {
            return Flags.all(flags, UNBOX) && unbox(right) == left
                   || Flags.all(flags, WIDEN) && (Flags.all(flags, UNBOX) || right.isPrimitive()) && TypeInfo.of(left).canWiden(TypeInfo.of(right));
        }

        return left == box(right);
    }

    /**
     Check whether an assignment is legal according to the {@linkplain #DEFAULT_CONVERSION default conversion flags}.

     @return {@link #canCast(long, Class, Class)} with the default conversion flags.
     */
    public static boolean canCast(Class<?> left, Class<?> right) {
        return canCast(DEFAULT_CONVERSION, left, right);
    }

    /**
     Test whether a sequence of types can be assigned from a parallel array of types with optional coercion.

     @param flags  see {@link #UNBOX} and {@link #WIDEN}
     @param offset offset of the first type in {@code left}
     @return whether the types in {@code left} can be assigned from the types in {@code right}
     */
    public static boolean canCast(long flags, int offset, Class<?>[] left, Class<?>... right) {
        if (left.length == right.length + offset) {
            if (flags == 0) {
                return Arrays.equals(left, offset, left.length, right, 0, right.length);
            }

            for (var i = offset; i != left.length; i++) {
                if (Flags.all(flags, UNBOX)) {
                    if (!canCast(flags, left[i], right[i - offset])) {
                        return false;
                    }
                } else if (!left[i].isAssignableFrom(right[i - offset])) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public static boolean canCast(long flags, int offset, Class<?>[] left, Object... right) {
        return canCast(flags, offset, left, Stream.of(right).map(Object::getClass).toArray(Class[]::new));
    }

    public static boolean canCast(long flags, int offset, Executable executable, Class<?>... types) {
        return canCast(flags, offset, executable.getParameterTypes(), types);
    }

    public static boolean canCast(long flags, int offset, Executable executable, Object... arguments) {
        return canCast(flags, offset, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(long flags, Class<?>[] left, Class<?>... right) {
        return canCast(flags, 0, left, right);
    }

    public static boolean canCast(long flags, Class<?>[] left, Object... right) {
        return canCast(flags, 0, left, right);
    }

    public static boolean canCast(long flags, Executable executable, Class<?>... types) {
        return canCast(flags, 0, executable.getParameterTypes(), types);
    }

    public static boolean canCast(long flags, Executable executable, Object... arguments) {
        return canCast(flags, 0, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(int offset, Class<?>[] left, Class<?>... right) {
        return canCast(DEFAULT_CONVERSION, offset, left, right);
    }

    public static boolean canCast(int offset, Class<?>[] left, Object... right) {
        return canCast(DEFAULT_CONVERSION, offset, left, right);
    }

    public static boolean canCast(int offset, Executable executable, Object... arguments) {
        return canCast(DEFAULT_CONVERSION, offset, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(Class<?>[] left, Class<?>... right) {
        return canCast(DEFAULT_CONVERSION, 0, left, right);
    }

    public static boolean canCast(Class<?>[] left, Object... right) {
        return canCast(DEFAULT_CONVERSION, 0, left, right);
    }

    public static boolean canCast(Executable executable, Class<?>... types) {
        return canCast(DEFAULT_CONVERSION, 0, executable.getParameterTypes(), types);
    }

    public static boolean canCast(Executable executable, Object... arguments) {
        return canCast(DEFAULT_CONVERSION, 0, executable.getParameterTypes(), arguments);
    }

    /**
     @param array an array
     @param <T>   the type of the boxed array
     @return the boxed version of {@code array} if its component type is primitive; {@code array} otherwise
     */
    public static <T> T[] box(Object array) {
        var type = array.getClass().componentType();
        var wrapper = box(type);

        if (wrapper == type) {
            return (T[]) array;
        }

        var length = Array.getLength(array);
        var boxed = (T[]) Array.newInstance(wrapper, length);
        IntStream.range(0, length).forEach(index -> Array.set(boxed, index, Array.get(array, index)));

        return boxed;
    }

    /**
     @param array an array
     @param <T>   the type of the unboxed array
     @return the unboxed version of {@code array} if its component type is a primitive wrapper type; {@code array} if its component type is primitive; {@literal null} otherwise
     */
    public static <T> T unbox(Object array) {
        var type = array.getClass().componentType();

        if (type.isPrimitive()) {
            return (T) array;
        }

        var primitive = unbox(type);

        if (primitive == null || primitive == type) {
            return nul();
        }

        var cast = (Object[]) array;
        var length = cast.length;
        var unboxed = (T) Array.newInstance(primitive, length);
        IntStream.range(0, length).forEach(index -> Array.set(unboxed, index, cast[index]));

        return unboxed;
    }
}
