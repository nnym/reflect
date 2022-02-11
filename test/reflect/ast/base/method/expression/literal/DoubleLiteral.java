package reflect.ast.base.method.expression.literal;

import reflect.util.Util;

public record DoubleLiteral(double value) implements Literal {
    public DoubleLiteral {
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("double literal cannot be infinity");
        }

        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("double literal cannot be NaN");
        }
    }

    @Override
    public String toString() {
        return Util.buildString(this.value, string -> {
            if (this.value % 1 != 0) {
                string.append('D');
            }
        });
    }
}
