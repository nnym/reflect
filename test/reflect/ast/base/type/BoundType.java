package reflect.ast.base.type;

import java.util.Locale;

public enum BoundType {
    EXTENDS,
    SUPER;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
