package test;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings("NewClassNamingConvention")
@Testable
@Order(0)
public class Warmup {
	@Test
	void load() {
		var path = Path.of(Reflect.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		try (var list = Files.list((Files.isDirectory(path) ? path : FileSystems.newFileSystem(path).getPath("/")).resolve("net/auoeke/reflect"))) {
			list.forEach(type -> Class.forName("net.auoeke.reflect." + type.getFileName().toString().replace(".class", "")));
		}
	}
}
