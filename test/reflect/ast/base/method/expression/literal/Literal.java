package reflect.ast.base.method.expression.literal;

import reflect.ast.base.method.expression.Expression;

public interface Literal extends Expression {
	NullLiteral null$ = NullLiteral.instance;

	static IntLiteral of(int value) {
		return new IntLiteral(value);
	}

	static LongLiteral of(long value) {
		return new LongLiteral(value);
	}

	static FloatLiteral of(float value) {
		return new FloatLiteral(value);
	}

	static DoubleLiteral of(double value) {
		return new DoubleLiteral(value);
	}

	static StringLiteral of(String string) {
		return new StringLiteral(string);
	}

	static TypeLiteral of(Class<?> type) {
		return new TypeLiteral(type);
	}
}
