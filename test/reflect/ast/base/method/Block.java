package reflect.ast.base.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reflect.ast.base.Node;
import reflect.ast.base.method.expression.Expression;
import reflect.ast.base.method.expression.StatementExpression;
import reflect.ast.base.method.statement.EmptyStatement;
import reflect.ast.base.method.statement.If;
import reflect.ast.base.method.statement.Return;
import reflect.ast.base.method.statement.Statement;
import reflect.ast.base.type.ConcreteType;
import reflect.ast.base.type.Type;

public class Block implements Statement {
	protected List<Statement> statements = new ArrayList<>();
	protected Map<String, Variable> variables = new HashMap<>();

	public static Block wrap(Statement statement) {
		return new Block().statement(statement);
	}

	public static Block wrap(StatementExpression statement) {
		return new Block().statement(statement);
	}

	public Block statement(Statement statement) {
		this.statements.add(statement);

		return this;
	}

	public Block statement(StatementExpression statement) {
		return this.statement(statement.statement());
	}

	public Block return$(Expression expression) {
		return this.statement(new Return(expression));
	}

	public Block newline() {
		this.statements.add(new EmptyStatement());

		return this;
	}

	public Block var(Consumer<VariableDeclaration> consumer) {
		var declaration = new VariableDeclaration();
		consumer.accept(declaration);

		if (this.var(declaration.name) != null) {
			throw new IllegalStateException("variable \"%s\" already exists".formatted(declaration.name));
		}

		this.variables.put(declaration.name, declaration.variable());
		this.statements.add(declaration);

		return this;
	}

	public Block var(Type type, String name, Expression initializer) {
		return this.var(variable -> variable.type(type).name(name).initialize(initializer));
	}

	public Block var(Class<?> type, String name, Expression initializer) {
		return this.var(new ConcreteType(type), name, initializer);
	}

	public Block var(String name, Expression initializer) {
		return this.var((Type) null, name, initializer);
	}

	public Variable var(String name) {
		return this.variables.get(name);
	}

	public Block if$(Consumer<If> consumer) {
		var if$ = new If();
		consumer.accept(if$);
		this.statement(if$);

		return this;
	}

	@Override
	public Stream<ConcreteType> referencedTypes() {
		return Stream.concat(this.statements.stream(), this.variables.values().stream()).flatMap(Node::referencedTypes);
	}

	@Override
	public String toString() {
		return this.statements.isEmpty() ? "{}" : this.statements.stream().flatMap(Statement::lines).map(line -> "\n    " + line).collect(Collectors.joining("", "{", "\n}"));
	}
}
