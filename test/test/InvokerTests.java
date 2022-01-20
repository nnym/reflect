package test;

import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import lombok.val;
import net.auoeke.reflect.Constructors;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class InvokerTests extends Invoker {
    private static final Class<InvokerTests> cla$$ = InvokerTests.class;

    @Test
    void unreflect() throws Throwable {
        assert Invoker.unreflect(RetentionPolicy.class, "valueOf").invoke("RUNTIME") == RetentionPolicy.RUNTIME;

        Integer four = 4;
        assert Invoker.unreflect(four, "doubleValue").invoke() instanceof Double doubleObject && doubleObject == 4;

        Function<String, String> stringTransformer = string -> string.charAt(0) + string.repeat(3) + string.charAt(string.length() - 1);
        assert "aabcdabcdabcdd".equals(Invoker.unreflect(stringTransformer, "apply").invoke("abcd"));

        Invoker.unreflect(new Object[0], "toString").invoke();
    }

    @Test
    void special() throws Throwable {
        val list = new ArrayList<>();
        assert !Invoker.findSpecial(Object.class, "toString", String.class).invoke(list).equals(list.toString());
    }

    @Test
    void member() throws Throwable {
        var handle = Invoker.findGetter(Integer.class, "value", int.class);
        Member member = Fields.of(Integer.class, "value");
        assert Invoker.field(handle).equals(member);
        assert Invoker.member(handle).equals(member);

        handle = Invoker.findConstructor(String.class, char[].class);
        member = Constructors.find(String.class, char[].class);
        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.constructor(handle).equals(member);

        handle = Invoker.findVirtual(Object.class, "toString", String.class);
        member = Methods.of(Object.class, "toString");
        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.findSpecial(String.class, "indexOfNonWhitespace", int.class);
        member = Methods.of(String.class, "indexOfNonWhitespace");
        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.findStatic(String.class, "valueOf", String.class, boolean.class);
        member = Methods.of(String.class, "valueOf", boolean.class);
        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.unreflect((Method) member);
        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);
    }

    @Test
    void invoke() {
        Runnable runnable = () -> {};
        Function<Integer, String> function = String::valueOf;
        IntFunction<Integer> intFunction = Integer::valueOf;

        Invoker.invoke(Invoker.bind(runnable, "run", void.class));

        assert Invoker.invoke(Invoker.bind(runnable, "run", void.class)) == null;
        assert "123".equals(Invoker.invoke(Invoker.bind(function, "apply", Object.class, Object.class), 123));
        assert (Integer) Invoker.invoke(Invoker.bind(intFunction, "apply", Object.class, int.class), 57) == 57;
    }

    @Test
    public void invokerOverload() throws Throwable {
        assert !(boolean) Invoker.unreflect(Boolean.class, "getBoolean", String.class).invoke(UUID.randomUUID().toString());
        assert (Boolean) Invoker.unreflectConstructor(Boolean.class, boolean.class).invoke(true);
    }

    @Test
    void adaptTest() throws Throwable {
        val type = MethodType.methodType(double.class, double.class, int.class);
        val adapt0 = adapt(findStatic(cla$$, "adapt0", double.class, int.class, double.class), type);
        assert adapt0.type().equals(type) && (double) adapt0.invoke(3, 1) == 10;
        assert adapt(findStatic(cla$$, "adapt0", double.class, int.class, double.class), int.class, double.class).type() == MethodType.methodType(double.class, int.class, double.class);
        assert adapt(findStatic(cla$$, "adapt0", double.class, int.class, double.class), List.of(int.class, double.class)).type() == MethodType.methodType(double.class, int.class, double.class);

        val object = new Object();
        var adapt1 = (Object[]) adapt(
            findStatic(cla$$, "adapt1", Object[].class, Invoker.class, Object.class, long.class, short.class, Float.class),
            short.class, long.class, Object.class, Float.class, Invoker.class
        ).invokeWithArguments((short) 24, 57, object, 4F, null);
        assert adapt1[0] == null && adapt1[1] == object && (long) adapt1[2] == 57 && (short) adapt1[3] == 24 && (float) adapt1[4] == 4;

        val adapt2 = findStatic(cla$$, "adapt2", void.class);
        assert adapt(adapt2) == adapt2;

        adapt1 = (Object[]) adapt(
            findStatic(cla$$, "adapt1", Object[].class, Invoker.class, Object.class, long.class, short.class, Float.class),
            Invoker.class, Float.class, long.class, short.class, Object.class
        ).invokeWithArguments(null, 4F, 57, (short) 24, object);
        assert adapt1[0] == null && adapt1[1] == object && (long) adapt1[2] == 57 && (short) adapt1[3] == 24 && (float) adapt1[4] == 4;
    }

    private static double adapt0(int a, double b) {
        return a + b * b;
    }

    private static Object[] adapt1(Invoker i, Object o, long l, short s, Float f) {
        return new Object[]{i, o, l, s, f};
    }

    private static void adapt2() {}
}
