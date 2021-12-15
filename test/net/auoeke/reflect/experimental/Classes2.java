package net.auoeke.reflect.experimental;

import java.lang.reflect.Type;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Reflect;

public class Classes2 extends Classes {
    public static <T> Class<T> load(Type type) {
        return load(Reflect.defaultClassLoader, true, type);
    }

    public static <T> Class<T> load(boolean initialize, Type type) {
        return load(Reflect.defaultClassLoader, initialize, type);
    }

    public static <T> Class<T> load(ClassLoader loader, Type type) {
        return load(loader, true, type);
    }

    public static <T> Class<T> load(ClassLoader loader, boolean initialize, Type type) {
        return (Class<T>) (type instanceof Class ? type : load(loader, initialize, type.getTypeName()));
    }
}
