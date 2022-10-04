package test;

import net.auoeke.reflect.Flags;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public final class FlagsTests extends Flags {
	@Test
	void flags() {
		Runnable runnable = () -> {};

		assert isPublic(Object.class);
		assert all(Object.class, PUBLIC);
		assert all(FlagsTests.class, PUBLIC | FINAL);
		assert none(FlagsTests.class, PRIVATE | PROTECTED | ENUM | INTERFACE | ANNOTATION);
		assert any(FlagsTests.class, PUBLIC | ENUM | INTERFACE | ANNOTATION | PRIVATE | PROTECTED);
		assert any(FlagsTests.class, FINAL | ENUM | INTERFACE | ANNOTATION | PRIVATE | PROTECTED);
		assert isSynthetic(runnable.getClass());
		assert remove(VISIBILITY, PRIVATE) == (PROTECTED | PUBLIC);
		assert isPackagePrivate(FlagsTests.class.getDeclaredMethod("flags"));
		assert isPackagePrivate(PackagePrivate.class);
		assert isStatic(PackagePrivate.class);
		assert !isStatic(Object.class);
		assert !isPackagePrivate(Object.class);
		assert string(FlagsTests.class).equals("public final");
	}

	static class PackagePrivate {}
}
