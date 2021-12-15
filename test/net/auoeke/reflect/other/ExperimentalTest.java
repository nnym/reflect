package net.auoeke.reflect.other;

import java.lang.reflect.Field;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.experimental.ReflectionFactory;
import net.auoeke.reflect.util.Logger;
import net.auoeke.reflect.util.Retainable;
import net.auoeke.reflect.util.Util;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings({"ResultOfMethodCallIgnored", "RedundantSuppression"})
@Disabled
@Testable
public class ExperimentalTest {
    @Retainable
    static long test2 = 0xFF0000000BL;
    static int test = 2;
    static int test1 = 97;
    byte byte0;
    byte byte1;
    float instance = 54.2F;

    @Test
    void trustedFinal() {
        Logger.log(Accessor.get(Fields.of(String.class, "value"), "trustedFinal"));
    }

    @Test
    void factory() {
        Field field = ReflectionFactory.field(ExperimentalTest.class, int.class, Flags.STATIC, 0);
        assert Accessor.getInt(field) == 0xB;
        assert Accessor.getLong(field) == 0xFF0000000BL;

        field = ReflectionFactory.field(ExperimentalTest.class, int.class, Flags.STATIC, 1);
        assert Accessor.getInt(field) == 2;

        field = ReflectionFactory.field(ExperimentalTest.class, int.class, Flags.STATIC, 2);
        assert Accessor.getInt(field) == 97;

        Accessor.put(field, 23);
        assert test1 == 23;

        Util.bp();
    }
}
