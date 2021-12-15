package net.auoeke.reflect;

import java.io.File;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.auoeke.reflect.misc.A;
import net.auoeke.reflect.misc.C;
import net.auoeke.reflect.misc.Enumeration;
import net.auoeke.reflect.misc.TestObject;
import net.auoeke.reflect.util.Logger;
import net.auoeke.reflect.util.Util;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings({"ResultOfMethodCallIgnored", "AssertWithSideEffects", "FieldMayBeFinal"})
@Testable
public class ReflectTest {
    static void logFields(Object object) {
        Reflect.run(() -> Fields.getInstance(object.getClass()).forEach(field ->
            System.out.printf("%s: %s\n", field, Accessor.get(object, field))
        ));
    }

    @Test
    public void changeLoader() {
        Class<?> PackagePrivate = Classes.defineBootstrapClass(ReflectTest.class.getClassLoader(), "net/auoeke/reflect/misc/PackagePrivate");
        assert PackagePrivate.getClassLoader() == null;

        Accessor.putReference((Object) PackagePrivate, "classLoader", Reflect.defaultClassLoader);
        assert PackagePrivate.getClassLoader() == Reflect.defaultClassLoader;
    }

    @Test
    public void constantPool() {
        Function<Integer, Integer> lambda = i -> i;
        var pool = new ConstantPool(lambda.getClass());
        int size = pool.getSize();

        Logger.log(size);

        for (var i = 0; i < size; i++) {
            var method = pool.getMethodAt(i);

            if (method != null) {
                Logger.log(method);
            }
        }
    }

    @Test
    public void invokeExact() throws Throwable {
        var c = new C();
        var handle = Invoker.findSpecial(A.class, "print", void.class);

        handle.invoke(c);
        handle.invokeExact((A) c);

        handle = handle.bindTo(c);

        handle.invoke();
        handle.invokeExact();
    }

    @SuppressWarnings("WrapperTypeMayBePrimitive")
    @Test
    public void pointer() {
        var a = new A();
        a.yes = 446;
        Double yes = a.yes;

        var pointer = new Pointer().bind(a).instanceField("yes");

        Util.repeat(() -> {
            pointer.putDouble(pointer.getDouble() + 4);
            Accessor.putDouble(yes, "value", yes + 4);

            assert yes == pointer.getDouble();
        });
    }

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

        EnumConstructor.add(Enumeration.class, "TEST", 1D);
        verifySize.run();

        EnumConstructor.add(Enumeration.class, 0, "TEST", 3D);
        verifySize.run();

        EnumConstructor.add(0L, Enumeration.class, "TEST", 4D);
        verifySize.run();

        EnumConstructor.add(0L, Enumeration.class, 1, "TEST", 5D);
        verifySize.run();

        EnumConstructor.add(Enumeration.class, enumeration);
        verifySize.run();

