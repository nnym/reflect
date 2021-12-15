package test;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static net.auoeke.reflect.Flags.*;

@Testable
public final class FlagsTests {
    @Test
    void flags() throws Throwable {
        assert isPublic(Object.class);
        assert all(Object.class, PUBLIC);
        assert all(FlagsTests.class, PUBLIC | FINAL);
        assert none(FlagsTests.class, PRIVATE | PROTECTED | ENUM | INTERFACE | ANNOTATION);
        assert any(FlagsTests.class, PUBLIC | ENUM | INTERFACE | ANNOTATION | PRIVATE | PROTECTED);
        assert any(FlagsTests.class, FINAL | ENUM | INTERFACE | ANNOTATION | PRIVATE | PROTECTED);
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
