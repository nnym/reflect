package constructors;

import java.util.concurrent.TimeUnit;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Constructors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SingleShotTime)
public class FirstGet {
    @Benchmark public Object getDeclaredConstructors() {
        return FirstGet.class.getDeclaredConstructors();
    }

    @Benchmark public Object of() {
        return Constructors.of(FirstGet.class);
    }
}
