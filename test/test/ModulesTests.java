package test;

import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Modules;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class ModulesTests {
    @Test
    void open() {

    }

    @Test
    void open1() {
    }

    @Test
    void open2() {
    }

    @Test
    void open3() {
    }

    @Test
    void anonymize() {
    }

    @Test
    void anonymize1() {
    }

    @Test
    void anonymizeAll() {
    }

    @Test
    void anonymize2() {
    }

    @Test
    void anonymizeAll1() {
    }

    @Test
    void anonymizeAll2() throws Throwable {
        var jdkUnsafe = Classes.load("jdk.internal.misc.Unsafe");
        var theUnsafe = jdkUnsafe.getDeclaredField("theUnsafe");
        var assertionError = new AssertionError("jdk.internal.misc.Unsafe should be inaccessible.");

        try {
            theUnsafe.trySetAccessible();
            theUnsafe.get(null);

            throw assertionError;
        } catch (Throwable throwable) {
            if (throwable == assertionError) {
                throw Unsafe.throwException(throwable);
            }
        }

        Modules.anonymizeAll();
        theUnsafe.trySetAccessible();
        theUnsafe.get(null);
    }

    @Test
    void allLayers() {
    }

    @Test
    void layers() {
    }

    @Test
    void allLayers1() {
    }

    @Test
    void modules() {
    }

    @Test
    void allModules() {
    }
}
