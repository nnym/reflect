package test;

import java.lang.invoke.MethodHandles;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Modules;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

// Must not comtaminate other tests; especially ReflectTests::instrumentationTest.
@Order(Integer.MAX_VALUE)
@Testable
public class ModulesTests {
    @Test
    void openTest() {
        Runnable getUnsafe = () -> {
            var theUnsafe = Classes.load("jdk.internal.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafe.trySetAccessible();
            theUnsafe.get(null);
        };

        Runnable getIMPL_LOOKUP = () -> {
            var field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.trySetAccessible();
            field.get(null);
        };

        Assert.exception("jdk.internal.misc.Unsafe should be inaccessible.", getUnsafe);
        Assert.exception("MethodHandles.Lookup#IMPL_LOOKUP should be inaccessible.", getIMPL_LOOKUP);
        Modules.open(Object.class.getModule());
        getUnsafe.run();
        getIMPL_LOOKUP.run();
    }
}
