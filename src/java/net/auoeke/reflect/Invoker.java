package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import net.gudenau.lib.unsafe.Unsafe;

import static net.gudenau.lib.unsafe.Unsafe.trustedLookup;

public class Invoker {
    public static Member member(MethodHandle handle) {
        return MethodHandles.reflectAs(Member.class, handle);
    }

    public static Field field(MethodHandle handle) {
        return MethodHandles.reflectAs(Field.class, handle);
    }

    public static Executable executable(MethodHandle handle) {
        return MethodHandles.reflectAs(Executable.class, handle);
    }

    public static <T> Constructor<T> constructor(MethodHandle handle) {
        return MethodHandles.reflectAs(Constructor.class, handle);
    }

    public static Method method(MethodHandle handle) {
        return MethodHandles.reflectAs(Method.class, handle);
    }

    public static <T> T invoke(MethodHandle handle) {
        try {
            return (T) handle.invoke();
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    public static <T> T invoke(MethodHandle handle, Object... arguments) {
        try {
            return (T) handle.invokeWithArguments(arguments);
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    public static MethodHandle bind(Object receiver, String name, MethodType type) {
        try {
            return trustedLookup.bind(receiver, name, type);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType) {
        try {
            return trustedLookup.bind(receiver, name, MethodType.methodType(returnType));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType, Class<?>... parameterTypes) {
        try {
            return trustedLookup.bind(receiver, name, MethodType.methodType(returnType, parameterTypes));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findConstructor(Class<?> refc, MethodType type) {
        try {
            return trustedLookup.findConstructor(refc, type);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findConstructor(Class<?> refc) {
        try {
            return trustedLookup.findConstructor(refc, MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findConstructor(Class<?> refc, Class<?>... parameterTypes) {
        try {
            return trustedLookup.findConstructor(refc, MethodType.methodType(void.class, parameterTypes));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
        try {
            return trustedLookup.findGetter(refc, name, type);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
        try {
            return trustedLookup.findSetter(refc, name, type);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller) {
        try {
            return trustedLookup.findSpecial(refc, name, type, specialCaller);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }


    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType) {
        try {
            return trustedLookup.findSpecial(refc, name, MethodType.methodType(returnType), refc);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        try {
            return trustedLookup.findSpecial(refc, name, MethodType.methodType(returnType), refc);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findSpecial(Class<?> specialCaller, Class<?> refc, String name, Class<?> returnType) {
        try {
            return trustedLookup.findSpecial(refc, name, MethodType.methodType(returnType), specialCaller);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findSpecial(Class<?> specialCaller, Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        try {
            return trustedLookup.findSpecial(refc, name, MethodType.methodType(returnType, parameterTypes), specialCaller);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findStatic(Class<?> refc, String name, MethodType type)  {
        try {
            return trustedLookup.findStatic(refc, name, type);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType)  {
        try {
            return trustedLookup.findStatic(refc, name, MethodType.methodType(returnType));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes)  {
        try {
            return trustedLookup.findStatic(refc, name, MethodType.methodType(returnType, parameterTypes));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
        try {
            return trustedLookup.findStaticGetter(refc, name, type);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
        try {
            return trustedLookup.findStaticSetter(refc, name, type);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findVirtual(Class<?> refc, String name, MethodType type) {
        try {
            return trustedLookup.findVirtual(refc, name, type);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findVirtual(Class<?> refc, String name, Class<?> returnType) {
        try {
            return trustedLookup.findVirtual(refc, name, MethodType.methodType(returnType));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle findVirtual(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        try {
            return trustedLookup.findVirtual(refc, name, MethodType.methodType(returnType, parameterTypes));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflect(Method method) {
        try {
            return trustedLookup.unreflect(method);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflect(Class<?> klass, String name, Class<?>... parameterTypes) {
        try {
            return trustedLookup.unreflect(Methods.getMethod(klass, name, parameterTypes));
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflect(Class<?> klass, String name) {
        try {
            return trustedLookup.unreflect(Methods.getMethod(klass, name));
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflect(Object object, String name, Class<?>... parameterTypes) {
        try {
            return trustedLookup.unreflect(Methods.getMethod(object, name, parameterTypes)).bindTo(object);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflect(Object object, String name) {
        try {
            return trustedLookup.unreflect(Methods.getMethod(object, name)).bindTo(object);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectConstructor(Constructor<?> constructor) {
        try {
            return trustedLookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectConstructor(Class<?> klass, Class<?>... parameterTypes) {
        try {
            return trustedLookup.unreflectConstructor(Constructors.constructor(klass, parameterTypes));
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectSpecial(Method method, Class<?> specialCaller) {
        try {
            return trustedLookup.unreflectSpecial(method, specialCaller);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectSpecial(Class<?> klass, String name, Class<?>... parameterTypes) {
        try {
            return trustedLookup.unreflectSpecial(Methods.getMethod(klass, name, parameterTypes), klass);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectSpecial(Class<?> specialCaller, Class<?> klass, String name, Class<?>... parameterTypes) {
        try {
            return trustedLookup.unreflectSpecial(Methods.getMethod(klass, name, parameterTypes), specialCaller);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectGetter(Field field) {
        try {
            return trustedLookup.unreflectGetter(field);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectGetter(Class<?> klass, String name) {
        try {
            return trustedLookup.unreflectGetter(Fields.field(klass, name));
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectSetter(Field field) {
        try {
            return trustedLookup.unreflectSetter(field);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static MethodHandle unreflectSetter(Class<?> klass, String name) {
        try {
            return trustedLookup.unreflectSetter(Fields.field(klass, name));
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static VarHandle findStaticVarHandle(Class<?> owner, String name, Class<?> type) {
        try {
            return trustedLookup.findStaticVarHandle(owner, name, type);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static VarHandle findVarHandle(Class<?> owner, String name, Class<?> type) {
        try {
            return trustedLookup.findVarHandle(owner, name, type);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }

    public static VarHandle unreflectVarHandle(Field field) {
        try {
            return trustedLookup.unreflectVarHandle(field);
        } catch (IllegalAccessException exception) {
            throw Unsafe.throwException(exception);
        }
    }
}
