package net.auoeke.reflect.ast.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.method.expression.Expression;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import net.auoeke.reflect.ast.base.type.Type;
import net.auoeke.reflect.util.Util;

public class FieldBuilder extends MemberBuilder<FieldBuilder> {
    protected Type type;
    protected Expression initializer;

    public FieldBuilder type(Type type) {
        this.type = type;

        return this;
    }

    public FieldBuilder type(Class<?> type) {
        return this.type(new ConcreteType(type));
    }

    @Override
    public FieldBuilder name(String name) {
        this.name = name;

        return this;
    }

    public FieldBuilder initialize(Expression initializer) {
        this.initializer = initializer;

        return this;
    }

    public FieldBuilder inherit(Field field) {
        this.access(field.getModifiers());
        this.type(field.getType());
        this.name(field.getName());

        return this;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Stream.of(this.type, this.initializer).flatMap(Node::referencedTypes);
    }

    @Override
    public String toString() {
        return Util.buildString(Modifier.toString(this.access), string -> {
            string.append(' ').append(this.type).append(' ').append(this.name);

            if (this.initializer != null) {
                string.append(" = ").append(this.initializer);
            }

            string.append(';');
        });
    }
}
