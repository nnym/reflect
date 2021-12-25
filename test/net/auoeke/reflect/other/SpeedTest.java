package net.auoeke.reflect.other;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.stream.IntStream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.ReflectTest;
import net.auoeke.reflect.Types;
import net.auoeke.reflect.misc.A;
import net.auoeke.reflect.misc.TestObject;
import net.auoeke.reflect.util.Logger;
import net.auoeke.reflect.util.Util;
import net.auoeke.uncheck.ThrowingRunnable;
import net.auoeke.uncheck.Uncheck;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings("ALL")
@Disabled
@Testable
public class SpeedTest {
    static final int iterations = 100;
    static final int tests = 1;
    static Runnable runnable0;
    static Runnable runnable1;

    static long mean(ThrowingRunnable test) {
        return mean(true, null, test);
    }

    static long mean(String label, ThrowingRunnable test) {
        return mean(true, label, test);
    }

    static long mean(boolean loud, String label, ThrowingRunnable test) {
        return Uncheck.handle(() -> {
            long time = System.nanoTime();

            for (int i = 0; i < iterations; i++) {
                test.run();
            }

            long duration = Math.round((double) (System.nanoTime() - time) / iterations);

            if (loud) {
                if (label == null) {
                    Logger.log(duration);
                } else Logger.log("%s: %s", label, duration);
            }

            return duration;
        });
    }

    static long time(ThrowingRunnable test) {
        return time(true, null, test);
    }

    static long time(String label, ThrowingRunnable test) {
        return time(true, label, test);
    }

    static long time(boolean loud, ThrowingRunnable test) {
        return time(loud, null, test);
    }

    static long time(boolean loud, String label, ThrowingRunnable test) {
        return Uncheck.handle(() -> {
            long time = System.nanoTime();

            test.run();

            time = System.nanoTime() - time;

            if (loud) {
                if (label == null) {
                    Logger.log(time);
                } else Logger.log("%s: %s", label, time);
            }

            return time;
        });
    }

    static long total(ThrowingRunnable test) {
        return total(true, null, test);
    }

    static long total(String label, ThrowingRunnable test) {
        return total(true, label, test);
    }

    static long total(boolean loud, ThrowingRunnable test) {
        return total(loud, null, test);
    }

    static long total(boolean loud, String label, ThrowingRunnable test) {
        return Uncheck.handle(() -> {
            long time = System.nanoTime();

            for (int i = 0; i < iterations; i++) {
                test.run();
            }

            time = System.nanoTime() - time;

            if (loud) {
                if (label == null) {
                    Logger.log(time);
                } else Logger.log("%s: %s", label, time);
            }

            return time;
        });
    }

    @Test
    void invoker() throws Throwable {
        var object = new Object();

        long start = System.nanoTime();

        var handle = Invoker.findVirtual(Object.class, "hashCode", int.class);

        for (int i = 0; i < iterations; i++) {
            int code = (int) handle.invokeExact(object);
        }

        System.out.println(System.nanoTime() - start);
        start = System.nanoTime();

        var method = Object.class.getMethod("hashCode");

        for (int i = 0; i < iterations; i++) {
            int code = (int) method.invoke(object);
        }

        System.out.println(System.nanoTime() - start);
    }

    @Test
    void normalField() {
        ThrowingRunnable test = () -> {
            var field = TestObject.class.getDeclaredField("integer");
            field.setAccessible(true);

            var modifiers = Fields.of(Field.class, "modifiers");
            Unsafe.putBoolean(modifiers, Fields.overrideOffset, true);
            modifiers.setInt(field, Flags.remove(field, Flags.FINAL));
        };

        time(test);
        time(test);
        time(test);
        time(test);
        time(test);
    }

    @Test
    void reflectField() {
        Fields.direct(TestObject.class);
        time("cache ", () -> Fields.of(TestObject.class));

        mean("cached", () -> Fields.of(TestObject.class).toList());
        mean("all   ", () -> Fields.all(TestObject.class).toList());
        mean("raw   ", () -> Fields.direct(TestObject.class));
    }

    @Test
    void instantiation() {
        mean("constructor", () -> new ArrayList<>());
        mean("Unsafe", () -> Unsafe.allocateInstance(ArrayList.class));
    }

    @Test
    void clon() {
        var a = new A();

        mean("clone         ", a::clone);
        mean("copy <init>   ", () -> new A(a));
        mean("copy          ", a::copy);
        mean("Accessor.clone", () -> Accessor.clone(a));
    }

    @Test
    public void unreflect() throws Throwable {
        var method = Methods.of(A.class, "privateMethod");
        var declaredMethod = A.class.getDeclaredMethod("privateMethod");
        var methodHandle = Invoker.findStatic(A.class, "privateMethod", String.class);
        var unreflected = Invoker.unreflect(method);

        mean("Method 0", () -> Methods.of(A.class, "privateMethod2", int.class));
        mean("Method 1", () -> Methods.of(A.class, "privateMethod"));

        mean("Method 2", () -> {
            var method1 = A.class.getDeclaredMethod("privateMethod");
            method1.setAccessible(true);
        });

        mean("MethodHandle#unreflect ", () -> Invoker.unreflect(declaredMethod));
        mean("MethodHandle#findStatic", () -> Invoker.findStatic(A.class, "privateMethod", String.class));
    }

    @Test
    void newInvokerUnreflect() {
        // timeN("new", () -> Invoker.unreflect2(A.class, "privateMethod"));

        mean("old", () -> Invoker.unreflect(A.class, "privateMethod"));
    }

    @Test
    void method() {
        mean(() -> Methods.of(TestObject.class));
        mean(ReflectTest.class::getDeclaredMethods);
    }

    @SuppressWarnings("Convert2Lambda")
    @Test
    void lambda() {
        time("anonymous class", () -> runnable0 = new Runnable() {
            @Override
            public void run() {}
        });

        time("lambda", () -> runnable1 = () -> {});
    }

    @Test
    void cast() {
        mean("checkcast", () -> {ReflectTest test = Util.nul();});
        mean("Class#cast", () -> {ReflectTest test = ReflectTest.class.cast(Util.nul());});
    }

    @Test
    void stringChars() {
        String string = "a".repeat(10000);

        Logger.log("array");
        mean("toCharArray", string::toCharArray);
        mean("codePoints", () -> string.codePoints().toArray());
        mean("getBytes", string::getBytes);

        Logger.log("\niteration");
        mean("toCharArary", () -> {
            for (char character : string.toCharArray()) {}
        });
        mean("codePoints", () -> string.codePoints().forEach(value -> {}));
        mean("getBytes", () -> {
            for (byte bite : string.getBytes()) {}
        });
    }

    @Test
    void typeCheck() {
        enum Type {INT, DOUBLE}
        record BoxRecord(Type type) {}
        abstract class BoxClass {}
        class IntBox extends BoxClass {int i;}
        class DoubleBox extends BoxClass {double d;}
        BoxClass b = new IntBox();
        BoxRecord r = new BoxRecord(Type.INT);

        mean("instanceof", Util.voidify(() -> b instanceof DoubleBox));
        mean("enum", Util.voidify(() -> r.type() == Type.DOUBLE));
    }

    @Test
    void box() {
        int[] array = IntStream.range(0, 1000).toArray();

        Types.box(array);
        time(() -> Types.box(array));
    }

    static {
        Logger.log("initialized in %s ms%n", time(false, Util::load) / 1000000D);
    }
}
