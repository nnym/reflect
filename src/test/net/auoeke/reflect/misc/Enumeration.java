package net.auoeke.reflect.misc;

public enum Enumeration implements GenericInterface<A> {;
    public final double test;

    Enumeration(double test) {
        this.test = test;
    }

    @Override
    public void endOfBridge(A thing) {

    }
}
