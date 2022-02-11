package reflect.ast.base;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.auoeke.reflect.Flags;
import reflect.ast.base.template.Discriminator;
import reflect.ast.base.template.Identifiable;

public abstract class MemberBuilder<T extends MemberBuilder<T>> implements Identifiable<T> {
    protected int access;
    protected String name;
    protected BiConsumer<Map<String, Discriminator<?>>, T> instantiator;

    @Override
    public T template(BiConsumer<Map<String, Discriminator<?>>, T> instantiator) {
        this.instantiator = (this.instantiator == null ? instantiator : this.instantiator.andThen(instantiator));

        return (T) this;
    }

    @Override
    public T nameTemplate(String pattern, Function<?, String>... transformers) {
        return null;
    }

    public T public$() {
        return this.access(Flags.PUBLIC);
    }

    public T protected$() {
        return this.access(Flags.PROTECTED);
    }

    public T private$() {
        return this.access(Flags.PRIVATE);
    }

    public T static$() {
        return this.access(Flags.STATIC);
    }

    public T abstract$() {
        return this.access(Flags.ABSTRACT);
    }

    public T final$() {
        return this.access(Flags.FINAL);
    }

    public T access(int... access) {
        this.access |= IntStream.of(access).reduce(0, (left, right) -> left | right);

        return (T) this;
    }

    public String name() {
        return this.name;
    }

    public T name(String name) {
        this.name = name;

        return (T) this;
    }

    public String accessString() {
        return Modifier.toString(this.access);
    }

    public String offsetAccessString() {
        return this.access == 0 ? "" : this.accessString() + ' ';
    }
}
