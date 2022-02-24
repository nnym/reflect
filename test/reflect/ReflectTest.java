package reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.Pointer;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.misc.A;
import reflect.misc.B;
import reflect.misc.C;
import reflect.misc.TestObject;
import reflect.util.Util;

@SuppressWarnings("FieldMayBeFinal")
@Testable
public class ReflectTest {
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
    public void reinterpret() {
        Double dubble = 0D;
        var loong = Classes.reinterpret(dubble, Long.class);

        Accessor.putLong(loong, "value", 0xFFFFFFFFFFL);
        assert loong == 0xFFFFFFFFFFL;

        Classes.reinterpret(loong, Double.class);

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
}
