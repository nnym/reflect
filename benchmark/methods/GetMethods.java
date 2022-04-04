package methods;

import java.util.Arrays;
import net.auoeke.reflect.Methods;
import org.openjdk.jmh.annotations.Benchmark;

public class GetMethods {
    @Benchmark public void getDeclaredMethods() {
        Arrays.asList(GetMethods.class.getDeclaredMethods());
    }

    @Benchmark public void of() {
        Methods.of(GetMethods.class).toList();
    }
}
