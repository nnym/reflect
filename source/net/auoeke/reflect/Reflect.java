package net.auoeke.reflect;

import java.util.HashMap;
import java.util.function.Supplier;
import lombok.SneakyThrows;

public class Reflect {
    public static boolean securityDisabled;

    /** the default class loader for operations that require a class loader */
    public static ClassLoader defaultClassLoader = Reflect.class.getClassLoader();

    /**
     Disable {@linkplain System#getSecurityManager() the deprecated security manager}.
     <br>
     Using this method is discouraged.
     */
    @SuppressWarnings("removal")
    public static void disableSecurity() {
        if (!securityDisabled) {
            Accessor.putReference(System.class, "security", null);
            securityDisabled = true;
        }
    }

    /**
     Clears the reflection field filter map, preventing {@link Class#getFields} and {@link Class#getDeclaredFields} from filtering,
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.

     @apiNote this method can break code that relies on the aforementioned methods filtering fields.
     */
    public static void clearFieldFilterMap() {
        Accessor.putReferenceVolatile(Classes.Reflection, "fieldFilterMap", new HashMap<>());
    }

    /**
     Clears the reflection method filter map, preventing {@link Class#getMethods} and {@link Class#getDeclaredMethods} from filtering
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.
     <br>
     @apiNote This method can break (and has broken some part of Gson) code that relies on the aforementioned methods filtering methods.
     */
    public static void clearMethodFilterMap() {
        Accessor.putReferenceVolatile(Classes.Reflection, "methodFilterMap", new HashMap<>());
    }

    /** Using this field instead of a literal `null` prevents redundant warnings. */
    static <T> T nul() {
        return null;
    }

    static <T> T runNull(ThrowingSupplier<T> supplier) {
        try {
            return supplier.execute();
        } catch (Throwable throwable) {
            return null;
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable extends Runnable {
        void execute() throws Throwable;

        @SneakyThrows
        @Override default void run() {
            this.execute();
        }
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> extends Supplier<T> {
        T execute() throws Throwable;

        @SneakyThrows
        @Override default T get() {
            return this.execute();
        }
    }
}
