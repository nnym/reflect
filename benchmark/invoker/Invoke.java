package invoker;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.auoeke.reflect.Invoker;
import org.openjdk.jmh.annotations.Benchmark;

@SuppressWarnings("unused")
public class Invoke {
    static final Object object = new Object();
    static final MethodHandle handle = Invoker.findVirtual(Object.class, "hashCode", int.class);
    static final Method method = Object.class.getMethod("hashCode");

    @Benchmark public int handleExact() {
        return (int) handle.invokeExact(object);
    }

    @Benchmark public int handle() {
        return (int) handle.invoke(object);
    }

    @Benchmark public int method() {
        return (int) method.invoke(object);
    }
}
