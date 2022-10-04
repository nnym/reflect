package reflect.ast.base.template;

import java.util.function.Consumer;
import reflect.ast.Access;

public class Discriminator<T> {
	public static final Discriminator<Class<?>> type = new Discriminator<>("type", boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, Object.class);
	public static final Discriminator<Access> access = new Discriminator<>("access", Access.values());

	public final String name;
	public final T[] defaultValues;

	private boolean present;
	private T value;

	@SafeVarargs
	public Discriminator(String name, T... defaultValues) {
		this.name = name;
		this.defaultValues = defaultValues;
	}

	@SafeVarargs
	public Discriminator(T... defaultValues) {
		this(null, defaultValues);
	}

	public T get() {
		if (!this.present) {
			throw new IllegalStateException(this.name == null ? "This discriminator does not have a value." : "Discriminator \"%s\" does not have a value.".formatted(this.name));
		}

		return this.value;
	}

	public void set(T value) {
		this.value = value;
		this.present = true;
	}

	public void ifPresent(Consumer<T> action) {
		if (this.present) {
			action.accept(this.value);
		}
	}
}
