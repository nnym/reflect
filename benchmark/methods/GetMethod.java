package methods;

import java.lang.reflect.Method;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import org.openjdk.jmh.annotations.Benchmark;
import reflect.misc.A;

public class GetMethod {
    static final Method declaredMethod = Methods.of(A.class, "privateMethod2");

    @Benchmark public Object bySignature() {
        return Methods.of(A.class, "privateMethod2", int.class);
    }

    @Benchmark public Object byName() {
        return Methods.of(A.class, "privateMethod2");
    }

    @Benchmark public Object unreflect() {
        return Invoker.unreflect(declaredMethod);
    }

    @Benchmark public Object getAndUnreflect() {
        return Invoker.unreflect(A.class, "privateMethod2", int.class);
    }

    @Benchmark public Object findStatic() {
        return Invoker.findStatic(A.class, "privateMethod2", String.class, int.class);
    }

    @Benchmark public Object getDeclaredMethod() {
        return A.class.getDeclaredMethod("privateMethod2", int.class);
    }
}
