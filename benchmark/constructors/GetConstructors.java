package constructors;

import java.util.Arrays;
import net.auoeke.reflect.Constructors;
import org.openjdk.jmh.annotations.Benchmark;

public class GetConstructors {
    @Benchmark public Object getDeclaredConstructors() {
        return Arrays.asList(GetConstructors.class.getDeclaredConstructors());
    }

    @Benchmark public Object of() {
        return Constructors.of(GetConstructors.class).toList();
    }
}
