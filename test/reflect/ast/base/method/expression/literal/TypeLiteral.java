package reflect.ast.base.method.expression.literal;

import reflect.ast.base.type.ConcreteType;
import java.util.stream.Stream;
import reflect.ast.base.method.expression.Expression;

public record TypeLiteral(ConcreteType type) implements Expression, Literal {
    public TypeLiteral(Class<?> type) {
        this(new ConcreteType(type));
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this.type);
    }

    @Override
    public String toString() {
        return this.type + ".class";
    }
}
