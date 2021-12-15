package net.auoeke.reflect.ast.base.type;

import java.util.Arrays;
import java.util.stream.Stream;
import net.auoeke.reflect.ast.base.Node;

public interface Type extends Node {
    static ConcreteType of(Class<?> type) {
        return new ConcreteType(type);
    }

    static ParameterizedType parameterized(ConcreteType type, TypeArgument... arguments) {
        return new ParameterizedType(type, arguments);
    }

    static ParameterizedType parameterized(Class<?> type, TypeArgument... arguments) {
        return new ParameterizedType(of(type), arguments);
    }

    static ParameterizedType parameterized(Class<?> type, Type... arguments) {
        return parameterized(type, Stream.of(arguments).map(arg -> new TypeArgument(arg, null)).toArray(TypeArgument[]::new));
    }

    static ParameterizedType parameterized(Class<?> type, Class<?>... arguments) {
        return parameterized(type, Stream.of(arguments).map(arg -> new TypeArgument(of(arg), null)).toArray(TypeArgument[]::new));
    }

    static ParameterizedType wildcard(Class<?> type) {
        var arguments = new TypeArgument[type.getTypeParameters().length];
        Arrays.fill(arguments, TypeArgument.wildcard);

        return parameterized(type, arguments);
    }
}
