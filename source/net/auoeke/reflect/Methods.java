package net.auoeke.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.SneakyThrows;

public class Methods {
    private static final MethodHandle getDeclaredMethods = Stream.of(Class.class.getDeclaredMethods())
        .filter(method -> Flags.isNative(method) && method.getReturnType() == Method[].class)
        .findAny()
        .map(Invoker::unreflectSpecial)
        .map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method)
        .get();

    private static final CacheMap<Class<?>, Method[]> methods = CacheMap.identity();
    // Todo: cache methods by name?
    // private static final CacheMap<Class<?>, CacheMap<String, Method[]>> methodsByName = CacheMap.identity();

    public static <T extends Executable> T find(long flags, int offset, Stream<T> methods, Object... arguments) {
        return methods.filter(method -> Types.canCast(flags, offset, method, arguments)).findAny().orElse(null);
    }

    public static <T extends Executable> T find(int offset, Stream<T> methods, Class<?>... parameterTypes) {
        return methods.filter(method -> Types.canCast(0L, offset, method, parameterTypes)).findAny().orElse(null);
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
     Get a type's declared methods directly without {@linkplain jdk.internal.reflect.Reflection#filterMethods filtering} or caching them or wrapping them in a stream.

     @param type a type
     @return the array containing the type's declared methods
     */
    @SneakyThrows
    public static Method[] direct(Class<?> type) {
        return (Method[]) getDeclaredMethods.invokeExact(type);
    }

    /**
     Get a type's declared methods without {@linkplain jdk.internal.reflect.Reflection#filterMethods filtering}.

     @param type a type
     @return a stream containing the type's declared methods
     */
    public static Stream<Method> of(Class<?> type) {
        return Stream.of(methods.computeIfAbsent(type, Methods::direct));
    }

    public static Method of(Class<?> type, String name) {
        return of(type).filter(method -> method.getName().equals(name)).findAny().orElse(null);
    }

    public static Method of(Class<?> type, String name, Class<?>... parameterTypes) {
        return of(type).filter(method -> {
            if (method.getName().equals(name)) {
                var actualTypes = method.getParameterTypes();
                return actualTypes.length == parameterTypes.length && Arrays.equals(actualTypes, parameterTypes);
            }

            return false;
        }).findAny().orElse(null);
    }

    /**
     Get all methods declared by all classes in a hierarchy starting at a given class and ending at one of its superclasses.

     @param start the starting class
     @param end the superclass at which to stop; may be null (exclusive)
     @return all methods is the hierarchy
     */
    public static Stream<Method> all(Class<?> start, Class<?> end) {
        return Types.classes(start, end).flatMap(Methods::of);
    }

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

    public static <T> T defaultValue(Class<? extends Annotation> type, String name) {
        return (T) of(type, name).getDefaultValue();
    }
}
