package test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jol.vm.VM;
import reflect.misc.TestObject;

@Testable
public class AccessorTests extends Accessor {
	@Test public void testCopy() {
		var one = new TestObject();
		var two = new TestObject();
		var fields = Fields.instanceOf(AccessorTests.class).toList();

		Methods.of(Accessor.class).map(Method::getName).filter(name -> name.startsWith("copy")).forEach(name -> {
			var typeName = name.substring(name.indexOf('y') + 1).toLowerCase();

			fields.forEach(field -> {
				var fieldTypeName = field.getType().getSimpleName().toLowerCase();

				if (fieldTypeName.equals(typeName)) {
					copyObject(one, two, field);
				} else if (fieldTypeName.endsWith("volatile")) {
					copyObjectVolatile(one, two, field);
				}
			});
		});
	}

	@Test void accessor() {
		assert getIntVolatile(0, "value") == 0 && getInt(0, "value") == 0;

		class A {
			final byte end = 123;
		}

		assert get(new A(), "end").equals((byte) 123);
		assert getVolatile(new A(), "end").equals((byte) 123);

		var obj = new Object() {
			int field = 22;
			String thing = "asd";
			Object object = new Object() {};
			List<Object> things = Arrays.asList(23, 46, "12");
		};

		obj.field = 84;
		obj.thing = "hjk";
		obj.object = null;
		obj.things = new ArrayList<>();

		var clone = clone(obj);
		assert clone.field == 84;
		assert "hjk".equals(clone.thing);
		assert clone.object == null;
		assert clone.things instanceof ArrayList<Object> things && things.isEmpty();
	}
}
