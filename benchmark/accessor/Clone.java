package accessor;

import net.auoeke.reflect.Accessor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import reflect.misc.A;

public class Clone {
	static final A a = new A();

	@Benchmark public Object ordinaryClone() {
		return a.clone();
	}

	@Benchmark public Object copyConstructor() {
		return new A(a);
	}

	@Benchmark public Object copy() {
		return a.copy();
	}

	@BenchmarkMode(Mode.SingleShotTime)
	@Benchmark public Object firstAccessorClone() {
		return Accessor.clone(a);
	}

	@Benchmark public Object accessorClone() {
		return Accessor.clone(a);
	}
}
