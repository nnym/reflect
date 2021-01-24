package user11681.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import net.gudenau.lib.unsafe.Unsafe;

public class Methods {
    private static final MethodHandle getDeclaredMethods;

    private static final HashMap<Class<?>, Method[]> methodCache = new HashMap<>();

    private static final Method NOT_FOUND = null;

    public static boolean argumentsMatchParameters(Executable executable, Object... arguments) {
        return argumentsMatchParameters(true, 0, executable, arguments);
    }

    public static boolean argumentsMatchParameters(int offset, Executable executable, Object... arguments) {
        return argumentsMatchParameters(true, offset, executable, arguments);
    }

    public static boolean argumentsMatchParameters(boolean unbox, Executable executable, Object... arguments) {
        return argumentsMatchParameters(unbox, 0, executable, arguments);
    }

    public static boolean argumentsMatchParameters(boolean unbox, int offset, Executable executable, Object... arguments) {
        final Class<?>[] types = executable.getParameterTypes();

        if (types.length == arguments.length + 2) {
            for (int i = 2, length = types.length; i != length; i++) {
                final Class<?> parameterType = types[i];

                if (unbox) {
                    if (!Types.equals(arguments[i - 2].getClass(), parameterType) && arguments[i - 2].getClass() != parameterType) {
                        return false;
                    }
                } else if (arguments[i - 2].getClass() != parameterType) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public static <T> T getDefaultValue(Class<? extends Annotation> annotationType, String elementName) {
        try {
            return (T) annotationType.getDeclaredMethod(elementName).getDefaultValue();
        } catch (NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static Method getMethod(Object object, String name) {
        Class<?> klass = object.getClass();
        Method method;

        while (klass != null) {
            if ((method = getMethod(klass, name)) != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return NOT_FOUND;
    }

    public static Method getMethod(Class<?> klass, String name) {
        final Method[] methods = getMethods(klass);

        for (int i = 0, length = methods.length; i < length; i++) {
            if (methods[i].getName().equals(name)) {
                return methods[i];
            }
        }

        return NOT_FOUND;
    }

    public static Method getMethod(Object object, String name, Class<?>... parameterTypes) {
        Class<?> klass = object.getClass();
        Method method;

        while (klass != null) {
            if ((method = getMethod(klass, name, parameterTypes)) != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return NOT_FOUND;
    }

    public static Method getMethod(Class<?> klass, String name, Class<?>... parameterTypes) {
        final Method[] methods = getMethods(klass);
        Class<?>[] parameterTypes1;
        Method method;
        int parameterCount;
        int j;

        method:
        for (int i = 0, length = methods.length; i < length; i++) {
            if (methods[i].getName().equals(name)) {
                method = methods[i];

                if ((parameterCount = (parameterTypes1 = method.getParameterTypes()).length) == parameterTypes.length) {
                    for (j = 0; j < parameterCount; j++) {
                        if (parameterTypes1[j] != parameterTypes[j]) {
                            continue method;
                        }
                    }

                    return methods[i];
                }
            }
        }

        return NOT_FOUND;
    }

    public static Method getRawMethod(Object object, String name) {
        Class<?> klass = object.getClass();
        Method method;

        while (klass != null) {
            if ((method = getRawMethod(klass, name)) != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return NOT_FOUND;
    }

    public static Method getRawMethod(Class<?> klass, String name) {
        final Method[] methods = getRawMethods(klass);

        for (int i = 0, length = methods.length; i < length; i++) {
            if (methods[i].getName().equals(name)) {
                return methods[i];
            }
        }

        return NOT_FOUND;
    }

    public static Method getRawMethod(Object object, String name, Class<?>... parameterTypes) {
        Class<?> klass = object.getClass();
        Method method;

        while (klass != null) {
            if ((method = getRawMethod(klass, name, parameterTypes)) != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return NOT_FOUND;
    }

    public static Method getRawMethod(Class<?> klass, String name, Class<?>... parameterTypes) {
        final Method[] methods = getRawMethods(klass);
        Class<?>[] parameterTypes1;
        Method method;
        int parameterCount;
        int j;

        method:
        for (int i = 0, length = methods.length; i < length; i++) {
            if (methods[i].getName().equals(name)) {
                method = methods[i];

                if ((parameterCount = (parameterTypes1 = method.getParameterTypes()).length) == parameterTypes.length) {
                    for (j = 0; j < parameterCount; j++) {
                        if (parameterTypes1[j] != parameterTypes[j]) {
                            continue method;
                        }
                    }

                    return methods[i];
                }
            }
        }

        return NOT_FOUND;
    }

    public static Method[] getMethods(Class<?> klass) {
        Method[] methods = methodCache.get(klass);

        if (methods == null) {
            methods = getRawMethods(klass);
            methodCache.put(klass, methods);

            for (Method method : methods) {
                Unsafe.putBoolean(method, Fields.overrideOffset, true);
            }
        }

        return methods;
    }

    public static Method[] getRawMethods(Class<?> klass) {
        try {
            return (Method[]) getDeclaredMethods.invokeExact(klass);
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    static {
        try {
            MethodHandle tempGetDeclaredMethods = null;

            for (Method method : Class.class.getDeclaredMethods()) {
                if (Modifier.isNative(method.getModifiers()) && method.getReturnType() == Method[].class) {
                    tempGetDeclaredMethods = Unsafe.trustedLookup.unreflect(method);

                    if (method.getParameterCount() > 0) {
                        tempGetDeclaredMethods = MethodHandles.insertArguments(tempGetDeclaredMethods, 1, false);
                    }

                    break;
                }
            }

            getDeclaredMethods = tempGetDeclaredMethods;
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }
}
