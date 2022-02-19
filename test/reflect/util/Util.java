package reflect.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Util {
    public static int repetitions = 10000;

    public static <T> T nul() {
        return null;
    }

    public static void bp() {}

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> Stream<T> join(Stream<? extends T>... streams) {
        return (Stream<T>) Stream.of(streams).reduce(Stream::concat).get();
    }

    public static String unqualifiedName(Class<?> type) {
        var name = type.getName();
        var dimensions = name.lastIndexOf('[') + 1;
        name = name.substring(dimensions);

        if (name.charAt(name.length() - 1) == ';') {
            name = name.substring(1, name.length() - 1);
        }

        return name.substring(name.lastIndexOf('.') + 1).replace('$', '.') + "[]".repeat(dimensions);
    }

    public static boolean equals(Object... objects) {
        return Stream.of(objects).skip(1).allMatch(objects[0]::equals);
    }

    public static void repeat(Runnable test) {
        repeat(repetitions, test);
    }

    public static void repeat(int repetitions, Runnable test) {
        for (var i = 0; i < repetitions; i++) {
            test.run();
        }
    }

    public static Runnable voidify(Supplier<?> supplier) {
        return supplier::get;
    }

    public static String buildString(Object initial, Consumer<StringBuilder> consumer) {
        var builder = new StringBuilder(String.valueOf(initial));
        consumer.accept(builder);

        return builder.toString();
    }

    public static String buildString(Consumer<StringBuilder> consumer) {
        return buildString("", consumer);
    }

    public static <T> void with(T value, T current, Consumer<? super T> set, Runnable action) {
        set.accept(value);
        action.run();
        set.accept(current);
    }
}
