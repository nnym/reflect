package reflect.ast;

import java.lang.invoke.MethodHandle;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import reflect.ast.base.method.expression.Invocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.commons.annotation.Testable;

@Disabled
@Testable
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnsafeBuilder extends TestBuilder {
    public UnsafeBuilder() {
        super("net.auoeke.unsafe.Unsafe");

        this.public$();
    }

    @Test
    void generate() {
        Methods.of(Classes.load("jdk.internal.misc.Unsafe")).filter(method -> Flags.isPublic(method) && Flags.isInstance(method)).forEach(unsafeMethod -> {
            this.method(method -> method.inherit(unsafeMethod));

            MethodHandle handle = Invoker.unreflectSpecial(unsafeMethod);

            this.field(field -> field.type(MethodHandle.class).name(unsafeMethod.getName()).initialize(new Invocation(Invoker.class, "bind")));
        });
    }

    @Override
    @AfterEach
    protected void tearDown() {
        super.tearDown();
    }
}
