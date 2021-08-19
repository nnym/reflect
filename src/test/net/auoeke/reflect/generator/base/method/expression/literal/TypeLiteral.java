package net.auoeke.reflect.generator.base.method.expression.literal;

import net.auoeke.reflect.generator.base.type.ConcreteType;
import java.util.stream.Stream;
import net.auoeke.reflect.generator.base.method.expression.Expression;

public record TypeLiteral(ConcreteType type) implements Expression, Literal {
    public TypeLiteral(Class<?> type) {
        this(new ConcreteType(type));
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this.type);
    }

    @Override
    public String toString() {
        return this.type + ".class";
    }
}
