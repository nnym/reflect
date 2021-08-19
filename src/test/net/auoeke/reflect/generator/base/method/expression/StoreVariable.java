package net.auoeke.reflect.generator.base.method.expression;

import net.auoeke.reflect.generator.base.method.Variable;
import net.auoeke.reflect.generator.base.type.ConcreteType;
import java.util.stream.Stream;
import net.auoeke.reflect.generator.base.method.statement.AssignmentStatement;
import net.auoeke.reflect.generator.base.method.statement.Statement;

public record StoreVariable(Variable variable, Expression expression) implements StatementExpression {
    @Override
    public Statement statement() {
        return new AssignmentStatement(this);
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.expression.referencedTypes();
    }

    @Override
    public String toString() {
        return "%s = %s".formatted(this.variable.name(), this.expression);
    }
}