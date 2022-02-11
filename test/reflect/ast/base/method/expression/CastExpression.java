package reflect.ast.base.method.expression;

import reflect.ast.base.type.ConcreteType;
import reflect.ast.base.type.Type;
import java.util.stream.Stream;

public record CastExpression(Expression expression, Type type) implements Expression {
    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.type.referencedTypes();
    }

    @Override
    public String toString() {
        return "(%s) %s".formatted(this.type, this.expression);
    }
}
