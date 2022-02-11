package test;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class Assert {
    public static void exception(String message, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            return;
        }

        assert false : message;
    }

    public static void equal(Object... objects) {
        assert objects.length < 2 || Stream.of(objects).allMatch(Predicate.isEqual(objects[0]));
    }
}
