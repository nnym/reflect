package net.auoeke.reflect.ast.base.template;

import java.util.function.Function;
import net.auoeke.reflect.ast.base.Node;

public interface Identifiable<T extends Identifiable<T>> extends Node<T> {
    T nameTemplate(String pattern, Function<?, String>... transformers);
}
