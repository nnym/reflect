package test;

import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Pointer;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.misc.A;

@Testable
public class PointerTests extends Pointer {
	public PointerTests() {
		super(null, 0, -1);
	}

	@SuppressWarnings("WrapperTypeMayBePrimitive")
	@Test public void pointer() {
		var a = new A();
		a.yes = 446;
		Double yes = a.yes;
		var pointer = of(a, "yes");
		pointer.putDouble(pointer.getDouble() + 4);
		Accessor.putDouble(yes, "value", yes + 4);

		assert yes == (double) pointer.get();
	}

	@Test public void typeTest() {
		@SuppressWarnings("unused")
		class Test {
			static final short a = 12, b = Short.MAX_VALUE;
			static final double d = 123.4;
			static Object o;
		}

		var pointer = of(Test.class, "a");
		assert pointer.type == SHORT && pointer.get() instanceof Short s && s == 12;
		assert pointer.staticField("b").get() instanceof Short s && s == Short.MAX_VALUE;

		pointer = pointer.staticField("d");
		assert pointer.type == DOUBLE && pointer.get() instanceof Double d && d == 123.4;

		pointer.put(99.9);
		assert (double) pointer.get() == 99.9;

		pointer = pointer.staticField("o");
		assert pointer.get() == null;
		pointer.put(new PointerTests());
		assert pointer.get() instanceof PointerTests;
	}
}
