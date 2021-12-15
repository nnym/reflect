package net.auoeke.reflect.ast.base.method;

import net.auoeke.reflect.ast.base.MethodBuilder;
import net.auoeke.reflect.ast.base.method.expression.StatementExpression;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import net.auoeke.reflect.ast.base.method.expression.Expression;

public class MethodBlock extends Block {
    protected final MethodBuilder method;

    public MethodBlock(MethodBuilder method) {
        this.method = method;
    }

    @Override
    public Block return$(Expression expression) {
        if (expression instanceof StatementExpression && this.method.return$().equals(ConcreteType.voidType)) {
            return this.statement((StatementExpression) expression);
        }

        return super.return$(expression);
    }

    @Override
    public Variable var(String name) {
        Variable variable = super.var(name);

        return variable == null ? this.method.parameter(name) : variable;

    }
}
