package net.auoeke.reflect.generator.base.method.expression.literal;

import net.auoeke.reflect.generator.base.method.expression.Expression;

public class NullLiteral implements Expression, Literal {
    public static final NullLiteral instance = new NullLiteral();

    private NullLiteral() {}

    @Override
    public String toString() {
        return "null";
    }
}
