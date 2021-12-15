package net.auoeke.reflect.ast.base.method.statement;

import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.Node;
import net.auoeke.reflect.ast.base.method.expression.Expression;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import net.auoeke.reflect.util.Util;

public class LoopVariable implements Node {
    protected String name;
    protected Expression initializer;

    public LoopVariable name(String name) {
        this.name = name;

        return this;
    }

    public LoopVariable initialize(Expression initializer) {
        this.initializer = initializer;

        return this;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.initializer == null ? Stream.empty() : this.initializer.referencedTypes();
    }

    @Override
    public String toString() {
        return Util.buildString(this.name, string -> {
            if (this.initializer == null) {
                string.append(';');
            } else {
                string.append(" = ").append(this.initializer);
            }
        });
    }
}
