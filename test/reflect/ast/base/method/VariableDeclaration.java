package reflect.ast.base.method;

import java.util.stream.Stream;
import reflect.ast.base.Node;
import reflect.ast.base.method.expression.Expression;
import reflect.ast.base.method.statement.Statement;
import reflect.ast.base.type.ConcreteType;
import reflect.ast.base.type.Type;
import reflect.util.Util;

public class VariableDeclaration implements Statement {
    protected Type type;
    protected String name;
    protected Expression initializer;

    public VariableDeclaration type(Class<?> type) {
        return this.type(new ConcreteType(type));
    }

    public VariableDeclaration type(Type type) {
        this.type = type;

        return this;
    }

    public VariableDeclaration name(String name) {
        this.name = name;

        return this;
    }

    public VariableDeclaration initialize(Expression initializer) {
        this.initializer = initializer;

        return this;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.concat(Stream.of(this.type), Stream.ofNullable(this.initializer)).flatMap(Node::referencedTypes);
    }

    @Override
    public String toString() {
        return Util.buildString(string -> {
            if (this.type == null && this.initializer == null) {
                throw new IllegalArgumentException("no initializer provided for var");
            }

            string.append(this.type == null ? "var" : this.type).append(' ').append(this.name);

            if (this.initializer != null) {
                string.append(" = ").append(this.initializer);
            }

            string.append(';');
        });
    }

    protected Variable variable() {
        return new Variable(this.type, this.name);
    }
}
