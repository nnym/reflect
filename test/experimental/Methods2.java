package experimental;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.auoeke.reflect.Invoker;

public class Methods2 {
	private static final MethodHandle rootCopy = Invoker.findSpecial(Method.class, "copy", Method.class);
	private static final MethodHandle leafCopy = Invoker.findSpecial(Method.class, "leafCopy", Method.class);
	
	public static Method rootCopy(Method method) {
		return (Method) rootCopy.invokeExact(method);
	}

	public static Method leafCopy(Method method) {
		return (Method) leafCopy.invokeExact(method);
	}

	public static Method copy(Method method) {
		return AccessibleObjects.root(method) == null ? rootCopy(method) : leafCopy(method);
	}
}
