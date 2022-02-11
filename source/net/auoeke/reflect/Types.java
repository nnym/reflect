package net.auoeke.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Types {
    // @formatter:off
    /**
     Widen primitives according to the automatic widening in Java.
     */
    public static final long WIDEN  = 1;

    /**
     Unbox primitive wrappers if they match the target primitive type.
     */
    public static final long UNBOX  = 1 << 1;

    /**
     Box primitives in order to match their wrappers.
     */
    public static final long BOX    = 1 << 2;

    /**
     Rewrap wrappers in wider types.
     */
    public static final long REWRAP = 1 << 3;
    // @formatter:on

    /**
     {@linkplain #WIDEN Widen primitives} and {@linkplain #UNBOX unbox wrappers}.
     */
    public static final long DEFAULT_CONVERSION = WIDEN | UNBOX | BOX;

    private static final CacheMap<Class<?>, Integer> classDepths = CacheMap.identity();
    private static final CacheMap<Class<?>, Integer> interfaceDepths = CacheMap.identity();

    /**
     Return a stream of a type's interfaces.

     @param type a type
     @return the type's interfaces
     */
    public static Stream<Class<?>> interfaces(Class<?> type) {
        return Stream.of(type.getInterfaces());
    }

    /**
     Return a type's supertypes in declaration order.

     @param type a type
     @param <T>  {@code type}
     @return {@code type}'s supertypes
     @throws NullPointerException if {@code type} is {@code null}
     */
    public static Stream<Class<?>> supertypes(Class<?> type) {
        var superclass = type.getSuperclass();
        var interfaces = interfaces(type);
        return superclass == null ? interfaces : Stream.concat(Stream.of(superclass), interfaces);
    }

    /**
     Get a class hierarchy starting at a class and ending at one of its ancestors.

     @param begin the most derived type
     @param end   the oldest ancestor; may be {@code null} (exclusive)
     @param <T>   the type of the initial class
     @return the hierarchy
     */
    public static <T> Stream<Class<?>> classes(Class<T> begin, Class<?> end) {
        return Stream.iterate(begin, type -> type != end, Class::getSuperclass);
    }

    /**
     Get the hierarchy of a class.

     @param begin the most derived type
     @param <T>   the most derived type
     @return the hierarchy
     */
    public static <T> Stream<Class<?>> classes(Class<T> begin) {
        return classes(begin, null);
    }

    /**
     Get a type's all inherited or declared interfaces.

     @param type a type
     @return all of its inherited or declared interfaces
     */
    public static Stream<Class<?>> allInterfaces(Class<?> type) {
        return classes(type).flatMap(Types::interfaces).distinct();
    }

    /**
     Return a stream of a type and its every base type.

     @param type a type
     @param <T>  {@code type}
     @return {@code type} and its every base type
     */
    public static <T> Stream<Class<?>> hierarchy(Class<T> type) {
        return Stream.of(type).<Class<?>>mapMulti((t, add) -> {
            add.accept(t);
            supertypes(type).forEach(supertype -> hierarchy(supertype).forEach(add));
        }).distinct();
    }

    /**
     Count a type's class or interface depth. A class' interface depth is 1 greater than the greatest interface depth of any of its declared or inherited interfaces.
     If {@code type} is null, then return 0.

     @param type       a type
     @param interfaces whether to compute interface depth instead of class depth
     @return the depth
     */
    public static int depth(Class<?> type, boolean interfaces) {
        return type == null ? 0
            : interfaces ? 1 + interfaceDepths.computeIfAbsent(type, t -> allInterfaces(t).mapToInt(t1 -> depth(t1, true)).max().orElse(0))
            : classDepths.computeIfAbsent(type, t -> (int) classes(t).count());
    }

    /**
     Count interface depth if {@code type} is an interface; else count class depth.

     @param type a type
     @return the depth
     @see #depth(Class, boolean)
     */
    public static int depth(Class<?> type) {
        return depth(type, type != null && type.isInterface());
    }

    /**
     Compute the generation gap between 2 types. The gap is positive if {@code a} is more derived than {@code b} or negative if {@code b} is more derived than {@code a}.
     If the types are unrelated, then return {@link Integer#MAX_VALUE}.

     @param a          a type
     @param b          a type
     @param interfaces whether to compute the generation gap in terms of interface depth instead of class depth
     @return the generation gap between {@code a} and {@code b}
     */
    public static int difference(Class<?> a, Class<?> b, boolean interfaces) {
        return a == null || b == null || a.isAssignableFrom(b) || b.isAssignableFrom(a) ? depth(a, interfaces) - depth(b, interfaces) : Integer.MAX_VALUE;

    }

    /**
     Count the generation gap between 2 types. The gap is positive if {@code a} is more derived than {@code b} or negative if {@code b} is more derived than {@code a}.
     <p>
     If the types are unrelated, then return {@link Integer#MAX_VALUE}.
     <p>
     If a or b is an interface, then count the interface generation gap.

     @param a a type
     @param b a type
     @return the generation gap between {@code a} and {@code b}
     @see #difference(Class, Class, boolean)
     */
    public static int difference(Class<?> a, Class<?> b) {
        return difference(a, b, a != null && a.isInterface() || b != null && b.isInterface());
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
     @throws NullPointerException if {@code left} or {@code right} is {@code null}
     @see #WIDEN
     @see #UNBOX
     @see #BOX
     @see #REWRAP
     */
    public static boolean canCast(long flags, Class<?> left, Class<?> right) {
        if (left.isAssignableFrom(right)) {
            return true;
        }

        if (left.isPrimitive()) {
            return Flags.any(flags, UNBOX) && unbox(right) == left
                   || Flags.any(flags, WIDEN) && (Flags.any(flags, UNBOX) || right.isPrimitive()) && TypeInfo.of(left).canWiden(TypeInfo.of(right));
        }

        return Flags.any(flags, BOX) && left == box(right)
               || Flags.any(flags, REWRAP) && !right.isPrimitive() && TypeInfo.of(left).canWiden(TypeInfo.of(right));
    }

    /**
     Check whether an assignment is legal according to the {@linkplain #DEFAULT_CONVERSION default conversion flags}.

     @param left  the type to which assignment is to be tested
     @param right the type from which assignment is to be tested
     @return {@link #canCast(long, Class, Class)} with the default conversion flags
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
                if (!canCast(flags, left[i], right[i - offset])) {
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

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(long flags, int offset, Executable executable, Class<?>... types) {
        return canCast(flags, offset, executable.getParameterTypes(), types);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(long flags, int offset, Executable executable, Object... arguments) {
        return canCast(flags, offset, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(long flags, Class<?>[] left, Class<?>... right) {
        return canCast(flags, 0, left, right);
    }

    public static boolean canCast(long flags, Class<?>[] left, Object... right) {
        return canCast(flags, 0, left, right);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(long flags, Executable executable, Class<?>... types) {
        return canCast(flags, 0, executable.getParameterTypes(), types);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(long flags, Executable executable, Object... arguments) {
        return canCast(flags, 0, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(int offset, Class<?>[] left, Class<?>... right) {
        return canCast(DEFAULT_CONVERSION, offset, left, right);
    }

    public static boolean canCast(int offset, Class<?>[] left, Object... right) {
        return canCast(DEFAULT_CONVERSION, offset, left, right);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(int offset, Executable executable, Object... arguments) {
        return canCast(DEFAULT_CONVERSION, offset, executable.getParameterTypes(), arguments);
    }

    public static boolean canCast(Class<?>[] left, Class<?>... right) {
        return canCast(DEFAULT_CONVERSION, 0, left, right);
    }

    public static boolean canCast(Class<?>[] left, Object... right) {
        return canCast(DEFAULT_CONVERSION, 0, left, right);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
    public static boolean canCast(Executable executable, Class<?>... types) {
        return canCast(DEFAULT_CONVERSION, 0, executable.getParameterTypes(), types);
    }

    @Deprecated(forRemoval = true, since = "4.5.0") // This is excessive.
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
            return null;
        }

        var cast = (Object[]) array;
        var length = cast.length;
        var unboxed = (T) Array.newInstance(primitive, length);
        IntStream.range(0, length).forEach(index -> Array.set(unboxed, index, cast[index]));

        return unboxed;
    }
}
