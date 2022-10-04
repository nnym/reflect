package reflect.ast.base.type;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import reflect.ast.base.Node;

public record ParameterizedType(ConcreteType type, TypeArgument... typeArguments) implements Type {
	public ParameterizedType {
		if (typeArguments.length == 0) {
			throw new IllegalArgumentException("parameterized type must have type arguments");
		}
	}

	@Override
	public String toString() {
		return this.type + Stream.of(this.typeArguments).map(TypeArgument::toString).collect(Collectors.joining(", ", "<", ">"));
	}

	@Override
	public Stream<ConcreteType> referencedTypes() {
		return Stream.concat(Stream.of(this.type), Stream.of(this.typeArguments).flatMap(Node::referencedTypes));
	}
}
