package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import net.auoeke.result.Result;
import net.gudenau.lib.unsafe.Unsafe;

/**
 @since 1.4.0
 */
public class Constructors {
	private static final MethodHandle copy = Invoker.findSpecial(Constructor.class, "copy", Constructor.class);

	/**
	 Return a stream of a type's constructors.

	 @param type a type
	 @return a stream containing the type's constructors
	 */
	public static <T> Stream<Constructor<T>> of(Class<T> type) {
		return Stream.of((Constructor<T>[]) type.getDeclaredConstructors());
	}

	/**
	 Instantiate {@code type} through a default constructor if it is available or fall back to {@link Unsafe#allocateInstance(Class)}.

	 @return the new instance; never {@code null}
	 @throws InstantiationException if {@code type} is abstract
	 */
	public static <T> T instantiate(Class<T> type) {
		return (T) Result.of(() -> Invoker.findConstructor(type).invoke()).or(() -> Unsafe.allocateInstance(type));
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
		return root == null ? (Constructor) copy.invokeExact(constructor) : copy(root);
	}
}
