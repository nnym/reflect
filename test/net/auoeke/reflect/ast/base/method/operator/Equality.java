package net.auoeke.reflect.ast.base.method.operator;

import net.auoeke.reflect.ast.base.Node;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.method.expression.Expression;
import net.auoeke.reflect.ast.base.method.expression.literal.Literal;

public class Equality implements Expression {
    protected Expression left;
    protected Expression right;

    public Equality() {}

    public Equality(Expression left, Expression right) {
        this.left(left).right(right);
    }

    public static Equality nul(Expression expression) {
        return new Equality().left(expression).right(Literal.null$);
    }

    public Equality left(Expression left) {
        this.left = left;

        return this;
    }

    public Equality right(Expression right) {
        this.right = right;

        return this;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this.left, this.right).flatMap(Node::referencedTypes);
    }

    @Override
    public String toString() {
        return "%s == %s".formatted(this.left, this.right);
    }
}
