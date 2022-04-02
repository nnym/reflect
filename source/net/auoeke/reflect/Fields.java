package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

/**
 @since 0.6.0
 */
public class Fields {
    private static final MethodHandle getDeclaredFields = Methods.of(Class.class)
        .filter(method -> Flags.isNative(method) && method.getReturnType() == Field[].class)
        .map(Invoker::unreflectSpecial)
        .map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method)
        .max(Comparator.comparing(method -> ((Field[]) method.invoke(Fields.class)).length))
        .get();

    private static final CacheMap<Class<?>, Field[]> fields = CacheMap.identity();
    private static final CacheMap<Class<?>, Field[]> staticFields = CacheMap.identity();
    private static final CacheMap<Class<?>, Field[]> instanceFields = CacheMap.identity();
    private static final CacheMap<Class<?>, CacheMap<String, Field>> fieldsByName = CacheMap.identity();

    public static final long modifiersOffset = offset(of(Field.class, "modifiers"));
    public static final long overrideOffset = offset(of(AccessibleObject.class, "override"));

    /**
     Compute the offset of a field from its (type or object) base.
     If the field is static, then the base is its declaring type;
     otherwise, the base is an instance of its declaring type.

     @param field a field
     @return the field's offset from its base
     @since 4.10.0
     */
    public static long offset(Field field) {
        return Flags.isStatic(field) ? Unsafe.staticFieldOffset(field) : Unsafe.objectFieldOffset(field);
    }

    /**
     Retrieve an array of fields declared by a type without filtering.

     @param type a type
     @return an array of {@code type}'s all fields
     */
    public static Field[] direct(Class<?> type) {
        return (Field[]) getDeclaredFields.invokeExact(type);
    }

    /**
     Return a stream of all fields declared by a type. The fields are cached and shared by all callers.

     @param type a type
     @return a stream of {@code type}'s all fields
     */
    public static Stream<Field> of(Class<?> type) {
        return Stream.of(fields.computeIfAbsent(type, Fields::direct));
    }

    /**
     Attempt to locate a field declared by a type by name.

     @param type a type
     @param name the name of a field declared by {@code type}
     @return the field declared by {@code type} and named by {@code name} if found or else {@code null}
     */
    public static Field of(Class<?> type, String name) {
        return fieldsByName.computeIfAbsent(type, type1 -> of(type1).collect(CacheMap::hash, (map, field) -> map.put(field.getName(), field), CacheMap::putAll)).get(name);
    }

    /**
     Attempt to locate a field in an object by name.
     If the object contains multiple fields of the same name; then the field declared by the most derived type is chosen.

     @param object an object
     @param name the name of a field in {@code object}
     @return the field in {@code object} and named by {@code name} if found or else {@code null}
     */
    public static Field of(Object object, String name) {
        return any(object.getClass(), name);
    }

    /**
     Return a sequential stream of all fields declared by {@code begin} and its base classes until {@code end} (exclusive).
     The fields in the stream are in class hierarchy order starting with the most derived class {@code begin}.

     @param begin the first class wherefrom to begin searching
     @param end the superclass of the last class to be searched for fields; may be {@code null}
     @return a sequential stream of all fields declared by {@code begin} and its base classes until {@code end}
     */
    public static Stream<Field> all(Class<?> begin, Class<?> end) {
        return Types.classes(begin, end).flatMap(Fields::of);
    }

    /**
     Return a sequential stream of all fields declared by {@code begin} and its base classes.
     The fields in the stream are in class hierarchy order starting with the most derived class {@code begin}.

     @param begin the first class wherefrom to begin searching
     @return a sequential stream of all fields declared by {@code begin} and its base classes
     */
    public static Stream<Field> all(Class<?> begin) {
        return all(begin, Object.class);
    }

    public static Stream<Field> all(Object object, Class<?> end) {
        return Types.classes(object.getClass(), end).flatMap(Fields::instanceOf);
    }

    public static Stream<Field> all(Object object) {
        return all(object, Object.class);
    }

    public static Field any(Class<?> type, String name) {
        return all(type).filter(field -> field.getName().equals(name)).findFirst().orElse(null);
    }

    public static Stream<Field> staticOf(Class<?> type) {
        return Stream.of(staticFields.computeIfAbsent(type, type1 -> of(type1).filter(Flags::isStatic).toArray(Field[]::new)));
    }

    public static Stream<Field> instanceOf(Class<?> type) {
        return Stream.of(instanceFields.computeIfAbsent(type, type1 -> of(type1).filter(Flags::isInstance).toArray(Field[]::new)));
    }

    public static Stream<Field> allInstance(Class<?> begin, Class<?> end) {
        return all(begin, end).filter(Flags::isInstance);
    }

    public static Stream<Field> allInstance(Class<?> type) {
        return allInstance(type, Object.class);
    }

    public static Stream<Field> allStatic(Class<?> type) {
        return all(type).filter(Flags::isStatic);
    }
}
