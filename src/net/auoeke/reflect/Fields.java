package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

import static net.auoeke.reflect.Reflect.run;

public class Fields {
    private static final MethodHandle getDeclaredFields = Methods.get(Class.class)
        .filter(method -> Flags.isNative(method) && method.getReturnType() == Field[].class).findAny()
        .map(Invoker::unreflectSpecial)
        .map(method -> method.type().parameterCount() > 1 ? MethodHandles.insertArguments(method, 1, false) : method).get();

    private static final CacheMap<Class<?>, Field[]> fields = CacheMap.identity();
    private static final CacheMap<Class<?>, Field[]> staticFields = CacheMap.identity();
    private static final CacheMap<Class<?>, Field[]> instanceFields = CacheMap.identity();
    private static final CacheMap<Class<?>, Map<String, Field>> fieldsByName = CacheMap.identity();

    public static final long modifiersOffset = get(Field.class).filter(field -> field.getName().equals("modifiers")).findAny().map(Unsafe::objectFieldOffset).get();
    public static final long overrideOffset = get(AccessibleObject.class).filter(field -> field.getName().equals("override")).findAny().map(Unsafe::objectFieldOffset).get();

    public static Field[] direct(Class<?> klass) {
        return run(() -> (Field[]) getDeclaredFields.invokeExact(klass));
    }

    public static Stream<Field> get(Class<?> type) {
        return Stream.of(fields.computeIfAbsent(type, Fields::direct));
    }

    public static Stream<Field> all(Class<?> begin, Class<?> end) {
        return Types.classes(begin, end).flatMap(Fields::get);
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

    public static Field get(String klass, String name) {
        return get(Classes.load(klass), name);
    }

    public static Field get(Class<?> type, String name) {
        return fieldsByName.computeIfAbsent(type, type1 -> get(type1).collect(IdentityHashMap::new, (map, field) -> map.put(field.getName(), field), Map::putAll)).get(name);
    }

    public static Field get(Object object, String name) {
        return any(object.getClass(), name);
    }

    public static Field any(Class<?> type, String name) {
        return all(type).filter(field -> field.getName().equals(name)).findAny().orElse(null);
    }

    public static Field any(String klass, String name) {
        return any(Classes.load(klass), name);
    }

    public static Stream<Field> getStatic(Class<?> type) {
        return Stream.of(staticFields.computeIfAbsent(type, type1 -> get(type1).filter(Flags::isStatic).toArray(Field[]::new)));
    }

    public static Stream<Field> getInstance(Object object) {
        return getInstance(object.getClass());
    }

    public static Stream<Field> getInstance(Class<?> type) {
        return Stream.of(instanceFields.computeIfAbsent(type, type1 -> get(type1).filter(Flags::isInstance).toArray(Field[]::new)));
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
