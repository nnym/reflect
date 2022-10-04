package template;

import java.lang.reflect.Field;
import net.gudenau.lib.unsafe.Unsafe;

public class AccessorTemplate extends Template {
	@Repeat(names = @Name(type = Object.class, name = "reference"),
			methods = @Name(type = Unsafe.class, name = "get$type"))
	public static long getAddress(Field field) {
		return Unsafe.getAddress(field.getDeclaringClass(), Unsafe.staticFieldOffset(field));
	}
}
