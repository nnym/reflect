package net.auoeke.reflect.ast.base.type;

import java.util.Objects;
import java.util.stream.Stream;
import net.auoeke.reflect.util.Util;

public record ConcreteType(Class<?> type) implements Type {
    public static final ConcreteType voidType = new ConcreteType(void.class);

    public ConcreteType {
        Objects.requireNonNull(type, "type must not be null");
    }

    @Override
    public String toString() {
        return Util.unqualifiedName(this.type);
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this);
    }
}
