package net.auoeke.reflect;

import static net.auoeke.dycon.Dycon.*;

/**
 @since 1.2.0
 */
public class JavaLangAccess {
	public static final Object javaLangAccess = Invoker.findStatic(Classes.SharedSecrets, "getJavaLangAccess", Classes.JavaLangAccess).invoke();

	public static Object getConstantPool(Class<?> klass) {
		return ldc(() -> Invoker.bind(javaLangAccess, "getConstantPool", Classes.ConstantPool, Class.class)).invoke(klass);
	}
}
