package test;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Pointer;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.ReflectTest;

@SuppressWarnings("AccessStaticViaInstance")
@Testable
public class ClassesTests extends Classes {
    @Test
    void classes() {
        var classPath = classPath(ReflectTest.class.getClassLoader());
        var url = Path.of("test").toUri().toURL();
        addURL(classPath, url);
        assert List.of(urls(classPath)).contains(url);
    }

    @Test
    void definer() {
        class Test {
            static final UUID u = UUID.randomUUID();
        }

        var bytes = classFile(Test.class);
        var buffer = ByteBuffer.wrap(bytes);

        var b = ClassDefiner.make().loader(new ClassLoader() {}).classFile(bytes).unsafe().initialize().define();
        var bb = ClassDefiner.make().loader(new SecureClassLoader() {}).classFile(buffer).unsafe().initialize().define();
        var s = ClassDefiner.make().secureLoader(new SecureClassLoader() {}).from(Test.class).initialize().define();
        var sb = ClassDefiner.make().secureLoader(new SecureClassLoader() {}).classFile(buffer.clear()).define();

        var u = Pointer.of(sb, "u");
        assert u.getReference() == null;
        Assert.distinct(Test.class, b, bb, s, sb)
            .equalBy(Class::getName, Test.class, b, bb, s, sb)
            .distinct(Stream.concat(Stream.of(null, Test.u), Stream.of(b, bb, s, initialize(sb)).map(u::getReference)).toArray())
            .exception("java.* packages are restricted", () -> ClassDefiner.make().loader(new SecureClassLoader() {}).from(UUID.class).unsafe().define());
    }
}
