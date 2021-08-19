package net.auoeke.reflect.generator.base.method.statement;

import net.auoeke.reflect.generator.base.TypeReferencer;
import java.util.stream.Stream;

public interface Statement extends TypeReferencer {
    default Stream<String> lines() {
        return this.toString().lines();
    }
}
