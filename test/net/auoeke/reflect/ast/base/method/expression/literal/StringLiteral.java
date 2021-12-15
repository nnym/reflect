package net.auoeke.reflect.ast.base.method.expression.literal;

import net.auoeke.reflect.ast.base.method.expression.Expression;

public record StringLiteral(String string) implements Expression, Literal {
    @Override
    public String toString() {
        return '"' + this.string + '"';
    }
}
