package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import lombok.SneakyThrows;

public class JavaLangAccess {
    public static final Object javaLangAccess = Invoker.invoke(Invoker.findStatic(Classes.SharedSecrets, "getJavaLangAccess", Classes.JavaLangAccess));

    private static final MethodHandle getConstantPool = Invoker.bind(javaLangAccess, "getConstantPool", Classes.ConstantPool, Class.class);

    @SneakyThrows
    public static Object getConstantPool(Class<?> klass) {
        return getConstantPool.invoke(klass);
    }
}
