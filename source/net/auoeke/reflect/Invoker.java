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
import java.util.List;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.val;

import static net.gudenau.lib.unsafe.Unsafe.trustedLookup;

@SuppressWarnings("unused")
public class Invoker {
    /**
     Discard input parameters that did not match any output parameter.
     */
    public static final long DISCARD_UNUSED = 1L << 63;

    /**
     {@linkplain MethodHandles#reflectAs(Class, MethodHandle) Reflect} a method handle as a {@link Member}.

     @param handle a method handle
     @return the method handle reflected as a {@link Member}
     */
    public static Member member(MethodHandle handle) {
        return MethodHandles.reflectAs(Member.class, handle);
    }

    /**
     {@linkplain MethodHandles#reflectAs(Class, MethodHandle) Reflect} a method handle as a {@link Field}.

     @param handle a method handle
     @return the method handle reflected as a {@link Field}
     */
    public static Field field(MethodHandle handle) {
        return MethodHandles.reflectAs(Field.class, handle);
    }

    /**
     {@linkplain MethodHandles#reflectAs(Class, MethodHandle) Reflect} a method handle as an {@link Executable}.

     @param handle a method handle
     @return the method handle reflected as an {@link Executable}
     */
    public static Executable executable(MethodHandle handle) {
        return MethodHandles.reflectAs(Executable.class, handle);
    }

    /**
     {@linkplain MethodHandles#reflectAs(Class, MethodHandle) Reflect} a method handle as a {@link Constructor}.

     @param handle a method handle
     @return the method handle reflected as a {@link Constructor}
     */
    public static <T> Constructor<T> constructor(MethodHandle handle) {
        return MethodHandles.reflectAs(Constructor.class, handle);
    }

    /**
     {@linkplain MethodHandles#reflectAs(Class, MethodHandle) Reflect} a method handle as a {@link Method}.

     @param handle a method handle
     @return the method handle reflected as a {@link Method}
     */
    public static Method method(MethodHandle handle) {
        return MethodHandles.reflectAs(Method.class, handle);
    }

    /**
     Invoke a method handle without arguments.

     @param handle a method handle
     @param <T>    the handle's return type
     @return the result
     */
    @SneakyThrows
    public static <T> T invoke(MethodHandle handle) {
        return (T) handle.invoke();
    }

    /**
     Invoke a method handle with arguments.

     @param handle    a method handle
     @param arguments arguments wherewith to invoke the handle
     @param <T>       the handle's return type
     @return the result
     */
    @SneakyThrows
    public static <T> T invoke(MethodHandle handle, Object... arguments) {
        return (T) handle.invokeWithArguments(arguments);
    }

    /**
     Invokes {@link MethodHandles.Lookup#bind} on the trusted implementation lookup.

     @param receiver the object on which to invoke the target method
     @param name     the target method's name
     @param type     the target method's type
     @return a method handle corresponding to the target method and bound to {@code receiver}
     @see MethodHandles.Lookup#bind
     */
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
    public static MethodHandle findStatic(Class<?> refc, String name, MethodType type) {
        return trustedLookup.findStatic(refc, name, type);
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType) {
        return findStatic(refc, name, MethodType.methodType(returnType));
    }

