package accessor;

import net.auoeke.reflect.Accessor;
import org.openjdk.jmh.annotations.Benchmark;
import reflect.misc.A;

public class Clone {
    static final A a = new A();

    @Benchmark public void ordinaryClone() {
        a.clone();
    }

    @Benchmark public void copyConstructor() {
        new A(a);
    }

    @Benchmark public void copy() {
        a.copy();
    }

    @Benchmark public void accessorClone() {
        Accessor.clone(a);
    }
}
