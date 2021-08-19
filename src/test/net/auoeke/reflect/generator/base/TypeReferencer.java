package net.auoeke.reflect.generator.base;

import java.util.stream.Stream;
import net.auoeke.reflect.generator.base.type.ConcreteType;

public interface TypeReferencer {
    default Stream<ConcreteType> referencedTypes() {
        return Stream.empty();
    }
}
