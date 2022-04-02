package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;

/**
 @since 1.2.0
 */
public class JavaLangAccess {
    public static final Object javaLangAccess = Invoker.findStatic(Classes.SharedSecrets, "getJavaLangAccess", Classes.JavaLangAccess).invoke();

    private static final MethodHandle getConstantPool = Invoker.bind(javaLangAccess, "getConstantPool", Classes.ConstantPool, Class.class);

    public static Object getConstantPool(Class<?> klass) {
        return getConstantPool.invoke(klass);
    }
}
