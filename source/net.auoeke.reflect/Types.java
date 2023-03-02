package net.auoeke.reflect;

import net.gudenau.lib.unsafe.Unsafe;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.auoeke.dycon.Dycon.ldc;

/**
 Utilities that deal with types and type conversion.

 @since 0.14.0
 */
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
	public static final long BOX	= 1 << 2;

	/**
	 Rewrap wrappers in wider types.
	 */
	public static final long REWRAP = 1 << 3;
	// @formatter:on

	/**
	 {@link #WIDEN Widen primitives} and {@link #UNBOX unbox wrappers}.
	 */
	public static final long DEFAULT_CONVERSION = WIDEN | UNBOX | BOX;

	/**
	 @return a stream of the primitive field types.
	 @since 6.0.0
	 */
	public static Stream<Class<?>> stackPrimitives() {
		return Stream.of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class);
	}

	/**
	 @return a stream of the base field types (primitive types and {@link Object}).
	 @since 6.0.0
	 */
	public static Stream<Class<?>> stackBase() {
		return Stream.of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, Object.class);
	}

	/**
	 @return a stream of the primitive method return types (field primitive types and {@code void}).
	 */
	public static Stream<Class<?>> returnPrimitives() {
		return Stream.of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, void.class);
	}

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
	 @param end the oldest ancestor; may be {@code null} (exclusive)
	 @param <T> the type of the initial class
	 @return the hierarchy
	 */
	public static <T> Stream<Class<?>> classes(Class<T> begin, Class<?> end) {
		return Stream.iterate(begin, type -> type != end, Class::getSuperclass);
	}

	/**
	 Get the hierarchy of a class.

	 @param begin the most derived type
	 @param <T> the most derived type
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
	 Returns a sequential stream of a type and its every base type traversed inorder.

	 @param type a type
	 @param <T> {@code type}
	 @return {@code type} and its every base type
	 */
	public static <T> Stream<Class<?>> hierarchy(Class<T> type) {
		return Stream.of(type).<Class<?>>mapMulti((t, add) -> {
			add.accept(t);
			supertypes(type).forEach(supertype -> hierarchy(supertype).forEach(add));
		}).distinct();
	}

	/**
	 Returns a stream of {@code type}'s all base types.

	 @param type a type
	 @return {@code type}'s all base types
	 @since 5.3.0
	 */
	public static Stream<Class<?>> baseTypes(Class<?> type) {
		return supertypes(type).flatMap(Types::hierarchy).distinct();
	}

	/**
	 Count a type's class or interface depth. A class' interface depth is 1 greater than the greatest interface depth of any of its declared or inherited interfaces.
	 If {@code type} is null, then return 0.

	 @param type a type
	 @param interfaces whether to compute interface depth instead of class depth
	 @return the depth
	 */
	public static int depth(Class<?> type, boolean interfaces) {
		return type == null ? 0
			: interfaces ? 1 + ldc(CacheMap::<Class<?>, Integer>identity).computeIfAbsent(type, t -> allInterfaces(t).mapToInt(t1 -> depth(t1, true)).max().orElse(0))
			: ldc(CacheMap::<Class<?>, Integer>identity).computeIfAbsent(type, t -> (int) classes(t).count());
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

	 @param a a type
	 @param b a type
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
	 Returns the size of an instance of {@code type} on the stack in bytes.

	 <p>The stack size of {@code void} is 0.
	 <br>The stack size of {@code boolean} is that of {@code byte}.
	 <br>The stack size of every other primitive value is the value of field {@code BYTES} in its wrapper class.
	 <br>The stack size of {@code type <: Object} is {@link sun.misc.Unsafe#addressSize the size of a reference}.

	 @param type a type
	 @return the size of an instance of {@code type} on the stack
	 @throws NullPointerException if {@code type == null}
	 @since 6.0.0
	 */
	public static int stackSize(Class<?> type) {
		return (type.isPrimitive() ? TypeInfo.of(type) : TypeInfo.REFERENCE).size;
	}

	/**
	 Returns an estimate of the size of an instance of {@code type} in bytes.

	 <p>The estimated size of {@code void} and every primitive value is its {@link #stackSize stack size}.
	 <br>The estimated size of an array is the sum of its {@link sun.misc.Unsafe#arrayBaseOffset base offset} and its length multiplied by its {@link sun.misc.Unsafe#arrayIndexScale index scale}.
	 <br>The estimated size of an instance {@code i} of {@code type <: Object} is the sum of its last {@link sun.misc.Unsafe#objectFieldOffset field's offset} and its {@link #stackSize stack size}.
	 If {@code i} does not have fields, then its estimated size is the offset of an object's {@link Classes#firstField first field}.

	 @param type a type
	 @return an estimated size of an instance of {@code type}
	 @throws NullPointerException if {@code type == null}
	 @since 6.0.0
	 */
	public static int size(Class<?> type) {
		return ldc(() -> {
			var map = CacheMap.<Class<?>, Integer>identity();
			map.putAll(Map.of(
				void.class, 0,
				boolean.class, Byte.BYTES,
				byte.class, Byte.BYTES,
				char.class, Character.BYTES,
				short.class, Short.BYTES,
				int.class, Integer.BYTES,
				float.class, Float.BYTES,
				long.class, Long.BYTES,
				double.class, Double.BYTES
			));

			return map;
		}).computeIfAbsent(type, t -> t.isArray() ? Unsafe.arrayBaseOffset(t) + Unsafe.arrayIndexScale(t) * Array.getLength(t)
			: (int) classes(type).flatMap(Fields::instanceOf)
			.mapToLong(field -> Unsafe.objectFieldOffset(field) + stackSize(field.getType()))
			.max()
			.orElse(Classes.firstField.offset)
		);
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
	 Returns whether 2 given types are the same or {@code null}
	 or if one of them is primitive and the other is its wrapper type.

	 @param type a type
	 @param other another type
	 @return whether {@code type} and {@code other} are the same or one of them is a wrapper of the other
	 @since 7.0.0
	 */
	public static boolean similar(Class<?> type, Class<?> other) {
		return type == other || type != null && other != null && (unbox(type) == other || unbox(other) == type);
	}

	/**
	 Check whether 2 types are the same or if one of them is a primitive and the other is its wrapper type.

	 @param type a type
	 @param other another type
	 @return whether they are the some or one of them is a wrapper of the other
	 @deprecated for removal in 7.0.0. This method's name is misleading; use {@link #similar}.
	 */
	@Deprecated(since = "6.3.0", forRemoval = true)
	public static boolean equals(Class<?> type, Class<?> other) {
		return similar(type, other);
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
	 Determines whether {@code subtype} extends {@code supertype}.

	 @param subtype the type to check as the subtype; may be null
	 @param supertype the type to check as the supertype; may be null
	 @return whether {@code subtype} extends {@code supertype}
	 @since 5.3.0
	 */
	public static boolean isSubtype(Class<?> subtype, Class<?> supertype) {
		return subtype != null && supertype != null && subtype != supertype && supertype.isAssignableFrom(subtype);
	}

	/**
	 Returns the raw upper bounds of a type by recursively flattening {@code type}.
	 The upper bound of a {@link Class} is itself.

	 @param type a type
	 @return {@code type}'s raw upper bounds
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is not a {@link Class}, {@link GenericArrayType}, {@link ParameterizedType}, {@link TypeVariable} or {@link WildcardType}.
	 @since 6.3.0
	 */
	public static Stream<Class<?>> upperBounds(Type type) {
		if (type instanceof Class<?> clas) return Stream.of(clas);
		if (type instanceof WildcardType wildcard) return Stream.of(wildcard.getUpperBounds()).flatMap(Types::upperBounds);
		if (type instanceof ParameterizedType parameterized) return upperBounds(parameterized.getRawType());
		if (type instanceof TypeVariable<?> variable) return Stream.of(variable.getBounds()).flatMap(Types::upperBounds);
		if (type instanceof GenericArrayType array) return upperBounds(array.getGenericComponentType()).map(Class::arrayType);

		throw new IllegalArgumentException("%s %s".formatted(type.getClass().getSimpleName(), type));
	}

	/**
	 Returns the raw lower bounds of a type by recursively flattening {@code type}.
	 Final types are not considered as lower bounds.

	 @param type a type
	 @return {@code type}'s raw lower bounds
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is not a {@link Class}, {@link GenericArrayType}, {@link ParameterizedType}, {@link TypeVariable} or {@link WildcardType}.
	 @since 6.3.0
	 */
	public static Stream<Class<?>> lowerBounds(Type type) {
		if (type instanceof Class<?> || type instanceof ParameterizedType || type instanceof TypeVariable<?>) return Stream.of();
		if (type instanceof WildcardType wildcard) return deepLowerBounds(type);
		if (type instanceof GenericArrayType array) return lowerBounds(array.getGenericComponentType()).map(Class::arrayType);

		throw new IllegalArgumentException("%s %s".formatted(type.getClass().getSimpleName(), type));
	}

	/**
	 Returns whether a {@link Type} {@code a} is assignable from another {@link Type} {@code b} by checking their raw
	 {@link #upperBounds upper} and {@link #lowerBounds lower} bounds.
	 {@code a} is assignable from {@code b} if a variable of type {@code a} can be assigned a value of type {@code b}.
	 <p>
	 This method is a generalization of {@link Class#isAssignableFrom} for the 5 standard {@link Type}s:
	 {@link Class}, {@link GenericArrayType}, {@link ParameterizedType}, {@link TypeVariable} and {@link WildcardType}.
	 Like {@link Class#isAssignableFrom}, this method returns {@code true} for primitive types {@code a} and {@code b}
	 only if {@code a == b}.

	 @param a a type on the left side of an assignment
	 @param b a type on the right side of an assignment
	 @return whether an assignment of {@code a} to {@code b} is valid
	 @since 6.3.0
	 */
	public static boolean canAssign(Type a, Type b) {
		if (a == null || b == null) return a == b;
		if (a instanceof Class<?> ca && b instanceof Class<?> cb) return ca.isAssignableFrom(cb);

		var bUpper = upperBounds(b).toList();
		var bLower = lowerBounds(b).toList();

		if (!lowerBounds(a).allMatch(al -> bUpper.stream().anyMatch(al::isAssignableFrom))) {
			return false;
		}

		if (bLower.isEmpty()) {
			return upperBounds(a).allMatch(au -> bUpper.stream().anyMatch(au::isAssignableFrom));
		}

		var aUpper = upperBounds(a).toList();

		return aUpper.stream().allMatch(au -> bLower.stream().anyMatch(au::isAssignableFrom))
			&& bUpper.stream().allMatch(bu -> aUpper.stream().anyMatch(bu::isAssignableFrom));
	}

	/**
	 Check whether an assignment is legal according to the given conversion flags.
	 <p>
	 {@code left variable = right;}

	 @param flags the conversion flags
	 @param left the type to which to assign
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
	 Check whether an assignment is legal according to the {@link #DEFAULT_CONVERSION default conversion flags}.

	 @param left the type to which assignment is to be tested
	 @param right the type from which assignment is to be tested
	 @return {@link #canCast(long, Class, Class)} with the default conversion flags
	 */
	public static boolean canCast(Class<?> left, Class<?> right) {
		return canCast(DEFAULT_CONVERSION, left, right);
	}

	/**
	 Test whether a sequence of types can be assigned from a parallel array of types with optional coercion.

	 @param flags see {@link #UNBOX} and {@link #WIDEN}
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

	public static boolean canCast(long flags, Class<?>[] left, Class<?>... right) {
		return canCast(flags, 0, left, right);
	}

	public static boolean canCast(long flags, Class<?>[] left, Object... right) {
		return canCast(flags, 0, left, right);
	}

	public static boolean canCast(int offset, Class<?>[] left, Class<?>... right) {
		return canCast(DEFAULT_CONVERSION, offset, left, right);
	}

	public static boolean canCast(int offset, Class<?>[] left, Object... right) {
		return canCast(DEFAULT_CONVERSION, offset, left, right);
	}

	public static boolean canCast(Class<?>[] left, Class<?>... right) {
		return canCast(DEFAULT_CONVERSION, 0, left, right);
	}

	public static boolean canCast(Class<?>[] left, Object... right) {
		return canCast(DEFAULT_CONVERSION, 0, left, right);
	}

	/**
	 Box an array and its elements if it is not already of an object type.

	 @param array an array
	 @param <T> the type of the boxed array
	 @return a boxed version of {@code array} if its component type is primitive or else {@code array}
	 */
	public static <T> T[] box(Object array) {
		// @formatter:off
		return (T[]) (
			array instanceof boolean[] b ? box(b)
			: array instanceof byte[] b ? box(b)
			: array instanceof char[] c ? box(c)
			: array instanceof short[] s ? box(s)
			: array instanceof int[] i ? box(i)
			: array instanceof long[] l ? box(l)
			: array instanceof float[] f ? box(f)
			: array instanceof double[] d ? box(d)
			: array
		);
		// @formatter:on
	}

	/**
	 Unbox an array and its elements if it is not already primitive.

	 @param array an array
	 @param <T> the type of the unboxed array
	 @return the unboxed version of {@code array} if its component type is a primitive wrapper type; {@code array} if its component type is primitive; {@literal null} otherwise
	 */
	public static <T> T unbox(Object array) {
		var type = array.getClass().componentType();

		if (type.isPrimitive()) {
			return (T) array;
		}

		var primitive = unbox(type);
		return primitive == null || primitive == type ? null : convert(array, primitive);
	}

	/**
	 Return a primitive version of an array with a given primitive component type.
	 If the component type of the source array is the same as the target type, then return {@code array}.
	 Elements may be widened in order to match the target component type.

	 @param array an array
	 @param componentType the component type of the primitive array
	 @param <T> the type of the unboxed array
	 @return a primitive version of {@code array} with component type {@code componentType}
	 @throws IllegalArgumentException if {@code componentType} is not primitive
	 @since 4.12.0
	 */
	public static <T> T unbox(Object array, Class<?> componentType) {
		if (!componentType.isPrimitive()) {
			throw new IllegalArgumentException(componentType + " is not primitive");
		}

		return convert(array, componentType);
	}

	/**
	 Convert the component type of an array.
	 If the target type is the same as the component type of the source array, then return {@code array}.
	 Primitive elements may be widened and primitive wrappers may be unwrapped in order to match the target component type.

	 @param array an array
	 @param componentType the component type of the new array
	 @param <T> the type of the new array
	 @return an array with component type {@code componentType} and the elements of the previous array possibly widened or unboxed
	 @since 4.12.0
	 */
	public static <T> T convert(Object array, Class<?> componentType) {
		if (array.getClass().componentType() == componentType) {
			return (T) array;
		}

		var length = Array.getLength(array);
		var boxed = Array.newInstance(componentType, length);
		IntStream.range(0, length).forEach(index -> Array.set(boxed, index, Array.get(array, index)));

		return (T) boxed;
	}

	/**
	 Box a {@code boolean[]} by constructing a new {@code Boolean[]} containing wrappers of the elements of the original array.

	 @param array a {@code boolean[]} to box
	 @return a {@code Boolean[]} with the same length and elements as those of {@code array}
	 */
	public static Boolean[] box(boolean[] array) {
		var box = new Boolean[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code byte[]} by constructing a new {@code Byte[]} containing wrappers of the elements of the original array.

	 @param array a {@code byte[]} to box
	 @return a {@code Byte[]} with the same length and elements as those of {@code array}
	 */
	public static Byte[] box(byte[] array) {
		var box = new Byte[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code char[]} by constructing a new {@code Character[]} containing wrappers of the elements of the original array.

	 @param array a {@code char[]} to box
	 @return a {@code Character[]} with the same length and elements as those of {@code array}
	 */
	public static Character[] box(char[] array) {
		var box = new Character[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code short[]} by constructing a new {@code Short[]} containing wrappers of the elements of the original array.

	 @param array a {@code short[]} to box
	 @return a {@code Short[]} with the same length and elements as those of {@code array}
	 */
	public static Short[] box(short[] array) {
		var box = new Short[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code int[]} by constructing a new {@code Integer[]} containing wrappers of the elements of the original array.

	 @param array a {@code int[]} to box
	 @return a {@code Integer[]} with the same length and elements as those of {@code array}
	 */
	public static Integer[] box(int[] array) {
		var box = new Integer[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code long[]} by constructing a new {@code Long[]} containing wrappers of the elements of the original array.

	 @param array a {@code long[]} to box
	 @return a {@code Long[]} with the same length and elements as those of {@code array}
	 */
	public static Long[] box(long[] array) {
		var box = new Long[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code float[]} by constructing a new {@code Float[]} containing wrappers of the elements of the original array.

	 @param array a {@code float[]} to box
	 @return a {@code Float[]} with the same length and elements as those of {@code array}
	 */
	public static Float[] box(float[] array) {
		var box = new Float[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	/**
	 Box a {@code double[]} by constructing a new {@code Double[]} containing wrappers of the elements of the original array.

	 @param array a {@code double[]} to box
	 @return a {@code Double[]} with the same length and elements as those of {@code array}
	 */
	public static Double[] box(double[] array) {
		var box = new Double[array.length];

		for (var i = 0; i < array.length; i++) {
			box[i] = array[i];
		}

		return box;
	}

	private static Stream<Class<?>> deepLowerBounds(Type type) {
		if (type instanceof Class<?> clas) return Stream.of(clas);
		if (type instanceof WildcardType wildcard) return Stream.of(wildcard.getLowerBounds()).flatMap(Types::deepLowerBounds);
		if (type instanceof ParameterizedType parameterized) return deepLowerBounds(parameterized.getRawType());
		if (type instanceof TypeVariable<?> variable) return Stream.of(variable.getBounds()).flatMap(Types::deepLowerBounds);
		if (type instanceof GenericArrayType array) return deepLowerBounds(array.getGenericComponentType()).map(Class::arrayType);

		throw new IllegalArgumentException("%s %s".formatted(type.getClass().getSimpleName(), type));
	}
}
