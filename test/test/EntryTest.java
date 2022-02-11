package test;

import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
@Order(0)
public class EntryTest {
    @Test
    void load() {
        try {
            Files.list(Path.of(Reflect.class.getResource(".").toURI())).forEach(type -> Class.forName("net.auoeke.reflect." + type.getFileName().toString().replace(".class", "")));
        } catch (NotDirectoryException exception) {}
    }
}
