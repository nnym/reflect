package invoker;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Random;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import org.openjdk.jmh.annotations.Benchmark;

@SuppressWarnings("unused")
public class Invoke {
    static final String NAME = "hashCode";

    static final Random object = new Random();
    static final Method method = Methods.of(Object.class, NAME);
    static final MethodHandle handle = Invoker.findVirtual(Object.class, NAME, int.class);
    static final MethodHandle bind = Invoker.bind(object, NAME, int.class);
    static final MethodHandle bindTo = handle.bindTo(object);

    @Benchmark public int direct() {
        return object.hashCode();
    }

    @Benchmark public int methodInvoke() {
        // ~70x direct
        return (int) method.invoke(object);
    }

    @Benchmark public int invoke() {
        // ~2x direct
        return (int) handle.invoke(object);
    }

    @Benchmark public int invokeWithExactSignature() {
        // same as direct
        return (int) handle.invoke((Object) object);
    }

    @Benchmark public int invokeExact() {
        // same as direct
        return (int) handle.invokeExact((Object) object);
    }

    @Benchmark public int bind() {
        // same as direct
        return (int) bind.invoke();
    }

    @Benchmark public int bindTo() {
        // slightly slower than direct
        return (int) bindTo.invoke();
    }
}
