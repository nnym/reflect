package other;

import net.auoeke.reflect.Reflect;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

public class Instrument {
	@BenchmarkMode(Mode.SingleShotTime)
	@Benchmark public void instrument() {
		Reflect.instrument().value();
	}
}
