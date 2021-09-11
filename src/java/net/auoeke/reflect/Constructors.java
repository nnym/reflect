package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import net.gudenau.lib.unsafe.Unsafe;

// todo: caching?
public class Constructors {
    private static final MethodHandle getDeclaredConstructors;

    public static <T> T instantiate(Class<T> klass) {
        Constructor<T> constructor = constructor(klass);

        return constructor == null ? Unsafe.allocateInstance(klass) : Invoker.invoke(Invoker.unreflectConstructor(constructor));

    }

    public static <T> T construct(Class<T> klass, Object... arguments) {
        try {
            return (T) Invoker.unreflectConstructor(constructor(klass, arguments)).invokeWithArguments(arguments);
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    public static <T> Constructor<T> constructor(Class<T> klass, Object... arguments) {
        return constructor(true, klass, arguments);
    }

    public static <T> Constructor<T> constructor(Class<T> klass, Class<?>... parameterTypes) {
        for (Constructor<T> constructor : constructors(klass)) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }

        return Reflect.nul();
    }

    public static <T> Constructor<T> constructor(boolean unbox, Class<T> klass, Object... arguments) {
        return Methods.find(unbox, constructors(klass), arguments);
    }

    public static <T> Constructor<T>[] constructors(Class<T> klass) {
        try {
            return (Constructor<T>[]) getDeclaredConstructors.invokeExact(klass);
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    static {
        MethodHandle handle = null;

        for (Method method : Methods.getRawMethods(Class.class)) {
            if ((method.getModifiers() & Modifier.NATIVE) != 0 && method.getReturnType() == Constructor[].class) {
                handle = Invoker.unreflectSpecial(method, Class.class);

                if (method.getParameterCount() > 0) {
                    handle = MethodHandles.insertArguments(handle, 1, false);
                }

                break;
            }
        }

        getDeclaredConstructors = handle;
    }
}
