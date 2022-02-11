package reflect.ast.base.method.statement;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import reflect.ast.base.Node;
import reflect.ast.base.method.Variable;
import reflect.ast.base.method.expression.Expression;
import reflect.ast.base.type.ConcreteType;
import reflect.ast.base.type.Type;
import reflect.util.Util;

public class EnhancedFor implements Statement {
    protected Variable variable;
    protected Expression iterable;
    protected Statement action;

    public EnhancedFor var(Type type, String name) {
        this.variable = new Variable(type, name);

        return this;
    }

    public EnhancedFor var(Class<?> type, String name) {
        return this.var(new ConcreteType(type), name);
    }

    public EnhancedFor iterable(Expression iterable) {
        this.iterable = iterable;

        return this;
    }

    public EnhancedFor action(Statement action) {
        this.action = action;

        return this;
    }

    public EnhancedFor action(Function<Variable, Statement> action) {
        return this.action(action.apply(this.variable));
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this.variable, this.iterable, this.action).filter(Objects::nonNull).flatMap(Node::referencedTypes);
    }

    @Override
    public String toString() {
        return Util.buildString("for (%s : %s)".formatted(this.variable.declaration(), this.iterable), string -> {
            if (this.action == null) {
                string.append(';');
            } else {
                string.append(' ').append(this.action);
            }
        });
    }
}
