package test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;
import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class ReflectTests extends Reflect {
    @Test public void instrumentationTest() {
        if (Reflect.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
            var loader = new URLClassLoader(Stream.of(Reflect.class, ReflectTests.class).map(c -> c.getProtectionDomain().getCodeSource().getLocation()).toArray(URL[]::new), null) {
                @Override protected Class<?> loadClass(String name, boolean resolve) {
                    if (name.startsWith("java.")) {
                        return ClassLoader.getSystemClassLoader().loadClass(name);
                    }

                    var type = this.findLoadedClass(name);

                    if (type == null) {
                        var classFile = this.findResource(name.replace('.', '/') + ".class");

                        if (classFile == null) {
                            return ClassLoader.getSystemClassLoader().loadClass(name);
                        }

                        var bytes = classFile.openStream().readAllBytes();
                        type = this.defineClass(null, bytes, 0, bytes.length);
                    }

                    if (resolve) {
                        this.resolveClass(type);
                    }

                    return type;
                }
            };

            var clone = loader.loadClass(ReflectTests.class.getName());
            clone.getDeclaredMethod("instrumentationTest").invoke(clone.getDeclaredConstructor().newInstance());
            loader.close();
        }

        assert instrumentation() != null && instrumentation().isRedefineClassesSupported() && instrumentation().isRetransformClassesSupported() && instrumentation().isNativeMethodPrefixSupported();
    }
}
