package experimental;

import java.lang.invoke.MethodHandle;
import net.auoeke.reflect.Invoker;

public interface Copiable<T extends Copiable<T>> extends Cloneable {
	MethodHandle clone = Invoker.findSpecial(Object.class, "clone", Object.class);

	default void copy(T copy) {}

	default T copy() {
		var copy = (T) (Object) clone.invokeExact((Object) this);
		this.copy(copy);

		return copy;
	}
}
