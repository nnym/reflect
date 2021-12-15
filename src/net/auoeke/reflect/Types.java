package net.auoeke.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.auoeke.reflect.Reflect.nul;

@SuppressWarnings("unused")
public class Types {
    /** Widen primitives. */
    public static long WIDEN = 1;

    /** Unbox primitive wrappers. */
    public static long UNBOX = 1 << 1;

    public static long DEFAULT_CONVERSION = WIDEN | UNBOX;

    public static <T> Stream<Class<? super T>> classes(Class<T> begin, Class<?> end) {
        return Stream.iterate(begin, type -> type != end, Class::getSuperclass);
    }

    public static <T> Stream<Class<? super T>> classes(Class<T> begin) {
        return classes(begin, null);
    }

    public static int size(Class<?> type) {
        return TypeInfo.of(type).size;
    }

    public static Class<?> unbox(Class<?> type) {
        return type.isPrimitive() ? nul() : TypeInfo.of(type).primitive;
    }

    public static Class<?> box(Class<?> type) {
        return type.isPrimitive() ? TypeInfo.of(type).reference : nul();
    }

    public static boolean equals(Class<?> type, Class<?> other) {
        return type == other || type != null && (unbox(type) == other || unbox(other) == type);
    }

    public static boolean isWrapper(Class<?> type) {
        return unbox(type) != null;
    }

    /** {@code left} var = {@code right}; */
    public static boolean canCast(long flags, Class<?> left, Class<?> right) {
        if (left.isAssignableFrom(right)) {
            return true;
        }

        if (left.isPrimitive()) {
            return Flags.all(flags, UNBOX) && !right.isPrimitive() && left == TypeInfo.of(right).primitive
                || Flags.all(flags, WIDEN) && (Flags.all(flags, UNBOX) || right.isPrimitive()) && TypeInfo.of(left).canWiden(TypeInfo.of(right));
        }

        return left == box(right);
    }

    public static boolean canCast(Class<?> left, Class<?> right) {
        return canCast(DEFAULT_CONVERSION, left, right);
    }

    /**
     @param flags see {@link #UNBOX} and {@link #WIDEN}
     @param offset offset of the first type in {@code left}
     @return whether {@code left} can be assigned to {@code right}
     */
    public static boolean canCast(long flags, int offset, Class<?>[] left, Class<?>... right) {
        if (left.length == right.length + offset) {
            for (int i = offset, length = left.length; i != length; i++) {
                if (Flags.all(flags, UNBOX)) {
                    if (!canCast(left[i], right[i - offset])) {
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
     @param <T>   a desired type for convenience casting
     @return the boxed version of {@code array} if its component type is primitive; {@code array} otherwise
     */
    public static <T> T[] box(Object array) {
        var type = array.getClass().componentType();
        var wrapper = box(type);

        if (wrapper == null) {
            return (T[]) array;
        }

        var length = Array.getLength(array);
        var boxed = (T[]) Array.newInstance(wrapper, length);
        IntStream.range(0, length).forEach(index -> Array.set(boxed, index, Array.get(array, index)));

        return boxed;
    }

    /**
     @param array an array
     @param <T>   a desired type for convenience casting
     @return the unboxed version of {@code array} if its component type is a primitive wrapper type; {@code array} if its component type is primitive; {@literal null} otherwise
     */
    public static <T> T unbox(Object array) {
        var type = array.getClass().componentType();

        if (type.isPrimitive()) {
            return (T) array;
        }

        var primitive = unbox(type);

        if (primitive == null) {
            return nul();
        }

        var cast = (Object[]) array;
        var length = cast.length;
        var unboxed = (T) Array.newInstance(primitive, length);
        IntStream.range(0, length).forEach(index -> Array.set(unboxed, index, cast[index]));

        return unboxed;
    }
}
