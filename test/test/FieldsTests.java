package test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.auoeke.reflect.Fields;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.misc.A;
import reflect.misc.B;
import reflect.misc.C;

@Testable
public class FieldsTests extends Fields {
	@Test void directTest() {
		assert Fields.of(Class.forName("jdk.internal.reflect.Reflection")).findAny().isPresent();
	}

	@Test void allFields() {
		assert Stream.of(A.class, B.class, C.class).allMatch(Fields.all(C.class).collect(HashMap::new, (map, field) -> map.computeIfAbsent(field.getDeclaringClass(), type -> new HashMap<>()), Map::putAll)::containsKey);
	}
}
