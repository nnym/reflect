package reflect.ast.base.method.statement;

import java.util.Objects;
import java.util.stream.Stream;
import reflect.ast.base.Node;
import reflect.ast.base.method.expression.Expression;
import reflect.ast.base.method.operator.Equality;
import reflect.ast.base.type.ConcreteType;
import reflect.util.Util;

public class If implements Statement {
	protected Expression condition;
	protected Statement then;
	protected Statement else$;

	public static If chain(Stream<If> stream) {
		var iterator = stream.iterator();
		var if$ = iterator.next();
		var else$ = if$;

		while (iterator.hasNext()) {
			else$.else$(else$ = iterator.next());
		}

		return if$;
	}

	public static If chain(Stream<If> stream, Statement else$) {
		return chain(stream).else$(else$);
	}

	public If condition(Expression condition) {
		this.condition = condition;

		return this;
	}

	public If identical(Expression left, Expression right) {
		return this.condition(new Equality(left, right));
	}

	public If null$(Expression expression) {
		return this.condition(Equality.nul(expression));
	}

	public If then(Statement statement) {
		this.then = statement;

		return this;
	}

	public If else$() {
		var else$ = new If();
		this.else$(else$);

		return else$;
	}

	public If else$(Statement statement) {
		this.else$ = statement;

		return this;
	}

	public If elseIf(Expression condition) {
		return new If().condition(condition);
	}

	@Override
	public Stream<ConcreteType> referencedTypes() {
		return Stream.of(this.condition, this.then, this.else$).filter(Objects::nonNull).flatMap(Node::referencedTypes);
	}

	@Override
	public String toString() {
		return Util.buildString(string -> {
			string.append("if (%s) %s".formatted(this.condition, this.then));

			if (this.else$ != null) {
				string.append(" else ").append(this.else$);
			}
		});
	}
}
