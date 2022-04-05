package other;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import net.auoeke.reflect.Reflect;
import org.openjdk.jmh.annotations.Benchmark;

public class Warmups {
    @Benchmark public void init() {
        var path = Path.of(Reflect.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        try (var list = Files.list((Files.isDirectory(path) ? path : FileSystems.newFileSystem(path).getPath("/")).resolve("net/auoeke/reflect"))) {
            list.forEach(type -> Class.forName("net.auoeke.reflect." + type.getFileName().toString().replace(".class", "")));
        }
    }
}
