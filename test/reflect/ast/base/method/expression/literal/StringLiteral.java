package reflect.ast.base.method.expression.literal;

import reflect.ast.base.method.expression.Expression;

public record StringLiteral(String string) implements Expression, Literal {
	@Override
	public String toString() {
		return '"' + this.string + '"';
	}
}
