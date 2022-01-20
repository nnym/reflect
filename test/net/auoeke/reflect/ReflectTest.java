package net.auoeke.reflect;

import java.io.File;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.val;
import net.auoeke.reflect.misc.A;
import net.auoeke.reflect.misc.B;
import net.auoeke.reflect.misc.C;
import net.auoeke.reflect.misc.Enumeration;
import net.auoeke.reflect.misc.TestObject;
import net.auoeke.reflect.util.Util;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings({"ResultOfMethodCallIgnored", "AssertWithSideEffects", "FieldMayBeFinal", "JavaReflectionMemberAccess"})
@Testable
public class ReflectTest {
    @SuppressWarnings("WrapperTypeMayBePrimitive")
    @Test
    public void pointer() {
        val a = new A();
        a.yes = 446;
        Double yes = a.yes;

        val pointer = new Pointer().bind(a).instanceField("yes");

        Util.repeat(() -> {
            pointer.putDouble(pointer.getDouble() + 4);
            Accessor.putDouble(yes, "value", yes + 4);

            assert yes == pointer.getDouble();
        });
    }

    @Test
    public void enumeration() {
        val retentionPolicyConstructor = EnumConstructor.constructor(0L, RetentionPolicy.class);
        val enumerationConstructor = EnumConstructor.constructor(Enumeration.class, 0D);

        assert EnumConstructor.construct(Enumeration.class, "TEST", 1).ordinal() == 0;
        assert EnumConstructor.construct(Enumeration.class, 0, "TEST", 553D).test == 553;
        assert EnumConstructor.construct(0L, Enumeration.class, "TEST", 4D).test == 4;
        assert EnumConstructor.construct(0L, Enumeration.class, 1, "TEST", 9023D).test == 9023;

        val enumeration = EnumConstructor.construct(Enumeration.class, 2, "TEST", 2D);
        assert enumeration != null && enumeration.test == 2;

        val expectedLength = new Object() {
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

        val constructor = new EnumConstructor<>(RetentionPolicy.class);
        Util.repeat(() -> constructor.add("TEST"));
        Enumeration.valueOf("TEST");
    }

    @Test
    public void reinterpret() {
        var a = Classes.reinterpret(A.class, Integer.class);
        a.toString();
        A.class.getClassLoader().toString();

        Double dubble = 0D;
        var loong = Classes.reinterpret(dubble, Long.class);

        Accessor.putLong(loong, "value", 0xFFFFFFFFFFL);
        assert loong == 0xFFFFFFFFFFL;

        Classes.reinterpret(loong, Double.class);
        Classes.reinterpret(A.class, (Object) Class.class);

        var object = (Object) Classes.reinterpret(Unsafe.allocateInstance(Object.class), ReflectTest.class);
        assert object.getClass() == (object = Unsafe.allocateInstance(ReflectTest.class)).getClass();
        assert object.getClass() == Classes.reinterpret(object, ReflectTest.class).getClass();
        assert Classes.reinterpret(Unsafe.allocateInstance(Object.class), new ReflectTest()).getClass() == ReflectTest.class;
    }

    @Test
    public void allFields() {
        assert Stream.of(A.class, B.class, C.class).allMatch(Fields.all(C.class).collect(HashMap::new, (map, field) -> map.computeIfAbsent(field.getDeclaringClass(), type -> new HashMap<>()), Map::putAll)::containsKey);
    }

    @Test
    public void testCopy() {
        var one = new TestObject();
        var two = new TestObject();
        var fields = Fields.instanceOf(ReflectTest.class).toList();

        Methods.of(Accessor.class).map(Method::getName).filter(name -> name.startsWith("copy")).forEach(name -> {
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
        val classPath = Classes.classPath(ReflectTest.class.getClassLoader());
        val file = new File("test");
        val url = file.toURI().toURL();
        Classes.addURL(classPath, url);
        Arrays.asList(Classes.urls(classPath)).contains(url);
    }

    @Test
    void constructor() throws Throwable {
        class PrivateCtor {
            public final int test;

            private PrivateCtor(int test) {
                this.test = test;
            }
        }

        assert Arrays.equals(Constructors.direct(PrivateCtor.class), new Constructor[]{PrivateCtor.class.getDeclaredConstructor(ReflectTest.class, int.class)});
        assert Constructors.find(PrivateCtor.class, ReflectTest.class, int.class).newInstance(this, 4).test == 4;
        assert Constructors.construct(PrivateCtor.class, this, 27).test == 27;
        assert Constructors.find(0L, Enumeration.class, "", 1, 4D) == null;
        assert Constructors.find(Enumeration.class, "", 1, 4D) != null;
    }

    @Test
    void accessor() {
        assert Accessor.getIntVolatile(0, "value") == 0 && Accessor.getInt(0, "value") == 0;

        class A {
            final byte end = 123;
        }

        assert Accessor.get(new A(), "end").equals((byte) 123);
        assert Accessor.getVolatile(new A(), "end").equals((byte) 123);

        val obj = new Object() {
            int field = 22;
            String thing = "asd";
            Object object = new Object() {};
            List<Object> things = Arrays.asList(23, 46, "12");
        };

        obj.field = 84;
        obj.thing = "hjk";
        obj.object = null;
        obj.things = new ArrayList<>();

        val clone = Accessor.clone(obj);
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

    static {
        Util.load();
    }
}
