package reflect.ast.base.type;

import java.util.stream.Stream;
import reflect.ast.base.Node;
import reflect.util.Util;

public class TypeArgument implements Node {
    public static final TypeArgument wildcard = new TypeArgument();

    protected final Type bound;
    protected final BoundType type;

    private TypeArgument() {
        this.bound = null;
        this.type = null;
    }

    public TypeArgument(Type bound, BoundType type) {
        if (bound == null) {
            throw new IllegalArgumentException("use TypeArgument.wildcard");
        }

        this.bound = bound;
        this.type = type;
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return this.bound == null ? Stream.empty() : this.bound.referencedTypes();
    }

    @Override
    public String toString() {
        return this.bound == null ? "?" : Util.buildString(string -> {
            if (this.type != null) {
                string.append('?').append(this.type).append(' ');
            }

            string.append(this.bound);
        });
    }
}
