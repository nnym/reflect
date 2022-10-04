package accessor;

import net.auoeke.reflect.Accessor;
import org.openjdk.jmh.annotations.Benchmark;
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

	@Benchmark public Object accessorClone() {
		return Accessor.clone(a);
	}
}
