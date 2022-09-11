package net.auoeke.reflect;

import java.util.function.Function;

/**
 @since 4.9.0
 */
class Exceptions {
    private static final Pointer detailMessage = Pointer.of(Throwable.class, "detailMessage");
    private static final Pointer stackTrace = Pointer.of(Throwable.class, "stackTrace");
    private static final StackTraceElement[] defaultStackTrace = stackTrace.getT(new Throwable());

    public static <T extends Throwable> T message(T trouble, String message) {
        detailMessage.putReference(trouble, message);
        return trouble;
    }

    public static <T extends Throwable> T message(T trouble, Function<String, String> transformer) {
        return message(trouble, transformer.apply(trouble.getMessage()));
    }

    public static <T extends Throwable> T stackTrace(T trouble, StackTraceElement[] trace) {
        stackTrace.putReference(trouble, trace);
        return trouble;
    }

    public static <T extends Throwable> T clearStackTrace(T trouble) {
        return stackTrace(trouble, defaultStackTrace);
    }

    public static <T extends Throwable> T disableStackTrace(T trouble) {
        return stackTrace(trouble, null);
    }
}
