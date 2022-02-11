package reflect.ast.base.method.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reflect.experimental.Copyable;
import reflect.ast.base.Node;
import reflect.ast.base.method.statement.InvocationStatement;
import reflect.ast.base.method.statement.Statement;
import reflect.ast.base.type.ConcreteType;
import reflect.util.Util;

public class Invocation implements StatementExpression, Copyable<Invocation> {
    protected boolean instance;
    protected Expression object;
    protected ConcreteType type;
    protected String name;
    protected ArrayList<Expression> arguments = new ArrayList<>();

    public Invocation() {}

    public Invocation(String name) {
        this.name(name);
    }

    public Invocation(Class<?> owner) {
        this.owner(owner);
    }

    public Invocation(Class<?> owner, String name) {
        this.owner(owner).name(name);
    }

    public Invocation(ConcreteType owner, String name) {
        this.owner(owner).name(name);
    }

    public Invocation instance(boolean instance) {
        this.instance = instance;

        return this;
    }

    public Invocation owner(Expression owner) {
        this.object = owner;
        this.instance = true;

        return this;
    }

    public Invocation owner(ConcreteType owner) {
        this.type = owner;

        return this;
    }

    public Invocation owner(Class<?> owner) {
        return this.owner(new ConcreteType(owner));
    }

    public Invocation name(String name) {
        this.name = name;

        return this;
    }

    public Invocation argument(Expression... arguments) {
        Collections.addAll(this.arguments, arguments);

        return this;
    }

    @Override
    public Statement statement() {
        return new InvocationStatement(this);
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.concat(Stream.ofNullable(this.instance ? this.object : this.type), this.arguments.stream()).flatMap(Node::referencedTypes);
    }

    @Override
    public void copy(Invocation copy) {
        copy.arguments = (ArrayList<Expression>) this.arguments.clone();
    }

    @Override
    public String toString() {
        return Util.buildString(string -> {
            if (this.instance) {
                string.append(Objects.requireNonNullElse(this.object, "this")).append('.');
            } else if (this.type != null) {
                string.append(this.type).append('.');
            }

            string.append(this.name).append("(").append(this.arguments.stream().map(Expression::toString).collect(Collectors.joining(", "))).append(')');
        });
    }
}
