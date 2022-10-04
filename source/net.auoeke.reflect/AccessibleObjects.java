package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import net.auoeke.reflect.Invoker;

public class AccessibleObjects {
	private static final MethodHandle getRoot = Invoker.findVirtual(AccessibleObject.class, "getRoot", AccessibleObject.class);

	public static <T extends AccessibleObject> T root(T object) {
		return (T) getRoot.invokeExact(object);
	}
}
