package experimental;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import net.auoeke.reflect.Invoker;

public class Fields2 {
	private static final MethodHandle copy = Invoker.findSpecial(Field.class, "copy", Field.class);

	public static Field copy(Field field) {
		return (Field) copy.invokeExact(field);
	}
}
