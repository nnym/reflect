package reflect.other;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.Reflect;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jol.info.ClassData;
import reflect.experimental.generics.Generics;
import reflect.generics.GenericTypeAware;
import reflect.generics.GenericTypeAwareTest;
import reflect.util.Logger;
import reflect.util.Util;

@Disabled
@Testable
public class IrrelevantTest {
    public static <T, R> R var(T object, Function<T, R> function) {
        return function.apply(object);
    }

    public static <T> void var(T object, Consumer<T> consumer) {
        consumer.accept(object);
    }

    @Test
    public void accessFlags() {
        var data = ClassData.parseInstance(IrrelevantTest.class);

        Util.bp();
    }

    @Test
    public void genericMetadata() {
        var interfaces = GenericTypeAwareTest.class.getGenericInterfaces();
        var superclass = GenericTypeAwareTest.class.getGenericSuperclass();
        Type[] parameters = GenericTypeAware.class.getTypeParameters();
        var typeArguments = Generics.typeArguments(GenericTypeAwareTest.Sub.Sub1.class);

        Util.bp();
    }

    @Test
    public void genericTypeAware() {
        var typeAware = new GenericTypeAwareTest();

        Logger.log(typeAware.type);
    }

    @Test
    void enumMethodHandle() {
        Invoker.findConstructor(RetentionPolicy.class, String.class, int.class).invoke(null, 1);
    }

    @Test
    void visibilities() {
        var count = (Consumer<Function<Class<?>, Member[]>>) members -> {
            var pub = 0;
            var pri = 0;

            for (var type : Reflect.instrument().value().getAllLoadedClasses()) {
                for (var method : members.apply(type)) {
                    if (Flags.isPublic(method)) {
                        ++pub;
                    } else if (Flags.isPrivate(method)) {
                        ++pri;
                    }
                }
            }

            System.out.printf("private: %d;%npublic: %d;%n%f%% private%n%n", pri, pub, pri / (float) (pub + pri) * 100);
        };

        System.out.println("fields");
        count.accept(Fields::direct);

        System.out.println("methods");
        count.accept(Methods::direct);
    }

    static {
        var(new Object() {
            Object field0 = "field0";
            Object field1 = "field1";
        }, Object -> {
            Object.field0 = "new0";
            Object.field1 = "new1";
        });

        System.setProperty("jol.tryWithSudo", "true");
    }
}
