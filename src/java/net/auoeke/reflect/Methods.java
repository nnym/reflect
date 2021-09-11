package net.auoeke.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.gudenau.lib.unsafe.Unsafe;

public class Methods {
    private static final MethodHandle getDeclaredMethods;

    private static final HashMap<Class<?>, Method[]> methodCache = new HashMap<>();

    public static <T extends Executable> T find(T[] methods, Class<?>... parameterTypes) {
        for (T method : methods) {
            if (Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }

        return Reflect.nul();
    }

    public static <T extends Executable> T find(T[] methods, Object... arguments) {
        return find(true, 0, methods, arguments);
    }

    public static <T extends Executable> T find(int offset, T[] methods, Object... arguments) {
        return find(true, offset, methods, arguments);
    }

    public static <T extends Executable> T find(boolean unbox, T[] methods, Object... arguments) {
        return find(unbox, 0, methods, arguments);
    }

    public static <T extends Executable> T find(boolean unbox, int offset, T[] methods, Object... arguments) {
        for (T method : methods) {
            if (Methods.argumentsMatchParameters(unbox, offset, method, arguments)) {
                return method;
            }
        }

        return Reflect.nul();
    }

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
        Class<?>[] types = executable.getParameterTypes();

        if (types.length == arguments.length + offset) {
            for (int i = offset, length = types.length; i != length; i++) {
                Class<?> parameterType = types[i];

                if (unbox) {
                    if (!Types.equals(arguments[i - offset].getClass(), parameterType) && arguments[i - offset].getClass() != parameterType) {
                        return false;
                    }
                } else if (arguments[i - offset].getClass() != parameterType) {
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

        while (klass != null) {
            var method = getMethod(klass, name);

            if (method != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return Reflect.nul();
    }

    public static Method getMethod(Class<?> klass, String name) {
        for (var method : getMethods(klass)) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        return Reflect.nul();
    }

    public static Method getMethod(Object object, String name, Class<?>... parameterTypes) {
        Class<?> klass = object.getClass();

        while (klass != null) {
            var method = getMethod(klass, name, parameterTypes);

            if (method != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return Reflect.nul();
    }

    public static Method getMethod(Class<?> klass, String name, Class<?>... parameterTypes) {
        Method[] methods = getMethods(klass);
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

        return Reflect.nul();
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

        return Reflect.nul();
    }

    public static Method getRawMethod(Class<?> klass, String name) {
        Method[] methods = getRawMethods(klass);

        for (int i = 0, length = methods.length; i < length; i++) {
            if (methods[i].getName().equals(name)) {
                return methods[i];
            }
        }

        return Reflect.nul();
    }

    public static Method getRawMethod(Object object, String name, Class<?>... parameterTypes) {
        Class<?> klass = object.getClass();

        while (klass != null) {
            var method = getRawMethod(klass, name, parameterTypes);

            if (method != null) {
                return method;
            }

            klass = klass.getSuperclass();
        }

        return Reflect.nul();
    }

    public static Method getRawMethod(Class<?> klass, String name, Class<?>... parameterTypes) {
        method:
        for (var method : getRawMethods(klass)) {
            if (method.getName().equals(name)) {
                var actualParameterTypes = method.getParameterTypes();

                if (actualParameterTypes.length == parameterTypes.length) {
                    for (int index = 0; index < actualParameterTypes.length; index++) {
                        if (actualParameterTypes[index] != parameterTypes[index]) {
                            continue method;
                        }
                    }

                    return method;
                }
            }
        }

        return Reflect.nul();
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

    public static List<Method> all(Class<?> type) {
        if (type == null) {
            return null;
        }

        var methods = new ArrayList<Method>();

        do {
            Collections.addAll(methods, getMethods(type));
            type = type.getSuperclass();
        } while (type != null);

        return methods;
    }

    static {
        try {
            MethodHandle tempGetDeclaredMethods = null;

            for (Method method : Class.class.getDeclaredMethods()) {
                if ((method.getModifiers() & Modifier.NATIVE) != 0 && method.getReturnType() == Method[].class) {
                    tempGetDeclaredMethods = Invoker.unreflectSpecial(method, Class.class);

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