        var constructor = new EnumConstructor<>(RetentionPolicy.class);
        Util.repeat(() -> constructor.add("TEST"));
        Enumeration.valueOf("TEST");
    }

    @Test
    public void reinterpret() {
        var a = Classes.reinterpret(A.class, Integer.class);

        System.out.println(a);
        System.out.println(A.class.getClassLoader());

        Double dubble = 0D;
        var loong = Classes.reinterpret(dubble, Long.class);

        System.out.println(dubble);
        Accessor.putLong(loong, "value", 0xFFFFFFFFFFL);
        System.out.println(loong);
        System.out.println(Classes.reinterpret(loong, Double.class));

        Classes.reinterpret(A.class, (Object) Class.class);
    }

    @Test
    public void invokerOverload() throws Throwable {
        Logger.log(Invoker.unreflect(Boolean.class, "getBoolean", String.class).invoke("123"));
        Logger.log(Invoker.unreflectConstructor(Boolean.class, boolean.class).invoke(true));
    }

    @Test
    public void allFields() {
        Fields.all(C.class).forEach(Logger::log);
    }

    @Test
    public void classPointer() {
        var object = Unsafe.allocateInstance(Object.class);
        System.out.println(object);
        Classes.reinterpret(object, ReflectTest.class);
        System.out.println(object);

        object = Unsafe.allocateInstance(ReflectTest.class);
        System.out.println(object);
        Classes.reinterpret(object, ReflectTest.class);
        System.out.println(object);

        object = Unsafe.allocateInstance(Object.class);
        System.out.println(object);
        Classes.reinterpret(object, new ReflectTest());
        System.out.println(object);
    }

    @Test
    public void testCopy() {
        var one = new TestObject();
        var two = new TestObject();
        var fields = Fields.getInstance(ReflectTest.class).toList();

        Methods.get(Accessor.class).map(Method::getName).filter(name -> name.startsWith("copy")).forEach(name -> {
            var typeName = name.substring(name.indexOf('y') + 1).toLowerCase();

            fields.forEach(field -> {
                var fieldTypeName = field.getType().getSimpleName().toLowerCase();

                if (fieldTypeName.equals(typeName)) {
                    Accessor.copyObject(one, two, field);
                } else if (fieldTypeName.endsWith("volatile")) {
                    Accessor.copyObjectVolatile(one, two, field);
                }
            });
        });
    }

    @Test
    public void classPath() throws Throwable {
        var classPath = Classes.classPath(ReflectTest.class.getClassLoader());
        var file = new File("test");

        for (var url : Classes.urls(classPath)) {
            Logger.log(url);
        }

        System.out.println();
        System.out.println();
        System.out.println();

        Classes.addURL(classPath, file.toURI().toURL());

        for (var url : Classes.urls(classPath)) {
            Logger.log(url);
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Test
    void constructor() throws Throwable {
        class PrivateCtor {
            public final int test;

            private PrivateCtor(int test) {
                this.test = test;
            }
        }

        var constructor = PrivateCtor.class.getDeclaredConstructor(ReflectTest.class, int.class);

        assert Arrays.equals(Constructors.direct(PrivateCtor.class), new Constructor[]{constructor});
        assert Constructors.find(PrivateCtor.class, ReflectTest.class, int.class).newInstance(this, 4).test == 4;
        assert Constructors.construct(PrivateCtor.class, this, 27).test == 27;
    }

    @Test
    void method() {
        assert Constructors.find(0L, Enumeration.class, "", 1, 4D) == null;
        assert Constructors.find(Enumeration.class, "", 1, 4D) != null;
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
    void member() throws Throwable {
        MethodHandle handle = Invoker.findGetter(Integer.class, "value", int.class);
        Member member = Fields.get(Integer.class, "value");

        assert Invoker.field(handle).equals(member);
        assert Invoker.member(handle).equals(member);

        handle = Invoker.findConstructor(String.class, char[].class);
        member = Constructors.find(String.class, char[].class);

        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.constructor(handle).equals(member);

        handle = Invoker.findVirtual(Object.class, "toString", String.class);
        member = Methods.get(Object.class, "toString");

        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.findSpecial(String.class, "indexOfNonWhitespace", int.class);
        member = Methods.get(String.class, "indexOfNonWhitespace");

        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.findStatic(String.class, "valueOf", String.class, boolean.class);
        member = Methods.get(String.class, "valueOf", boolean.class);

        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);

        handle = Invoker.unreflect((Method) member);

        assert Invoker.member(handle).equals(member);
        assert Invoker.executable(handle).equals(member);
        assert Invoker.method(handle).equals(member);
    }

    @Test
    void accessor() {
        assert Accessor.getIntVolatile(0, "value") == 0 && Accessor.getInt(0, "value") == 0;

        class A {
            final byte end = 123;
        }

        assert Accessor.get(new A(), "end").equals((byte) 123);
        assert Accessor.getVolatile(new A(), "end").equals((byte) 123);

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

        var clone = Accessor.clone(obj);

        assert clone.field == 84;
        assert "hjk".equals(clone.thing);
        assert clone.object == null;
        assert clone.things instanceof ArrayList<Object> things && things.isEmpty();
    }

    @Test
    void stack() throws Throwable {
        Supplier<Class<?>> lambda = StackFrames::caller;

        assert Util.equals(
            StackFrames.caller(),
            StackFrames.caller(0),
            StackFrames.caller(frame -> true),
            lambda.get(),
            StackFrames.frames().get(1).getDeclaringClass()
        );

        assert StackFrames.trace()[0].getClassName().equals(ReflectTest.class.getName());
        assert StackFrames.traceFrame().getMethodName().equals("stack");
        assert StackFrames.traceFrame(0).getMethodName().equals("stack");
    }

    @Test
    void box() {
        var ints = IntStream.range(0, 10000).toArray();
        assert Arrays.equals(ints, Stream.of(Types.box(ints)).mapToInt(i -> (int) i).toArray());

        var doubles = DoubleStream.iterate(0, d -> d < 100, d -> d + 1).toArray();
        assert Arrays.equals(doubles, Stream.of(Types.box(doubles)).mapToDouble(d -> (double) d).toArray());

        byte[] bytes = {1, 2, 3, 4, 5};
        var box = Types.<Byte>box(bytes);

        assert box[0] == bytes[0]
            && box[1] == bytes[1]
            && box[2] == bytes[2]
            && box[3] == bytes[3]
            && box[4] == bytes[4];

        assert Types.box(box) == box;

        Object[] objects = new Object[0];
        assert Types.box(objects) == objects;
    }

    @Test
    void unbox() {
        Integer[] ints = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).toArray(Integer[]::new);
        assert Arrays.equals(Stream.of(ints).mapToInt(i -> i).toArray(), Types.unbox(ints));

        Double[] doubles = Stream.iterate(0D, d -> d < 100, d -> d + 1).toArray(Double[]::new);
        assert Arrays.equals(Stream.of(doubles).mapToDouble(d -> d).toArray(), Types.unbox(doubles));

        Byte[] box = {1, 2, 3, 4, 5};
        byte[] bytes = Types.unbox(box);

        assert bytes[0] == box[0]
            && bytes[1] == box[1]
            && bytes[2] == box[2]
            && bytes[3] == box[3]
            && bytes[4] == box[4];

        assert Types.unbox(bytes) == bytes;
        assert Types.unbox(new Object[0]) == null;
    }

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
    void types() {
        assert Types.canCast(Object.class, Object.class);
        assert Types.canCast(Object.class, this.getClass());
        assert !Types.canCast(this.getClass(), Object.class);
        assert Types.canCast(Integer.class, int.class);
        assert Types.canCast(int.class, Integer.class);
        assert Types.canCast(new Class[]{Double.class, Long.class, long.class, this.getClass(), Object.class}, double.class, Long.class, Long.class, this.getClass(), this.getClass());
        assert Types.canCast(new Class[]{Double.class, Long.class, long.class, this.getClass(), Object.class}, 0D, 0L, 0L, this, this);
        assert !Types.canCast(Double.class, int.class);
        assert !Types.canCast(Double.class, float.class);
        assert !Types.canCast(Double.class, Float.class);
        assert Types.canCast(double.class, float.class);
        assert Types.canCast(long.class, int.class);
        assert Types.canCast(long.class, short.class);
        assert Types.canCast(int.class, char.class);
        assert !Types.canCast(int.class, boolean.class);

        assert Types.classes(C.class, A.class).count() == 2;
        assert Types.classes(Object.class).count() == 1;
    }

    @Test
    void special() throws Throwable {
        var handle = Invoker.findSpecial(Object.class, "toString", String.class);
        var list = new ArrayList<>();

        assert !list.toString().equals(handle.invoke(list));
    }

    static {
        Reflect.run(Util::load);
    }
}
