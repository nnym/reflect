package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

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
     If the field is static, then the offset is from its declaring type;
     otherwise, the offset is from an object of the type of its declaring class.

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

    public static Field of(String type, String name) {
        return of(Class.forName(type), name);
    }

    public static Field of(Class<?> type, String name) {
        return fieldsByName.computeIfAbsent(type, type1 -> of(type1).collect(CacheMap::hash, (map, field) -> map.put(field.getName(), field), CacheMap::putAll)).get(name);
    }

    public static Stream<Field> all(Class<?> begin, Class<?> end) {
        return Types.classes(begin, end).flatMap(Fields::of);
    }

    public static Stream<Field> all(Class<?> begin) {
        return all(begin, Object.class);
    }

    public static Stream<Field> all(Object object, Class<?> end) {
        return all(object.getClass(), end);
    }

    public static Stream<Field> all(Object object) {
        return all(object.getClass(), Object.class);
    }

    public static Field of(Object object, String name) {
        return any(object.getClass(), name);
    }

    public static Field any(Class<?> type, String name) {
        return all(type).filter(field -> field.getName().equals(name)).findFirst().orElse(null);
    }

    public static Field any(String type, String name) {
        return any(Class.forName(type), name);
    }

    public static Stream<Field> staticOf(Class<?> type) {
        return Stream.of(staticFields.computeIfAbsent(type, type1 -> of(type1).filter(Flags::isStatic).toArray(Field[]::new)));
    }

    public static Stream<Field> instanceOf(Object object) {
        return instanceOf(object.getClass());
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

    public static Stream<Field> allInstance(Object object) {
        return allInstance(object.getClass());
    }

    public static Stream<Field> allStatic(Class<?> type) {
        return all(type).filter(Flags::isStatic);
    }

    public static Stream<Field> allStatic(Object object) {
        return allStatic(object.getClass());
    }
}
