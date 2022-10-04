package reflect.experimental;

import java.lang.invoke.MethodHandle;
import net.auoeke.reflect.Invoker;

public interface Copyable<T extends Copyable<T>> extends Cloneable {
	MethodHandle clone = Invoker.findSpecial(Object.class, "clone", Object.class);

	default void copy(T copy) {}

	default T copy() {
		var copy = (T) (Object) clone.invokeExact((Object) this);
		this.copy(copy);

		return copy;
	}
}
