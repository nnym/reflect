package methods;

import java.lang.reflect.Method;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import org.openjdk.jmh.annotations.Benchmark;
import reflect.misc.A;

public class GetMethod {
    static final Method declaredMethod = Methods.of(A.class, "privateMethod2");

    @Benchmark public void bySignature() {
        Methods.of(A.class, "privateMethod2", int.class);
    }

    @Benchmark public void byName() {
        Methods.of(A.class, "privateMethod");
    }

    @Benchmark public void unreflect() {
        Invoker.unreflect(declaredMethod);
    }

    @Benchmark public void getAndUnreflect() {
        Invoker.unreflect(A.class, "privateMethod2", int.class);
    }

    @Benchmark public void findStatic() {
        Invoker.findStatic(A.class, "privateMethod", String.class);
    }

    @Benchmark public void getDeclaredMethod() {
        A.class.getDeclaredMethod("privateMethod");
    }

    @Benchmark public void getDeclaredMethodWithParameter() {
        A.class.getDeclaredMethod("privateMethod2", int.class);
    }
}
