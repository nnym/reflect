package test;

import java.util.function.Predicate;
import java.util.stream.Stream;
import net.auoeke.uncheck.ThrowingRunnable;

public class Assert {
    public static void exception(String message, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            return;
        }

        throw new AssertionError(message);
    }

    public static void equal(Object... objects) {
        if (objects.length > 0) {
            var first = objects[0];
            assert Stream.of(objects).allMatch(Predicate.isEqual(first));
        }
    }
}
