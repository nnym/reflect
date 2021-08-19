package net.auoeke.reflect.experimental;

import java.lang.invoke.MethodHandle;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.gudenau.lib.unsafe.Unsafe;

public class Modules {
    private static final MethodHandle implAddOpensToAllUnnamed = Invoker.findVirtual(Module.class, "implAddOpensToAllUnnamed", void.class, String.class);

    public static void open(String type) {
        open(Classes.load(type));
    }

    public static void open(Class<?> type) {
        open(type.getPackageName(), type.getModule());
    }

    public static void open(String pkg, String klass) {
        open(pkg, Classes.load(pkg + "." + klass).getModule());
    }

    public static void open(String pkg, Module module) {
        try {
            implAddOpensToAllUnnamed.invokeExact(module, pkg);
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }
}
