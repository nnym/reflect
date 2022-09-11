package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

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
     Instantiate {@code type} through a default constructor if it is available or fall back to {@link Unsafe#allocateInstance(Class)}.

     @return the new instance; never {@code null}
     @throws InstantiationException if {@code type} is abstract
     */
    public static <T> T instantiate(Class<T> type) {
        var constructor = Reflect.runNull(() -> Invoker.findConstructor(type));
        return constructor == null ? Unsafe.allocateInstance(type) : (T) constructor.invoke();
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
}