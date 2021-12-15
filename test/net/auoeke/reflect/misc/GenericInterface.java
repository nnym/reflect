package net.auoeke.reflect.misc;

public interface GenericInterface<T> {
    default void endOfBridge(T thing) {}

    default Object object() {
        return null;
    }

    class GenericClass<T extends Integer> implements GenericInterface<T> {
        @Override
        public Double object() {
            return (Double) GenericInterface.super.object();
        }
    }
}
