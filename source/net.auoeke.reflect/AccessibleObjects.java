package net.auoeke.reflect;

import java.lang.reflect.AccessibleObject;

import static net.auoeke.dycon.Dycon.*;

public class AccessibleObjects {
	public static <T extends AccessibleObject> T root(T object) {
		return (T) ldc(() -> Invoker.findVirtual(AccessibleObject.class, "getRoot", AccessibleObject.class)).invokeExact(object);
	}
}
