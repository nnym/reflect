package net.auoeke.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import net.gudenau.lib.unsafe.Unsafe;

public class Reflect {
    public static boolean illegalAccessLoggerDisabled;
    public static boolean securityDisabled;

    /** the default class loader for operations that require a class loader */
    public static ClassLoader defaultClassLoader = Reflect.class.getClassLoader();

    /** Using this field instead of a literal `null` prevents redundant warnings. */
    static <T> T nul() {
        return null;
    }

    public static void disableSecurity() {
        if (!securityDisabled) {
            Field security = Fields.rawField(System.class, "security");

            if (Modifier.isVolatile(security.getModifiers())) {
                Unsafe.putObject(System.class, Unsafe.staticFieldOffset(security), null);
            } else {
                Unsafe.putObjectVolatile(System.class, Unsafe.staticFieldOffset(security), null);
            }

            securityDisabled = true;
        }
    }

    /**
     Clears the reflection field filter map, preventing {@link Class#getFields} and {@link Class#getDeclaredFields} from filtering,
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.

     @apiNote this method can break code that relies on the aforementioned methods filtering fields.
     */
    public static void clearFieldFilterMap() {
        Unsafe.putObjectVolatile(Classes.Reflection, Unsafe.staticFieldOffset(Fields.field(Classes.Reflection, "fieldFilterMap")), new HashMap<>());
    }

    /**
     Clears the reflection method filter map, preventing {@link Class#getMethods} and {@link Class#getDeclaredMethods} from filtering,
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.

     @apiNote this method can break code that relies on the aforementioned methods filtering methods.
     */
    public static void clearMethodFilterMap() {
        Unsafe.putObjectVolatile(Classes.Reflection, Unsafe.staticFieldOffset(Fields.field(Classes.Reflection, "methodFilterMap")), new HashMap<>());
    }
}
