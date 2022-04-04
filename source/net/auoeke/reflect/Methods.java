package net.auoeke.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
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
    private static final CacheMap<MethodKey, Method> methodsBySignature = CacheMap.hash();

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
        return (Method[]) getDeclaredMethods.invokeExact(type);
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
            new MethodKey(type, name, parameterTypes),
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
     Get an annotation interface's element's default value.

     @param type the annotation interface
     @param name the element's name
     @param <T> the type of the default value
     @return the default value of {@code type::name}
     */
    public static <T> T defaultValue(Class<? extends Annotation> type, String name) {
        return (T) of(type, name).getDefaultValue();
    }

    private record MethodKey(Class<?> owner, String name, Class<?>[] parameterTypes) {
        @Override public boolean equals(Object object) {
            return object instanceof MethodKey key && this.owner == key.owner && this.name.equals(key.name) && Arrays.equals(this.parameterTypes, key.parameterTypes);
        }

        @Override public int hashCode() {
            return this.owner.hashCode() ^ this.name.hashCode() ^ Arrays.hashCode(this.parameterTypes);
        }
    }
}
