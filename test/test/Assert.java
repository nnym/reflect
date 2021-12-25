package test;

import net.auoeke.uncheck.ThrowingRunnable;

public class Assert {
    public static void assertException(String message, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            return;
        }

        throw new AssertionError(message);
    }
}
