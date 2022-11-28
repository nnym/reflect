package net.auoeke.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.auoeke.dycon.Dycon.*;

/**
 @since 0.13.0
 */
public class Methods {
	public static <T extends Executable> T find(long flags, int offset, Stream<T> methods, Object... arguments) {
		return methods.filter(method -> Types.canCast(flags, offset, method.getParameterTypes(), arguments)).findAny().orElse(null);
	}

	public static <T extends Executable> T find(int offset, Stream<T> methods, Class<?>... parameterTypes) {
		return methods.filter(method -> Types.canCast(0L, offset, method.getParameterTypes(), parameterTypes)).findAny().orElse(null);
	}

	public static <T extends Executable> T find(long flags, Stream<T> methods, Object... arguments) {
		return find(flags, 0, methods, arguments);
	}

	public static <T extends Executable> T find(int offset, Stream<T> methods, Object... arguments) {
		return find(Types.DEFAULT_CONVERSION, offset, methods, arguments);
	}

	public static <T extends Executable> T find(Stream<T> methods, Object... arguments) {
		return find(Types.DEFAULT_CONVERSION, 0, methods, arguments);
	}

	public static <T extends Executable> T find(Stream<T> methods, Class<?>... parameterTypes) {
		return find(0, methods, parameterTypes);
	}

