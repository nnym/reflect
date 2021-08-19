package net.auoeke.reflect.generator.base.method;

import net.auoeke.reflect.generator.base.MethodBuilder;
import net.auoeke.reflect.generator.base.method.expression.StatementExpression;
import net.auoeke.reflect.generator.base.type.ConcreteType;
import net.auoeke.reflect.generator.base.method.expression.Expression;

public class MethodBlock extends Block {
    protected final MethodBuilder method;

    public MethodBlock(MethodBuilder method) {
        this.method = method;
    }

    @Override
    public Block ret(Expression expression) {
        if (expression instanceof StatementExpression && this.method.returnType().equals(ConcreteType.voidType)) {
            return this.statement((StatementExpression) expression);
        }

        return super.ret(expression);
    }

    @Override
    public Variable var(String name) {
        Variable variable = super.var(name);

        return variable == null ? this.method.parameter(name) : variable;

    }
}
