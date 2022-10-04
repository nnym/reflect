package experimental;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import net.auoeke.reflect.Invoker;

public class Constructors2 {
	private static final MethodHandle copy = Invoker.findSpecial(Constructor.class, "copy", Constructor.class);

	public static Constructor copy(Constructor constructor) {
		return (Constructor) copy.invokeExact(constructor);
	}
}
