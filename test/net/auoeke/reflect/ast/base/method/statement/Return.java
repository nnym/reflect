package net.auoeke.reflect.ast.base.method.statement;

import net.auoeke.reflect.ast.base.type.ConcreteType;
import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.method.expression.Expression;

public record Return(Expression expression) implements Statement {
    @Override
    public String toString() {
        return "return " + this.expression + ';';
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.expression.referencedTypes();
    }
}
