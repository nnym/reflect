package net.auoeke.reflect.generator.base.method.expression.literal;

import net.auoeke.reflect.generator.base.method.expression.Expression;

public record StringLiteral(String string) implements Expression, Literal {
    @Override
    public String toString() {
        return '"' + this.string + '"';
    }
}
