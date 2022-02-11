package reflect.ast.base.method.expression;

import reflect.ast.base.method.Variable;

public record LoadVariable(Variable variable) implements Expression {
    @Override
    public String toString() {
        return this.variable.name();
    }
}
