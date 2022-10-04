package reflect.ast.base.method.statement;

import java.util.stream.Stream;
import reflect.ast.base.method.expression.Invocation;
import reflect.ast.base.type.ConcreteType;

public record InvocationStatement(Invocation invocation) implements Statement {
	@Override
	public Stream<ConcreteType> referencedTypes() {
		return this.invocation.referencedTypes();
	}

	@Override
	public String toString() {
		return this.invocation.toString() + ';';
	}
}
