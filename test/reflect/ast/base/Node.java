package reflect.ast.base;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import reflect.ast.base.template.Discriminator;
import reflect.ast.base.type.ConcreteType;

public interface Node<T extends Node<T>> {
    default T template(BiConsumer<Map<String, Discriminator<?>>, T> instantiator) {
        throw new UnsupportedOperationException();
    }

    /**
     @param instantiator
     @return
     */
    default T template(Consumer<T> instantiator) {
        this.template((discriminators, node) -> instantiator.accept(node));

        return (T) this;
    }

    /**
     @return a stream of nodes that will supplant this node after its instantiation.
     */
    default Stream<T> instantiate() {
        return null;
    }

    /**
     @return a stream of the types (if any) referenced by this node or any of its children.
     */
    default Stream<ConcreteType> referencedTypes() {
        return Stream.empty();
    }
}
