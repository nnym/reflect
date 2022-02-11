package reflect.ast;

import java.util.Locale;
import java.util.function.Consumer;

public enum Access {
    GET,
    PUT;

    public boolean get() {
        return this == GET;
    }

    public boolean put() {
        return this == PUT;
    }

    public Access get(Runnable action) {
        return this.if$(this.get(), action);
    }

    public Access put(Runnable action) {
        return this.if$(this.put(), action);
    }

    public <T> Access get(T arg, Consumer<T> action) {
        return this.if$(this.get(), () -> action.accept(arg));
    }

    public <T> Access put(T arg, Consumer<T> action) {
        return this.if$(this.put(), () -> action.accept(arg));
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    private Access if$(boolean condition, Runnable action) {
        if (condition) {
            action.run();
        }

        return this;
    }
}
