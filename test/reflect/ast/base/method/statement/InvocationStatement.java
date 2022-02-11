package reflect.ast.base.method.statement;

import reflect.ast.base.type.ConcreteType;
import java.util.stream.Stream;
import reflect.ast.base.method.expression.Invocation;

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
