package net.auoeke.reflect;

import java.util.function.Function;

import static net.auoeke.dycon.Dycon.*;

/**
 @since 4.9.0
 */
class Exceptions {
	private static final Pointer stackTrace = Pointer.of(Throwable.class, "stackTrace");

	public static <T extends Throwable> T message(T trouble, String message) {
		ldc(() -> Pointer.of(Throwable.class, "detailMessage")).putReference(trouble, message);
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
		return stackTrace(trouble, ldc(() -> stackTrace.getT(new Throwable())));
	}

	public static <T extends Throwable> T disableStackTrace(T trouble) {
		return stackTrace(trouble, null);
	}
}
