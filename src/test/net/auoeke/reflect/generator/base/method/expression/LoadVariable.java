package net.auoeke.reflect.generator.base.method.expression;

import net.auoeke.reflect.generator.base.method.Variable;

public record LoadVariable(Variable variable) implements Expression {
    @Override
    public String toString() {
        return this.variable.name();
    }
}
