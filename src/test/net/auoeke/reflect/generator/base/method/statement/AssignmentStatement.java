package net.auoeke.reflect.generator.base.method.statement;

import net.auoeke.reflect.generator.base.method.expression.StoreVariable;
import net.auoeke.reflect.generator.base.type.ConcreteType;
import java.util.stream.Stream;

public record AssignmentStatement(StoreVariable variable) implements Statement {
    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.variable.referencedTypes();
    }

    @Override
    public String toString() {
        return this.variable.toString() + ';';
    }
}
