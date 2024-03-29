package net.auoeke.reflect;

import net.auoeke.result.Result;
import net.gudenau.lib.unsafe.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

import static net.auoeke.dycon.Dycon.ldc;

/**
 @since 1.4.0
 */
public class Constructors {
	/**
	 Return a stream of a type's constructors.

	 @param type a type
	 @return a stream containing the type's constructors
	 */
	public static <T> Stream<Constructor<T>> of(Class<T> type) {
		return Stream.of((Constructor<T>[]) type.getDeclaredConstructors());
	}

	/**
	 Returns the canonical constructor of a record type.

	 @param type a record type
	 @param <T> {@code type}
	 @return {@code type}'s canonical constructor
	 @throws NullPointerException if {@code type} is {@code null} or not a record type
	 @since 6.2.0
	 */
	public static <T extends Record> Constructor<T> canonical(Class<T> type) {
		return find(type, Stream.of(type.getRecordComponents()).map(RecordComponent::getType).toArray(Class[]::new));
	}

	/**
	 Instantiate {@code type} through a default constructor if it is available or fall back to {@link Unsafe#allocateInstance(Class)}.

	 @return the new instance; never {@code null}
	 @throws InstantiationException if {@code type} is abstract
	 */
	public static <T> T instantiate(Class<T> type) {
		return Result.of(() -> (T) Invoker.findConstructor(type).invoke())
			.or(() -> Unsafe.allocateInstance(type))
			.value();
	}

	public static <T> T construct(Class<T> type, Object... arguments) {
		return (T) Invoker.unreflectConstructor(find(type, arguments)).invokeWithArguments(arguments);
	}

	public static <T> Constructor<T> find(long flags, int offset, Class<T> type, Object... arguments) {
		return Methods.find(flags, offset, of(type), arguments);
	}

	public static <T> Constructor<T> find(long flags, Class<T> type, Object... arguments) {
		return Methods.find(flags, of(type), arguments);
	}

	public static <T> Constructor<T> find(int offset, Class<T> type, Object... arguments) {
		return Methods.find(offset, of(type), arguments);
	}

	public static <T> Constructor<T> find(int offset, Class<T> type, Class<?>... parameterTypes) {
		return Methods.find(offset, of(type), parameterTypes);
	}

	public static <T> Constructor<T> find(Class<T> type, Object... arguments) {
		return Methods.find(Types.DEFAULT_CONVERSION, of(type), arguments);
	}

	public static <T> Constructor<T> find(Class<T> type, Class<?>... parameterTypes) {
		return Methods.find(of(type), parameterTypes);
	}

	/**
	 Copies a constructor without its {@link AccessibleObject#setAccessible(boolean) accessibility} flag.
	 If the constructor is {@code null}, then {@code null} is returned.

	 @param constructor a constructor
	 @return a copy of {@code constructor} with its accessibility flag not set
	 @since 5.3.0
	 */
	public static Constructor copy(Constructor constructor) {
		if (constructor == null) {
			return null;
		}

		var root = AccessibleObjects.root(constructor);
		return root == null ? (Constructor) ldc(() -> Invoker.findSpecial(Constructor.class, "copy", Constructor.class)).invokeExact(constructor) : copy(root);
	}
}
