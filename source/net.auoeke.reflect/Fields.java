package net.auoeke.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

import static net.auoeke.dycon.Dycon.*;

/**
 @since 0.6.0
 */
public class Fields {
	public static final Pointer modifiers = Pointer.of(Field.class, "modifiers");
	public static final Pointer override = Pointer.of(AccessibleObject.class, "override");

	/**
	 Compute the offset of a field from its (type or object) base.
	 If the field is static, then the base is its declaring type;
	 otherwise, the base is an instance of its declaring type.

	 @param field a field
	 @return the field's offset from its base
	 @since 4.10.0
	 */
	public static long offset(Field field) {
		return Flags.isStatic(field) ? Unsafe.staticFieldOffset(field) : Unsafe.objectFieldOffset(field);
	}

	/**
	 Compute the offset of a field from its (type or object) base.
	 If the field is static, then the base is its declaring type;
	 otherwise, the base is an instance of its declaring type.

	 @param declaringType the field's declaring type
	 @param name the field's name
	 @return the field's offset from its base
	 @since 5.0.0
	 */
	public static long offset(Class<?> declaringType, String name) {
		return offset(of(declaringType, name));
	}

	/**
	 Retrieve an array of fields declared by a type without filtering.

	 @param type a type
	 @return an array of {@code type}'s all fields
	 */
	public static Field[] direct(Class<?> type) {
		return (Field[]) ldc(() -> Methods.of(Class.class)
			.filter(method -> Flags.isNative(method) && method.getReturnType() == Field[].class)
			.map(Invoker::unreflectSpecial)
			.map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method)
			.max(Comparator.comparing(method -> ((Field[]) method.invoke(Fields.class)).length))
			.get()
		).invokeExact(type);
	}

	/**
	 Return a stream of all fields declared by a type. The fields are cached and shared by all callers.

	 @param type a type
	 @return a stream of {@code type}'s all fields
	 */
	public static Stream<Field> of(Class<?> type) {
		return Stream.of(ldc(CacheMap::<Class<?>, Field[]>identity).computeIfAbsent(type, Fields::direct));
	}

	/**
	 Attempt to locate a field declared by a type by name.

	 @param type a type
	 @param name the name of a field declared by {@code type}
	 @return the field declared by {@code type} and named by {@code name} if found or else {@code null}
	 */
	public static Field of(Class<?> type, String name) {
		return ldc(CacheMap::<Class<?>, CacheMap<String, Field>>identity)
			.computeIfAbsent(type, t -> of(t).collect(CacheMap::hash, (map, field) -> map.put(field.getName(), field), CacheMap::putAll))
			.get(name);
	}

	/**
	 Attempt to locate a field in an object by name.
	 If the object contains multiple fields of the same name; then the field declared by the most derived type is chosen.

	 @param object an object
	 @param name the name of a field in {@code object}
	 @return the field in {@code object} and named by {@code name} if found or else {@code null}
	 */
	public static Field of(Object object, String name) {
		return any(object.getClass(), name);
	}

	/**
	 Return a sequential stream of all fields declared by {@code begin} and its base classes.
	 The fields in the stream are in class hierarchy order starting with the most derived class {@code begin}.

	 @param begin the first class wherefrom to begin searching
	 @return a sequential stream of all fields declared by {@code begin} and its base classes
	 */
	public static Stream<Field> all(Class<?> begin) {
		return Types.classes(begin, Object.class).flatMap(Fields::of);
	}

	public static Field any(Class<?> type, String name) {
		return all(type).filter(field -> field.getName().equals(name)).findFirst().orElse(null);
	}

	public static Stream<Field> staticOf(Class<?> type) {
		return Stream.of(ldc(CacheMap::<Class<?>, Field[]>identity)
			.computeIfAbsent(type, t -> of(t).filter(Flags::isStatic).toArray(Field[]::new))
		);
	}

	public static Stream<Field> instanceOf(Class<?> type) {
		return Stream.of(ldc(CacheMap::<Class<?>, Field[]>identity)
			.computeIfAbsent(type, t -> of(t).filter(Flags::isInstance).toArray(Field[]::new))
		);
	}

	public static Stream<Field> allInstance(Class<?> type) {
		return Types.classes(type, Object.class).flatMap(Fields::of).filter(Flags::isInstance);
	}

	public static Stream<Field> allStatic(Class<?> type) {
		return Types.classes(type, Object.class).flatMap(Fields::of).filter(Flags::isStatic);
	}

	/**
	 Copies a field without its {@link AccessibleObject#setAccessible(boolean) accessibility} flag.
	 If the field is {@code null}, then {@code null} is returned.

	 @param field a field
	 @return a copy of {@code field} with its accessibility flag not set
	 @since 5.3.0
	 */
	public static Field copy(Field field) {
		if (field == null) {
			return null;
		}

		var root = AccessibleObjects.root(field);
		return root == null ? (Field) ldc(() -> Invoker.findSpecial(Field.class, "copy", Field.class)).invokeExact(field) : copy(root);
	}
}
