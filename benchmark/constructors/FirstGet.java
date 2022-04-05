package constructors;

import java.util.concurrent.TimeUnit;
import net.auoeke.reflect.Constructors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

public class FirstGet {
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark public Object getDeclaredConstructors() {
        return FirstGet.class.getDeclaredConstructors();
    }

    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark public Object of() {
        return Constructors.of(FirstGet.class);
    }
}
