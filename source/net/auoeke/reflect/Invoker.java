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
import java.util.ArrayList;
import lombok.SneakyThrows;
import lombok.val;

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

    @SneakyThrows
    public static <T> T invoke(MethodHandle handle) {
        return (T) handle.invoke();
    }

    @SneakyThrows
    public static <T> T invoke(MethodHandle handle, Object... arguments) {
        return (T) handle.invokeWithArguments(arguments);
    }

    @SneakyThrows
    public static MethodHandle bind(Object receiver, String name, MethodType type) {
        return trustedLookup.bind(receiver, name, type);
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType) {
        return bind(receiver, name, MethodType.methodType(returnType));
    }

    public static MethodHandle bind(Object receiver, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return bind(receiver, name, MethodType.methodType(returnType, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle findConstructor(Class<?> refc, MethodType type) {
        return trustedLookup.findConstructor(refc, type);
    }

    public static MethodHandle findConstructor(Class<?> refc) {
        return findConstructor(refc, MethodType.methodType(void.class));
    }

    public static MethodHandle findConstructor(Class<?> refc, Class<?>... parameterTypes) {
        return findConstructor(refc, MethodType.methodType(void.class, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
        return trustedLookup.findGetter(refc, name, type);
    }

    @SneakyThrows
    public static MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
        return trustedLookup.findSetter(refc, name, type);
    }

    @SneakyThrows
    public static MethodHandle findSpecial(Class<?> refc, String name, MethodType type) {
        return trustedLookup.findSpecial(refc, name, type, refc);
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType) {
        return findSpecial(refc, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findSpecial(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return findSpecial(refc, name, MethodType.methodType(returnType, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle findStatic(Class<?> refc, String name, MethodType type)  {
        return trustedLookup.findStatic(refc, name, type);
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType)  {
        return findStatic(refc, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes)  {
        return findStatic(refc, name, MethodType.methodType(returnType, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) {
        return trustedLookup.findStaticGetter(refc, name, type);
    }

    @SneakyThrows
    public static MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) {
        return trustedLookup.findStaticSetter(refc, name, type);
    }

    @SneakyThrows
    public static MethodHandle findVirtual(Class<?> refc, String name, MethodType type) {
        return trustedLookup.findVirtual(refc, name, type);
    }

    public static MethodHandle findVirtual(Class<?> type, String name, Class<?> returnType) {
        return findVirtual(type, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findVirtual(Class<?> type, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return findVirtual(type, name, MethodType.methodType(returnType, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle unreflect(Method method) {
        return trustedLookup.unreflect(method);
    }

    public static MethodHandle unreflect(Class<?> type, String name, Class<?>... parameterTypes) {
        return unreflect(Methods.of(type, name, parameterTypes));
    }

    public static MethodHandle unreflect(Class<?> type, String name) {
        return unreflect(Methods.of(type, name));
    }

    public static MethodHandle unreflect(Object object, String name, Class<?>... parameterTypes) {
        return unreflect(Methods.any(object, name, parameterTypes)).bindTo(object);
    }

    public static MethodHandle unreflect(Object object, String name) {
        return unreflect(Methods.any(object, name)).bindTo(object);
    }

    @SneakyThrows
    public static MethodHandle unreflectConstructor(Constructor<?> constructor) {
        return trustedLookup.unreflectConstructor(constructor);
    }

    public static MethodHandle unreflectConstructor(Class<?> klass, Class<?>... parameterTypes) {
        return unreflectConstructor(Constructors.find(klass, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle unreflectSpecial(Method method) {
        return trustedLookup.unreflectSpecial(method, method.getDeclaringClass());
    }

    public static MethodHandle unreflectSpecial(Class<?> type, String name, Class<?>... parameterTypes) {
        return unreflectSpecial(Methods.of(type, name, parameterTypes));
    }

    @SneakyThrows
    public static MethodHandle unreflectGetter(Field field) {
        return trustedLookup.unreflectGetter(field);
    }

    public static MethodHandle unreflectGetter(Class<?> klass, String name) {
        return unreflectGetter(Fields.of(klass, name));
    }

    @SneakyThrows
    public static MethodHandle unreflectSetter(Field field) {
        return trustedLookup.unreflectSetter(field);
    }

    public static MethodHandle unreflectSetter(Class<?> klass, String name) {
        return unreflectSetter(Fields.of(klass, name));
    }

    @SneakyThrows
    public static VarHandle findStaticVarHandle(Class<?> owner, String name, Class<?> type) {
        return trustedLookup.findStaticVarHandle(owner, name, type);
    }

    @SneakyThrows
    public static VarHandle findVarHandle(Class<?> owner, String name, Class<?> type) {
        return trustedLookup.findVarHandle(owner, name, type);
    }

    @SneakyThrows
    public static VarHandle unreflectVarHandle(Field field) {
        return trustedLookup.unreflectVarHandle(field);
    }

    public static VarHandle unreflectVarHandle(Class<?> type, String name) {
        return unreflectVarHandle(Fields.of(type, name));
    }

    /**
     Produce a method handle of type {@code type} that invokes {@code handle} by reordering the resulting handle's parameters to match {@code handle}'s parameters by type.
     <p>
     If {@code handle.type()} and {@code type} are equal, then return {@code handle}.

     @param handle the method handle
     @param type the method type whereto to adapt {@code handle}
     @return the adapted handle
     */
    public static MethodHandle adapt(MethodHandle handle, MethodType type) {
        val order = new int[handle.type().parameterCount()];
        val outputTypes = new ArrayList<>(handle.type().parameterList());
        val inputTypes = type.parameterArray();

        target: for (var inputIndex = 0; inputIndex < inputTypes.length; inputIndex++) {
            val inputType = inputTypes[inputIndex];
            val outputIterator = outputTypes.listIterator();

            while (outputIterator.hasNext()) {
                val outputType = outputIterator.next();

                if (outputType != null && outputType.isAssignableFrom(inputType)) {
                    order[outputIterator.previousIndex()] = inputIndex;
                    outputIterator.set(null);

                    continue target;
                }
            }

            throw new IllegalArgumentException("No matching parameter was found for input %s parameter at index %d.".formatted(inputType, inputIndex));
        }

        for (var index = 0; index < order.length; index++) {
            if (order[index] != index) {
                return MethodHandles.permuteArguments(handle, type, order);
            }
        }

        return handle.asType(type);
    }

    public static MethodHandle adapt(MethodHandle handle, Class<?>... parameterTypes) {
        return adapt(handle, MethodType.methodType(handle.type().returnType(), parameterTypes));
    }
}
