package experimental;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Types2 {
	public static Stream<Class<?>> lowerBounds(Type type) {
		if (type instanceof Class<?> clas) {
			return Stream.of(clas);
		}

		if (type instanceof WildcardType wildcard) {
			return Stream.of(wildcard.getLowerBounds()).flatMap(Types2::lowerBounds);
		}

		if (type instanceof ParameterizedType parameterized) {
			return lowerBounds(parameterized.getRawType());
		}

		if (type instanceof TypeVariable<?> variable) {
			return Stream.of(variable.getBounds()).flatMap(Types2::lowerBounds);
		}

		if (type instanceof GenericArrayType array) {
			return lowerBounds(array.getGenericComponentType()).map(Class::arrayType);
		}

		throw new IllegalArgumentException("%s %s".formatted(type.getClass(), type));
	}

	public static boolean isAssignableFrom(Type a, Type b) {
		var aBounds = lowerBounds(a).toArray(Class<?>[]::new);
		var bBounds = lowerBounds(b).toArray(Class<?>[]::new);
		return aBounds.length == bBounds.length && IntStream.range(0, aBounds.length).allMatch(index -> aBounds[index].isAssignableFrom(bBounds[index]));
	}
}
