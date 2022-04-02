package net.auoeke.reflect;

import java.util.function.Function;

/**
 @since 4.9.0
 */
class Exceptions {
    private static final Pointer detailMessage = Pointer.of(Throwable.class, "detailMessage");

    public static <T extends Throwable> T message(T trouble, String message) {
        detailMessage.putReference(trouble, message);
        return trouble;
    }

    public static <T extends Throwable> T message(T trouble, Function<String, String> transformer) {
        return message(trouble, transformer.apply(trouble.getMessage()));
    }
}
