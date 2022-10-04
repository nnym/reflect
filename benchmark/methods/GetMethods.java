package methods;

import java.util.Arrays;
import net.auoeke.reflect.Methods;
import org.openjdk.jmh.annotations.Benchmark;

public class GetMethods {
	@Benchmark public Object getDeclaredMethods() {
		return Arrays.asList(GetMethods.class.getDeclaredMethods());
	}

	@Benchmark public Object of() {
		return Methods.of(GetMethods.class).toList();
	}
}
