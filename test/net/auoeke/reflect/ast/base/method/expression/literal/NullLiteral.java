package net.auoeke.reflect.ast.base.method.expression.literal;

public class NullLiteral implements Literal {
    public static final NullLiteral instance = new NullLiteral();

    private NullLiteral() {}

    @Override
    public String toString() {
        return "null";
    }
}
