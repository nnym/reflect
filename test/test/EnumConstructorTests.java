package test;

import java.lang.annotation.RetentionPolicy;
import net.auoeke.reflect.EnumConstructor;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.misc.Enumeration;
import reflect.util.Util;

@Testable
class EnumConstructorTests {
	@Test
	public void enumeration() {
		var retentionPolicyConstructor = EnumConstructor.constructor(0L, RetentionPolicy.class);
		var enumerationConstructor = EnumConstructor.constructor(Enumeration.class, 0D);

		assert EnumConstructor.construct(Enumeration.class, "TEST", 1).ordinal() == 0;
		assert EnumConstructor.construct(Enumeration.class, 0, "TEST", 553D).test == 553;
		assert EnumConstructor.construct(0L, Enumeration.class, "TEST", 4D).test == 4;
		assert EnumConstructor.construct(0L, Enumeration.class, 1, "TEST", 9023D).test == 9023;

		var enumeration = EnumConstructor.construct(Enumeration.class, 2, "TEST", 2D);
		assert enumeration != null && enumeration.test == 2;

		var expectedLength = new Object() {
			int length = 0;
		};

		Runnable verifySize = () -> {
			assert Enumeration.values().length == expectedLength.length++;
		};
		verifySize.run();

		var t0 = EnumConstructor.add(Enumeration.class, "TEST", 1D);
		verifySize.run();

		var t1 = EnumConstructor.add(Enumeration.class, 0, "TEST", 3D);
		verifySize.run();

		var t2 = EnumConstructor.add(0L, Enumeration.class, "TEST", 4D);
		verifySize.run();

		var t3 = EnumConstructor.add(0L, Enumeration.class, 1, "TEST", 5D);
		verifySize.run();

		var t4 = EnumConstructor.add(Enumeration.class, enumeration);
		verifySize.run();

		var constructor = EnumConstructor.get(RetentionPolicy.class);
		Util.repeat(4, () -> constructor.add("TEST"));
		Enumeration.valueOf("TEST");
	}
}
