package reflect.ast.base.method.statement;

import reflect.ast.base.method.expression.StoreVariable;
import reflect.ast.base.type.ConcreteType;
import java.util.stream.Stream;

public record AssignmentStatement(StoreVariable variable) implements Statement {
    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.variable.referencedTypes();
    }

    @Override
    public String toString() {
        return this.variable.toString() + ';';
    }
}
