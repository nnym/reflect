package net.auoeke.reflect.ast.base.method.statement;

import net.auoeke.reflect.ast.base.Node;
import java.util.stream.Stream;

public interface Statement extends Node {
    default Stream<String> lines() {
        return this.toString().lines();
    }
}
