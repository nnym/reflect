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

import static net.auoeke.reflect.Reflect.run;
import static net.gudenau.lib.unsafe.Unsafe.trustedLookup;

@SuppressWarnings("unused")
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
        return run(() -> (T) handle.invoke());
    }

    public static <T> T invoke(MethodHandle handle, Object... arguments) {
        return run(() -> (T) handle.invokeWithArguments(arguments));
    }

    public static MethodHandle bind(Object receiver, String name, MethodType type) {
        return run(() -> trustedLookup.bind(receiver, name, type));
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType) {
        return bind(receiver, name, MethodType.methodType(returnType));
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return bind(receiver, name, MethodType.methodType(returnType, parameterTypes));
    }

    public static MethodHandle findConstructor(Class<?> refc, MethodType type) {
        return run(() -> trustedLookup.findConstructor(refc, type));
    }

    public static MethodHandle findConstructor(Class<?> refc) {
        return findConstructor(refc, MethodType.methodType(void.class));
    }

    public static MethodHandle findConstructor(Class<?> refc, Class<?>... parameterTypes) {
        return findConstructor(refc, MethodType.methodType(void.class, parameterTypes));
    }

    public static MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
        return run(() -> trustedLookup.findGetter(refc, name, type));
    }

    public static MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
        return run(() -> trustedLookup.findSetter(refc, name, type));
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, MethodType type) {
        return run(() -> trustedLookup.findSpecial(refc, name, type, refc));
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType) {
        return findSpecial(refc, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return findSpecial(refc, name, MethodType.methodType(returnType, parameterTypes));
    }

    public static MethodHandle findStatic(Class<?> refc, String name, MethodType type)  {
        return run(() -> trustedLookup.findStatic(refc, name, type));
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType)  {
        return findStatic(refc, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes)  {
        return findStatic(refc, name, MethodType.methodType(returnType, parameterTypes));
    }

    public static MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
        return run(() -> trustedLookup.findStaticGetter(refc, name, type));
    }

    public static MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
        return run(() -> trustedLookup.findStaticSetter(refc, name, type));
    }

    public static MethodHandle findVirtual(Class<?> refc, String name, MethodType type) {
        return run(() -> trustedLookup.findVirtual(refc, name, type));
    }

    public static MethodHandle findVirtual(Class<?> type, String name, Class<?> returnType) {
        return findVirtual(type, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findVirtual(Class<?> type, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return findVirtual(type, name, MethodType.methodType(returnType, parameterTypes));
    }

    public static MethodHandle unreflect(Method method) {
        return run(() -> trustedLookup.unreflect(method));
    }

    public static MethodHandle unreflect(Class<?> type, String name, Class<?>... parameterTypes) {
        return unreflect(Methods.get(type, name, parameterTypes));
    }

    public static MethodHandle unreflect(Class<?> type, String name) {
        return unreflect(Methods.get(type, name));
    }

    public static MethodHandle unreflect(Object object, String name, Class<?>... parameterTypes) {
        return unreflect(Methods.any(object, name, parameterTypes)).bindTo(object);
    }

    public static MethodHandle unreflect(Object object, String name) {
        return unreflect(Methods.any(object, name)).bindTo(object);
    }

    public static MethodHandle unreflectConstructor(Constructor<?> constructor) {
        return run(() -> trustedLookup.unreflectConstructor(constructor));
    }

    public static MethodHandle unreflectConstructor(Class<?> klass, Class<?>... parameterTypes) {
        return unreflectConstructor(Constructors.find(klass, parameterTypes));
    }

    public static MethodHandle unreflectSpecial(Method method) {
        return run(() -> trustedLookup.unreflectSpecial(method, method.getDeclaringClass()));
    }

    public static MethodHandle unreflectSpecial(Class<?> type, String name, Class<?>... parameterTypes) {
        return unreflectSpecial(Methods.get(type, name, parameterTypes));
    }

    public static MethodHandle unreflectGetter(Field field) {
        return run(() -> trustedLookup.unreflectGetter(field));
    }

    public static MethodHandle unreflectGetter(Class<?> klass, String name) {
        return unreflectGetter(Fields.of(klass, name));
    }

    public static MethodHandle unreflectSetter(Field field) {
        return run(() -> trustedLookup.unreflectSetter(field));
    }

    public static MethodHandle unreflectSetter(Class<?> klass, String name) {
        return unreflectSetter(Fields.of(klass, name));
    }

    public static VarHandle findStaticVarHandle(Class<?> owner, String name, Class<?> type) {
        return run(() -> trustedLookup.findStaticVarHandle(owner, name, type));
    }

    public static VarHandle findVarHandle(Class<?> owner, String name, Class<?> type) {
        return run(() -> trustedLookup.findVarHandle(owner, name, type));
    }

    public static VarHandle unreflectVarHandle(Field field) {
        return run(() -> trustedLookup.unreflectVarHandle(field));
    }

    public static VarHandle unreflectVarHandle(Class<?> type, String name) {
        return unreflectVarHandle(Fields.of(type, name));
    }
}
