package pointer;

import java.lang.invoke.MethodHandle;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Pointer;
import org.openjdk.jmh.annotations.Benchmark;

public class ClonePointer {
    static final Pointer pointer = Pointer.of(Integer.class, "value");
    static final MethodHandle clone = Invoker.bind(pointer, "clone", Object.class);

    @Benchmark public Pointer cloneable() {
        return (Pointer) (Object) clone.invokeExact();
    }

    @Benchmark public Pointer customClone() {
        return pointer.clone();
    }
}
