package user11681.reflect.generator.base.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import user11681.reflect.generator.base.type.Type;

public class Body implements Statement {
    protected List<Statement> statements = new ArrayList<>();
    protected Map<String, Variable> variables = new HashMap<>();

    public Body statement(Statement statement) {
        this.statements.add(statement);

        return this;
    }

    public Body statement(ExpressionStatement statement) {
        return this.statement(statement.statement());
    }

    public Body ret(Expression expression) {
        return this.statement(new ReturnStatement(expression));
    }

    public Body newline() {
        this.statements.add(new EmptyStatement());

        return this;
    }

    public Body variable(Class<?> type, String name, Expression initializer) {
        return this.variable(new ConcreteType(type), name, initializer);
    }

    public Body variable(Type type, String name, Expression initializer) {
        return this.variable(variable -> variable.type(type).name(name).initialize(initializer));
    }

    public Body variable(Consumer<VariableDeclaration> consumer) {
        VariableDeclaration declaration = new VariableDeclaration();
        consumer.accept(declaration);

        if (this.variable(declaration.name) != null) {
            throw new IllegalStateException("variable \"%s\" already exists".formatted(declaration.name));
        }

        this.variables.put(declaration.name, declaration.variable());
        this.statements.add(declaration);

        return this;
    }

    public Variable variable(String name) {
        return this.variables.get(name);
    }

    @Override
    public String toString() {
        return this.statements.stream().flatMap(Statement::lines).collect(Collectors.joining("\n    ", "{\n    ", "\n}"));
    }
}