	/**
	 Returns a type's declared methods directly without {@link jdk.internal.reflect.Reflection#filterMethods filtering} or caching them or wrapping them in a stream.

	 @param type a type
	 @return the array containing the type's declared methods
	 @since 4.0.0
	 */
	public static Method[] direct(Class<?> type) {
		return (Method[]) ldc(() -> Stream.of(Class.class.getDeclaredMethods())
			.filter(method -> Flags.isNative(method) && method.getReturnType() == Method[].class)
			.map(Invoker::unreflectSpecial)
			.map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method)
			.max(Comparator.comparing(method -> ((Method[]) method.invoke(Reflect.class)).length))
			.get()
		).invoke(type);
	}

	/**
	 Returns a type's declared methods without {@link jdk.internal.reflect.Reflection#filterMethods filtering}.

	 @param type a type
	 @return a stream containing the type's declared methods
	 @since 4.0.0
	 */
	public static Stream<Method> of(Class<?> type) {
		return Stream.of(ldc(CacheMap::<Class<?>, Method[]>identity).computeIfAbsent(type, Methods::direct));
	}

	/**
	 Finds {@code type}'s methods by {@code name} and returns them as a sequential stream.

	 @param type a type
	 @param name a method's name
	 @return the methods found with the given name
	 @since 6.0.0
	 */
	public static Stream<Method> of(Class<?> type, String name) {
		return Stream.of(ldc(CacheMap::<Class<?>, CacheMap<String, Method[]>>identity)
			.computeIfAbsent(type, t -> CacheMap.hash())
			.computeIfAbsent(name, n -> of(type).filter(method -> method.getName().equals(n)).toArray(Method[]::new))
		);
	}

	/**
	 Attempts to find a method in {@code type} by {@code name}.
	 If {@code type::name} does not exist, then {@code null} is returned.

	 @param type the type
	 @param name a method's name
	 @return the first method with the given name if found or else {@code null}
	 @since 6.0.0
	 */
	public static Method firstOf(Class<?> type, String name) {
		return of(type, name).findAny().orElse(null);
	}

	/**
	 Attempts to find {@code type}'s first method by name and parameter types.
	 If it does not exist, then {@code null} is returned.

	 @param type a type
	 @param name the method's name
	 @param parameterTypes the method's parameter types
	 @return the first method with the given name and parameter types if found or else {@code null}
	 @since 4.0.0
	 */
	public static Method of(Class<?> type, String name, Class<?>... parameterTypes) {
		return ldc(CacheMap::<CacheKey, Method>hash).computeIfAbsent(
			new CacheKey(type, name, parameterTypes),
			key -> of(key.owner())
				.filter(method -> method.getName().equals(key.name()) && Arrays.equals(method.getParameterTypes(), key.parameterTypes()))
				.findFirst()
				.orElse(null)
		);
	}

	public static Method any(Class<?> type, String name) {
		return Types.hierarchy(type).flatMap(Methods::of).filter(method -> method.getName().equals(name)).findAny().orElse(null);
	}

	/**
	 Attempts to find {@code type}'s or its supertypes' first method by name and parameter types.
	 If it does not exist, then {@code null} is returned.

	 @param type a type
	 @param name the method's name
	 @param parameterTypes the method's parameter types
	 @return the first method with the given name and parameter types if found or else {@code null}
	 @since 6.0.0
	 */
	public static Method any(Class<?> type, String name, Class<?>... parameterTypes) {
		return Types.hierarchy(type).map(t -> of(t, name, parameterTypes)).filter(Objects::nonNull).findAny().orElse(null);
	}

	/**
	 Copies a method without its {@link AccessibleObject#setAccessible(boolean) accessibility} flag.
	 If the method is {@code null}, then {@code null} is returned.

	 @param method a method
	 @return a copy of {@code method} with its accessibility flag not set
	 @since 5.3.0
	 */
	public static Method copy(Method method) {
		var copy = ldc(() -> Invoker.findSpecial(Method.class, "copy", Method.class));
		var leafCopy = ldc(() -> Invoker.findSpecial(Method.class, "leafCopy", Method.class));

		return method == null ? null
			: (Method) (AccessibleObjects.root(method) == null ? copy : leafCopy).invokeExact(method);
	}

	/**
	 Returns a {@link MethodType} representing {@code method}'s type.

	 @param method a method
	 @return {@code method}'s type as a {@link MethodType}
	 @throws NullPointerException if {@code method} is {@code null}
	 @since 5.3.0
	 */
	public static MethodType type(Method method) {
		return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
	}

	/**
	 Returns an annotation interface's element's default value.

	 @param type the annotation interface
	 @param name the element's name
	 @param <T> the type of the default value
	 @return the default value of {@code type::name}
	 @throws NullPointerException if {@code type} does not have an element with name {@code name}
	 @since 4.0.0
	 */
	public static <T> T defaultValue(Class<? extends Annotation> type, String name) {
		return (T) firstOf(type, name).getDefaultValue();
	}

	/**
	 Determines whether {@code implementation} overrides {@code base}.

	 @param implementation the method to check as the implementation
	 @param base the method to check as the base method
	 @return whether {@code implementation} overrides {@code base}
	 @since 5.3.0
	 */
	public static boolean overrides(Method implementation, Method base) {
		if (Types.isSubtype(implementation.getDeclaringClass(), base.getDeclaringClass()) && implementation.getName().equals(base.getName())) {
			return implementation.getReturnType() == base.getReturnType()
				&& Arrays.equals(implementation.getParameterTypes(), base.getParameterTypes());
			   /* || crossBridge
				&& base.getReturnType().isAssignableFrom(implementation.getReturnType())
				&& Types.canCast(implementation.getParameterTypes(), base.getParameterTypes()); */
		}

		return false;
	}

	/**
	 Transforms a stream of methods to retain only its least derived methods:
	 every method that overrides another method in the stream is excluded from the resulting stream.

	 @param methods a stream of methods
	 @return a stream of {@code methods}' least derived methods
	 @throws NullPointerException if {@code methods} is {@code null}
	 @since 5.3.0
	 */
	public static Stream<Method> filterBase(Stream<Method> methods) {
		return filter(methods, false);
	}

	/**
	 Transforms a stream of methods to retain only its most derived methods:
	 every method for which an overriding method exists in the stream is excluded from the resulting stream.

	 @param methods a stream of methods
	 @return a stream of {@code methods}' most derived methods
	 @throws NullPointerException if {@code methods} is {@code null}
	 @since 5.3.0
	 */
	public static Stream<Method> filterOverriding(Stream<Method> methods) {
		return filter(methods, true);
	}

	/**
	 Finds methods overridden by a type. The methods may be overridden directly or indirectly any number of times.

	 @param implementor a type
	 @return the methods overridden by {@code implementor}
	 @throws NullPointerException if {@code implementor} is {@code null}
	 @since 5.3.0
	 */
	public static Stream<Method> overridden(Class<?> implementor) {
		var methods = of(implementor).filter(Flags::isInstance).map(Descriptor::new).collect(Collectors.toSet());
		return Types.baseTypes(implementor)
			.flatMap(Methods::of)
			.filter(base -> Flags.isInstance(base) && methods.contains(new Descriptor(base)));
	}

	/**
	 Finds the least derived single abstract method implemented by a type.
	 This method does not return methods that override.
	 For example, given an {@code interface R extends Runnable} that overrides {@link Runnable#run run}
	 and a lambda {@code l} of type {@code R}, {@code R::run} will be ignored and {@code sam(l.getClass())} will match {@link Runnable#run}.
	 <p>
	 If the number of abstract methods is 0 or greater than 1, then {@code null} is returned.

	 @param implementor a type
	 @return the single abstract method implemented by {@code implementor} if it exists or else {@code null}
	 @throws NullPointerException if {@code implementor} is {@code null}
	 @since 5.3.0
	 */
	public static Method sam(Class<?> implementor) {
		var ams = filterBase(overridden(implementor).filter(Flags::isAbstract)).toArray();
		return ams.length == 1 ? (Method) ams[0] : null;
	}

	private static Stream<Method> filter(Stream<Method> methods, boolean override) {
		var types = new HashMap<Descriptor, List<Method>>();

		methods.forEach(method -> {
			var methodList = types.computeIfAbsent(new Descriptor(method), descriptor -> new ArrayList<>());

			for (var iterator = methodList.listIterator(); iterator.hasNext(); ) {
				var existingMethod = iterator.next();

				if (override ? Types.isSubtype(method.getDeclaringClass(), existingMethod.getDeclaringClass()) : Types.isSubtype(existingMethod.getDeclaringClass(), method.getDeclaringClass())) {
					iterator.set(method);
					return;
				} else if (override ? method.getDeclaringClass().isAssignableFrom(existingMethod.getDeclaringClass()) : existingMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
					return;
				}
			}

			methodList.add(method);
		});

		return types.values().stream().flatMap(List::stream);
	}

	private record CacheKey(Class<?> owner, String name, Class<?>[] parameterTypes) {
		@Override public boolean equals(Object object) {
			return object instanceof CacheKey key && this.owner == key.owner && this.name.equals(key.name) && Arrays.equals(this.parameterTypes, key.parameterTypes);
		}

		@Override public int hashCode() {
			return this.owner.hashCode() ^ this.name.hashCode() ^ Arrays.hashCode(this.parameterTypes);
		}
	}

	private record Descriptor(Class<?> returnType, String name, Class<?>[] parameterTypes) {
		private Descriptor(Method method) {
			this(method.getReturnType(), method.getName(), method.getParameterTypes());
		}

		@Override public boolean equals(Object object) {
			return object instanceof Descriptor descriptor && this.returnType == descriptor.returnType && this.name.equals(descriptor.name) && Arrays.equals(this.parameterTypes, descriptor.parameterTypes);
		}

		@Override public int hashCode() {
			return this.returnType.hashCode() ^ this.name.hashCode() ^ Arrays.hashCode(this.parameterTypes);
		}
	}
}