    public static MethodHandle findStatic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
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
     Each input parameter (in {@code type}) is matched to the first {@linkplain Types#canCast(long, Class, Class) applicable} output parameter (in {@code handle})
     with the smallest {@linkplain Types#difference generation gap} between their types.
     <p>
     If {@code handle.type()} and {@code type} are equal, then return {@code handle}.

     @param flags  {@link #DISCARD_UNUSED}
     @param handle the method handle
     @param type   the method type whereto to adapt {@code handle}
     @return the adapted method handle
     @throws IllegalArgumentException if {@code handle.type().parameterCount()} != {@code type.parameterCount()}
     */
    public static MethodHandle adapt(long flags, MethodHandle handle, MethodType type) {
        val discardUnused = Flags.any(flags, DISCARD_UNUSED);

        if (!discardUnused && handle.type().parameterCount() != type.parameterCount()) {
            throw new IllegalArgumentException("Handle's (%d) and target type's (%d) parameter count differ.".formatted(handle.type().parameterCount(), type.parameterCount()));
        }

        val order = new int[handle.type().parameterCount()];
        val outputTypes = new ArrayList<>(handle.type().parameterList());
        val inputTypes = type.parameterArray();

        for (var inputIndex = 0; inputIndex < inputTypes.length; inputIndex++) {
            val inputType = inputTypes[inputIndex];
            val outputIterator = outputTypes.listIterator();
            val scores = new int[outputTypes.size()];
            var bestMatch = -1;
            var deviation = Integer.MAX_VALUE;

            while (outputIterator.hasNext()) {
                val outputType = outputIterator.next();

                if (outputType != null && outputType.isAssignableFrom(inputType)) {
                    if (deviation > (deviation = Math.min(deviation, Types.difference(inputType, outputType)))) {
                        bestMatch = outputIterator.previousIndex();
                    }
                }
            }

            if (bestMatch < 0) {
                if (!discardUnused) {
                    throw new IllegalArgumentException("No matching output parameter was found for input %s parameter at index %d.".formatted(inputType, inputIndex));
                }
            } else {
                order[bestMatch] = inputIndex;
                outputTypes.set(bestMatch, null);
            }
        }

        val outputIterator = outputTypes.listIterator();

        while (outputIterator.hasNext()) {
            val parameterType = outputIterator.next();

            if (parameterType != null) {
                throw new IllegalArgumentException("No matching input parameter was found for output %s parameter at index %d.".formatted(parameterType, outputIterator.previousIndex()));
            }
        }

        if (inputTypes.length != outputTypes.size() || IntStream.range(0, order.length).anyMatch(index -> order[index] != index)) {
            return MethodHandles.permuteArguments(handle, type, order);
        }

        return handle.asType(type);
    }

    /**
     Produce a method handle that invokes {@code handle} with input parameters reordered to match output parameters by type.

     @param handle         the method handle
     @param parameterTypes the input parameter types
     @return the adapted method handle
     @see #adapt(MethodHandle, MethodType)
     */
    public static MethodHandle adapt(long flags, MethodHandle handle, Class<?>... parameterTypes) {
        return adapt(flags, handle, MethodType.methodType(handle.type().returnType(), parameterTypes));
    }

    /**
     Produce a method handle that invokes {@code handle} with input parameters reordered to match output parameters by type.

     @param handle         the method handle
     @param parameterTypes the input parameter types
     @return the adapted method handle
     @see #adapt(MethodHandle, MethodType)
     */
    public static MethodHandle adapt(long flags, MethodHandle handle, List<? extends Class<?>> parameterTypes) {
        return adapt(flags, handle, MethodType.methodType(handle.type().returnType(), (List<Class<?>>) parameterTypes));
    }

    public static MethodHandle adapt(MethodHandle handle, MethodType type) {
        return adapt(0, handle, type);
    }

    /**
     Produce a method handle that invokes {@code handle} with input parameters reordered to match output parameters by type.

     @param handle         the method handle
     @param parameterTypes the input parameter types
     @return the adapted method handle
     @see #adapt(MethodHandle, MethodType)
     */
    public static MethodHandle adapt(MethodHandle handle, Class<?>... parameterTypes) {
        return adapt(0, handle, parameterTypes);
    }

    /**
     Produce a method handle that invokes {@code handle} with input parameters reordered to match output parameters by type.

     @param handle         the method handle
     @param parameterTypes the input parameter types
     @return the adapted method handle
     @see #adapt(MethodHandle, MethodType)
     */
    public static MethodHandle adapt(MethodHandle handle, List<? extends Class<?>> parameterTypes) {
        return adapt(0, handle, parameterTypes);
    }
}
