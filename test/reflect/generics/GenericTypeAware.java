package reflect.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericTypeAware<T> {
	public final Class<T> type;

	protected GenericTypeAware() {
		Type genericSuperclass = this.getClass().getGenericSuperclass();
		assert genericSuperclass instanceof ParameterizedType;
		this.type = (Class<T>) Class.forName(((ParameterizedType) genericSuperclass).getActualTypeArguments()[0].getTypeName());
	}

	@SafeVarargs
	protected GenericTypeAware(boolean bad, T... typeArray) {
		this.type = (Class<T>) typeArray.getClass().getComponentType();
	}
}
