package reflect.util;

import java.util.function.Function;

public class Box<T> {
    public T value;

    public static <T> Box of(T value) {
        var box = new Box<>();
        box.value = value;

        return box;
    }

    public T get() {
        return this.value;
    }

    public Box<T> set(T value) {
        this.value = value;

        return this;
    }

    public Box<T> set(Function<T, T> function) {
        return this.set(function.apply(this.value));
    }

    public <R> Box<T> map(Function<T, R> function) {
        return of(function.apply(this.value));
    }
}
