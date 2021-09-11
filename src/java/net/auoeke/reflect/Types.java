package net.auoeke.reflect;

import java.lang.reflect.Array;

public class Types {
    public static boolean equals(Class<?> klass, Class<?> other) {
        return klass == other || klass != null && (primitive(klass) == other || primitive(other) == klass);
    }

    public static boolean isWrapper(Class<?> klass) {
        return primitive(klass) != null;
    }

    public static Class<?> primitive(Class<?> klass) {
        if (klass == Void.class) return void.class;
        if (klass == Boolean.class) return boolean.class;
        if (klass == Byte.class) return byte.class;
        if (klass == Character.class) return char.class;
        if (klass == Short.class) return short.class;
        if (klass == Integer.class) return int.class;
        if (klass == Long.class) return long.class;
        if (klass == Float.class) return float.class;
        if (klass == Double.class) return double.class;

        return Reflect.nul();
    }

    public static Class<?> wrapper(Class<?> klass) {
        if (klass == void.class) return Void.class;
        if (klass == boolean.class) return Boolean.class;
        if (klass == byte.class) return Byte.class;
        if (klass == char.class) return Character.class;
        if (klass == short.class) return Short.class;
        if (klass == int.class) return Integer.class;
        if (klass == long.class) return Long.class;
        if (klass == float.class) return Float.class;
        if (klass == double.class) return Double.class;

        return Reflect.nul();
    }

    /**
     @param array an array
     @param <T>   a desired type for convenience casting
     @return the boxed version of {@code array} if its component type is primitive; {@code array} otherwise
     */
    public static <T> T[] box(Object array) {
        var type = array.getClass().componentType();
        var wrapper = wrapper(type);

        if (wrapper == null) {
            return (T[]) array;
        }

        var length = Array.getLength(array);
        var boxed = (T[]) Array.newInstance(wrapper, length);

        for (int index = 0; index < length; index++) {
            Array.set(boxed, index, Array.get(array, index));
        }

        return boxed;

    }

    /**
     @param array an array
     @param <T>   a desired type for convenience casting
     @return the unboxed version of {@code array} if its component type is a primitive wrapper type; {@literal null} otherwise
     */
    public static <T> T unbox(Object array) {
        var type = array.getClass().componentType();

        if (type.isPrimitive()) {
            return (T) array;
        }

        var primitive = primitive(type);

        if (primitive == null) {
            return Reflect.nul();
        }

        var cast = (Object[]) array;
        var length = cast.length;
        var unboxed = (T) Array.newInstance(primitive, length);

        for (int index = 0; index < length; index++) {
            Array.set(unboxed, index, cast[index]);
        }

        return unboxed;
    }
}
