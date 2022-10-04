package net.auoeke.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 @since 0.13.0
 */
public class Methods {
    private static final MethodHandle getDeclaredMethods = Stream.of(Class.class.getDeclaredMethods())
        .filter(method -> Flags.isNative(method) && method.getReturnType() == Method[].class)
        .map(Invoker::unreflectSpecial)
        .map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method)
        .max(Comparator.comparing(method -> ((Method[]) method.invoke(Reflect.class)).length))
        .get();

    private static final CacheMap<Class<?>, Method[]> methods = CacheMap.identity();
    private static final CacheMap<Class<?>, CacheMap<String, Method[]>> methodsByName = CacheMap.identity();
    private static final CacheMap<CacheKey, Method> methodsBySignature = CacheMap.hash();

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
     Get a type's declared methods directly without {@link jdk.internal.reflect.Reflection#filterMethods filtering} or caching them or wrapping them in a stream.

     @param type a type
     @return the array containing the type's declared methods
     */
    public static Method[] direct(Class<?> type) {
        return (Method[]) getDeclaredMethods.invoke(type);
    }

    /**
     Get a type's declared methods without {@link jdk.internal.reflect.Reflection#filterMethods filtering}.

     @param type a type
     @return a stream containing the type's declared methods
     */
    public static Stream<Method> of(Class<?> type) {
        return Stream.of(methods.computeIfAbsent(type, Methods::direct));
    }

    /**
     Attempt to find a type's first method with a given name.

     @param type the type
     @param name the method's name
     @return the first method found with the given name; {@code null} if not found
     */
    public static Method of(Class<?> type, String name) {
        var methods = methodsByName.computeIfAbsent(type, t -> CacheMap.hash()).computeIfAbsent(name, n -> of(type).filter(method -> method.getName().equals(n)).toArray(Method[]::new));
        return methods.length > 0 ? methods[0] : null;
    }

    /**
     Attempt to find a type's first method with given name and parameter types.

     @param type the type
     @param name the method's name
     @param parameterTypes the method's parameter types
     @return the first method found with the given name and parameter types; {@code null} if not found
     */
    public static Method of(Class<?> type, String name, Class<?>... parameterTypes) {
        return methodsBySignature.computeIfAbsent(
            new CacheKey(type, name, parameterTypes),
            key -> of(key.owner()).filter(method -> method.getName().equals(key.name()) && Arrays.equals(method.getParameterTypes(), key.parameterTypes())).findFirst().orElse(null)
        );
    }

    /**
     Get all methods declared by all classes in a hierarchy starting at a given class and ending at one of its base classes.

     @param start the starting class
     @param end the superclass at which to stop; may be null (exclusive)
     @return all methods is the hierarchy
     */
    public static Stream<Method> all(Class<?> start, Class<?> end) {
        return Types.classes(start, end).flatMap(Methods::of);
    }

    /**
     Get all methods declared by a type or any of its base classes.

     @param type the type
     @return all methods belonging to the type or any of its base classes
     */
    public static Stream<Method> all(Class<?> type) {
        return all(type, null);
    }

    public static Stream<Method> all(Object object, Class<?> end) {
        return all(object.getClass(), end);
    }

    public static Stream<Method> all(Object object) {
        return all(object.getClass(), null);
    }

    public static Method any(Class<?> type, String name) {
        return all(type, null).filter(method -> method.getName().equals(name)).findAny().orElse(null);
    }

    public static Method any(Object object, String name) {
        return any(object.getClass(), name);
    }

    public static Method any(Object object, String name, Class<?>... parameterTypes) {
        return Types.classes(object.getClass()).map(type -> of(type, name, parameterTypes)).filter(Objects::nonNull).findAny().orElse(null);
    }

	/**
	 Returns a {@link MethodType} representing {@code method}'s type.

	 @param method a method
	 @return {@code method}'s type as a {@link MethodType}
	 @since 5.3.0
	 */
	public static MethodType type(Method method) {
		return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
	}

    /**
     Get an annotation interface's element's default value.

     @param type the annotation interface
     @param name the element's name
     @param <T> the type of the default value
     @return the default value of {@code type::name}
     */
    public static <T> T defaultValue(Class<? extends Annotation> type, String name) {
        return (T) of(type, name).getDefaultValue();
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

			for (var iterator = methodList.listIterator(); iterator.hasNext();) {
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
