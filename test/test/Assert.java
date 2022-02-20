package test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("InstantiationOfUtilityClass")
public class Assert {
    public static Assert exception(String message, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            return null;
        }

        assert false : message;

        return new Assert();
    }

    public static Assert equal(Object... objects) {
        assert objects.length < 2 || Stream.of(objects).allMatch(Predicate.isEqual(objects[0])) : Arrays.deepToString(objects);

        return new Assert();
    }

    public static Assert arraysEqual(Object[]... arrays) {
        assert arrays.length < 2 || Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)) : Arrays.deepToString(arrays);

        return new Assert();
    }

    public static <T, X> Assert equal(Function<T, X> key, T a, T b) {
        equal(key.apply(a), key.apply(b));

        return new Assert();
    }
}
