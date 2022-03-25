package test;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Pointer;
import net.auoeke.reflect.Reflect;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.ReflectTest;

@SuppressWarnings("AccessStaticViaInstance")
@Testable
public class ClassesTests extends Classes {
    @Test public void reinterpret() {
        Double dubble = 0D;
        var loong = Classes.reinterpret(dubble, Long.class);

        Accessor.putLong(loong, "value", 0xFFFFFFFFFFL);
        assert loong == 0xFFFFFFFFFFL;

        Classes.reinterpret(loong, Double.class);

        var object = (Object) Classes.reinterpret(Unsafe.allocateInstance(Object.class), ReflectTest.class);
        assert object.getClass() == (object = Unsafe.allocateInstance(ReflectTest.class)).getClass();
        assert object.getClass() == Classes.reinterpret(object, ReflectTest.class).getClass();
        assert Classes.reinterpret(Unsafe.allocateInstance(Object.class), new ReflectTest()).getClass() == ReflectTest.class;
    }

    @Test void classes() {
        var classPath = classPath(ReflectTest.class.getClassLoader());
        var url = Path.of("test").toUri().toURL();
        addURL(classPath, url);
        assert List.of(urls(classPath)).contains(url);
    }

    @Test void definer() {
        class Test {
            static final UUID u = UUID.randomUUID();
        }

        var bytes = classFile(Test.class);
        var buffer = ByteBuffer.wrap(bytes);

        var b = ClassDefiner.make().loader(new ClassLoader() {}).classFile(bytes).unsafe().initialize().define();
        var bb = ClassDefiner.make().loader(new SecureClassLoader() {}).classFile(buffer).unsafe().initialize().define();
        var s = ClassDefiner.make().from(Test.class).secureLoader(new SecureClassLoader() {}, Test.class.getProtectionDomain().getCodeSource()).initialize().define();
        var sb = ClassDefiner.make().secureLoader(new SecureClassLoader() {}, Test.class.getProtectionDomain().getCodeSource()).classFile(buffer.clear()).define();
        var doNotInitialize = ClassDefiner.make().loader(new ClassLoader() {}).classFile("test/DoNotInitialize").define();
        var u = Pointer.of(sb, "u");

        Assert.equal(u.getReference(), null)
            .equalBy(type -> type.getProtectionDomain().getCodeSource(), s, sb, Test.class)
            .equalBy(Class::getName, Test.class, b, bb, s, sb)
            .distinctBy(Class::getProtectionDomain, s, sb, Test.class)
            .distinct(Test.class, b, bb, s, sb)
            .distinct(Stream.concat(Stream.of(null, Test.u), Stream.of(b, bb, s, initialize(sb)).map(u::getReference)).toArray())
            .exception("java.* packages are restricted", () -> ClassDefiner.make().loader(new SecureClassLoader() {}).from(UUID.class).unsafe().define());
    }

    @Test void load() {
        var loader = new URLClassLoader(new URL[]{ClassesTests.class.getProtectionDomain().getCodeSource().getLocation()}) {
            @Override protected Class<?> loadClass(String name, boolean resolve) {
                try {
                    this.findClass(name);
                    return null;
                } catch (ClassNotFoundException bad) {
                    return super.loadClass(name, resolve);
                }
            }
        };

        assert loader.loadClass(Object.class.getName()) != null;
        assert loader.loadClass(ClassesTests.class.getName()) == null;

        class Test {
            static Class<?> load(String name) {
                return loadWithCaller(name);
            }
        }

        assert ClassesTests.class == Invoker.findStatic(ClassDefiner.make().loader(loader).classFile(classFile(Test.class)).define(), "load", Class.class, String.class).invoke(ClassesTests.class.getName());
    }
}

class DoNotInitialize {
    static {
        if (true) { // Thankfully the compiler is stupid.
            throw new UnsupportedOperationException("initialization");
        }
    }
}
