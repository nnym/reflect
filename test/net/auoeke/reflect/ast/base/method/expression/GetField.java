package net.auoeke.reflect.ast.base.method.expression;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.Node;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import net.auoeke.reflect.ast.base.util.Union;
import net.auoeke.reflect.util.Util;

public class GetField implements Expression {
    protected final Union<Node, ConcreteType, Expression> owner = new Union<>(ConcreteType.class, Expression.class);
    protected String name;

    protected boolean instance;

    public GetField() {}

    public GetField(Field field) {
        this.of(field.getDeclaringClass()).name(field.getName());
    }

    public GetField of(Class<?> type) {
        return this.of(new ConcreteType(type));
    }

    public GetField of(ConcreteType type) {
        this.owner.set(type);

        return this;
    }

    public GetField of(Expression object) {
        this.instance = true;
        this.owner.set(object);

        return this;
    }

    public GetField name(String name) {
        this.name = name;

        return this;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.owner.get().referencedTypes();
    }

    @Override
    public String toString() {
        return Util.buildString(string -> {
            if (this.instance) {
                string.append(Objects.requireNonNullElse(this.owner.get(), "this")).append('.');
            } else if (this.owner.present()) {
                string.append(this.owner.get()).append('.');
            }

            string.append(this.name);
        });
    }
}
