package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import net.gudenau.lib.unsafe.Unsafe;

public class Constructors {
    private static final MethodHandle getDeclaredConstructors = Methods.of(Class.class)
        .filter(method -> Flags.isNative(method) && method.getReturnType() == Constructor[].class).findAny()
        .map(Invoker::unreflectSpecial)
        .map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method).get();

    private static final CacheMap<Class<?>, Constructor<?>[]> constructors = CacheMap.identity();

    /**
     Get a type's declared constructors directly without caching them or wrapping them in a stream.

     @param type a type
     @return the array containing the type's declared methods
     */
    @SneakyThrows
    public static <T> Constructor<T>[] direct(Class<T> type) {
        return (Constructor<T>[]) getDeclaredConstructors.invokeExact(type);
    }

    /**
     Get a type's declared constructors.

     @param type a type
     @return a stream containing the type's declared methods
     */
    public static <T> Stream<Constructor<T>> of(Class<T> type) {
        return Stream.of((Constructor<T>[]) constructors.computeIfAbsent(type, Constructors::direct));
    }

    /**
     Instantiate {@code type} through a constructor if it is available or fall back to {@link Unsafe#allocateInstance(Class)}.

     @return the new instance; never {@code null}
     @throws InstantiationException if {@code type} is abstract
     */
    public static <T> T instantiate(Class<T> type) {
        return Optional.ofNullable(Invoker.findConstructor(type)).map(Invoker::<T>invoke).orElseGet(() -> Unsafe.allocateInstance(type));
    }

    @SneakyThrows
    public static <T> T construct(Class<T> type, Object... arguments) {
        return (T) Invoker.unreflectConstructor(find(type, arguments)).invokeWithArguments(arguments);
    }

    public static <T> Constructor<T> find(long flags, int offset, Class<T> type, Object... arguments) {
        return Methods.find(flags, offset, of(type), arguments);
    }

    public static <T> Constructor<T> find(long flags, Class<T> klass, Object... arguments) {
        return Methods.find(flags, of(klass), arguments);
    }

    public static <T> Constructor<T> find(int offset, Class<T> klass, Object... arguments) {
        return Methods.find(offset, of(klass), arguments);
    }

    public static <T> Constructor<T> find(int offset, Class<T> type, Class<?>... parameterTypes) {
        return Methods.find(offset, of(type), parameterTypes);
    }

    public static <T> Constructor<T> find(Class<T> klass, Object... arguments) {
        return find(Types.DEFAULT_CONVERSION, klass, arguments);
    }

    public static <T> Constructor<T> find(Class<T> type, Class<?>... parameterTypes) {
        return Methods.find(of(type), parameterTypes);
    }
}
