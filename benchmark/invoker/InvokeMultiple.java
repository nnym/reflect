package invoker;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.auoeke.reflect.Invoker;
import org.openjdk.jmh.annotations.Benchmark;

public class InvokeMultiple {
    static final List<MethodHandle> list = Stream.of("a", "b", "c").map(name -> Invoker.findStatic(InvokeMultiple.class, name, int.class)).toList();
    static final MethodHandle[] array = list.toArray(MethodHandle[]::new);
    static final Random random = new Random();

    @Benchmark public static int invokeStatically() {
        return a() + b() + c();
    }

    @Benchmark public static int invokeArray() {
        var result = 0;

        for (var handle : array) {
            result += (int) handle.invokeExact();
        }

        return result;
    }

    @Benchmark public static int invokeList() {
        var result = 0;

        for (var handle : list) {
            result += (int) handle.invokeExact();
        }

        return result;
    }

    static int a() {
        return random.nextInt() + random.nextInt();
    }

    static int b() {
        return random.nextInt() + random.nextInt();
    }

    static int c() {
        return random.nextInt() + random.nextInt();
    }
}
