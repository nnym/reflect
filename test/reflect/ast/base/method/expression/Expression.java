package reflect.ast.base.method.expression;

import reflect.ast.base.Node;
import reflect.ast.base.type.Type;

public interface Expression extends Node {
	default Invocation invoke() {
		return new Invocation().owner(this);
	}

	default Invocation invoke(String method) {
		return this.invoke().name(method);
	}

	default Expression cast(Type type) {
		return new CastExpression(this, type);
	}

	default Expression cast(Class<?> type) {
		return this.cast(Type.of(type));
	}
}
