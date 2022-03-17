package test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("InstantiationOfUtilityClass")
public class Assert {
    private static final Assert instance = new Assert();

    public static Assert exception(String message, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            return instance;
        }

        throw new AssertionError(message);
    }

    public static Assert exception(Runnable action) {
        return exception(null, action);
    }

    public static Assert equal(Object... objects) {
        return asert(objects.length < 2 || Stream.of(objects).allMatch(Predicate.isEqual(objects[0])), () -> Arrays.deepToString(objects));
    }

    public static Assert arraysEqual(Object[]... arrays) {
        return asert(arrays.length < 2 || Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)), () -> Arrays.deepToString(arrays));
    }

    public static <T> Assert equalBy(Function<T, Object> key, T... objects) {
        return equal(Stream.of(objects).map(key).toArray());
    }

    public static Assert distinct(Object... objects) {
        return asert(Stream.of(objects).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
    }

    public static <T> Assert distinctBy(Function<T, Object> key, T... objects) {
        return asert(Stream.of(objects).map(key).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
    }

    private static Assert asert(boolean b) {
        assert b;
        return instance;
    }

    private static Assert asert(boolean b, Supplier<String> message) {
        assert b : message.get();
        return instance;
    }
}
