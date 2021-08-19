package net.auoeke.reflect.generator;

import net.auoeke.reflect.Methods;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.generator.base.method.expression.Invocation;

public class UnsafeBuilder extends TestBuilder {
    public UnsafeBuilder() {
        super("net.auoeke.unsafe.Unsafe");

        this.pub();
    }

    @Override
    @Test
    protected void all() throws Throwable {
        super.all();
    }

    @Test
    void generate() {
        Class<Object> type = Classes.load("jdk.internal.misc.Unsafe");

        for (Method unsafeMethod : Methods.getMethods(type)) {
            if (Modifier.isPublic(unsafeMethod.getModifiers()) && !Modifier.isStatic(unsafeMethod.getModifiers())) {
                this.method(method -> method.inherit(unsafeMethod));

                MethodHandle handle = Invoker.unreflectSpecial(unsafeMethod, type);

                this.field(field -> field.type(MethodHandle.class).name(unsafeMethod.getName()).initialize(new Invocation(Invoker.class, "bind")));
            }
        }
    }

    @Override
    @AfterEach
    protected void tearDown() {
        super.tearDown();
    }
}
