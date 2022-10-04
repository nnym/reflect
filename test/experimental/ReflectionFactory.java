package experimental;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import net.auoeke.reflect.Invoker;

public class ReflectionFactory {
	private static final MethodHandle field;

	public static Field field(Class<?> declaringClass, Class<?> type, int modifiers, int slot) {
		return field(declaringClass, null, type, modifiers, slot, null, null);
	}

	public static Field field(Class<?> declaringClass, String name, Class<?> type, int modifiers, int slot) {
		return field(declaringClass, name, type, modifiers, slot, null, null);
	}

	public static Field field(Class<?> declaringClass, String name, Class<?> type, int modifiers, int slot, String signature, byte[] annotations) {
		return (Field) field.invokeExact(declaringClass, name, type, modifiers, slot, signature, annotations);
	}

	static {
		var field$init = Invoker.unreflectConstructor(Field.class.getDeclaredConstructors()[0]);
		field = field$init.type().parameterCount() == 8 ? MethodHandles.insertArguments(field$init, 4, false) : field$init;
	}
}
