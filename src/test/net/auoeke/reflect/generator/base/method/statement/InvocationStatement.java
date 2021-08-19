package net.auoeke.reflect.generator.base.method.statement;

import net.auoeke.reflect.generator.base.type.ConcreteType;
import java.util.stream.Stream;
import net.auoeke.reflect.generator.base.method.expression.Invocation;

public record InvocationStatement(Invocation invocation) implements Statement {
    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.invocation.referencedTypes();
    }

    @Override
    public String toString() {
        return this.invocation.toString() + ';';
    }
}
