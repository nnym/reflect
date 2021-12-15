package net.auoeke.reflect.ast.base.type;

import java.util.stream.Stream;
import net.auoeke.reflect.util.Util;

public record TypeParameter(String name, BoundType boundType, Type bound) implements Type {
    public String declaration() {
        return Util.buildString(string -> {
            string.append(this.name);

            if (this.bound != null) {
                string.append(' ').append(this.boundType).append(' ').append(this.bound);
            }
        });
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.bound == null ? Stream.empty() : this.bound.referencedTypes();
    }
}
