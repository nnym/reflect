package test;

import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class ReflectTests extends Reflect {
    @Test void instrumentationTest() {
        assert instrumentation() != null && instrumentation().isRedefineClassesSupported() && instrumentation().isRetransformClassesSupported() && instrumentation().isNativeMethodPrefixSupported();
    }
}
