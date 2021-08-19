package net.auoeke.reflect.generator.base.method;

import net.auoeke.reflect.generator.base.TypeReferencer;
import java.util.Objects;
import java.util.stream.Stream;
import net.auoeke.reflect.generator.base.method.expression.Expression;
import net.auoeke.reflect.generator.base.method.expression.LoadVariable;
import net.auoeke.reflect.generator.base.method.expression.StoreVariable;
import net.auoeke.reflect.generator.base.type.ConcreteType;
import net.auoeke.reflect.generator.base.type.Type;

public record Variable(Type type, String name) implements Expression, TypeReferencer {
    public LoadVariable load() {
        return new LoadVariable(this);
    }

    public StoreVariable store(Expression expression) {
        return new StoreVariable(this, expression);
    }

    public String declaration() {
        return "%s %s".formatted(this.type, this.name);
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.type.referencedTypes();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Variable variable) {
            return Objects.equals(this.type, variable.type) && Objects.equals(this.name, variable.name);
        }

        return false;

    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
