package reflect.ast.base.util;

public class Union<C, A extends C, B extends C> {
	public final Class<A> a;
	public final Class<B> b;

	protected Class<?> type;
	protected C value;

	public Union(Class<A> a, Class<B> b) {
		this.a = a;
		this.b = b;
	}

	public Union<C, A, B> set(C value) {
		this.type = value != null ? value.getClass() : null;
		this.value = value;

		return this;
	}

	public C get() {
		return this.value;
	}

	public A getA() {
		return (A) this.get();
	}

	public B getB() {
		return (B) this.get();
	}

	public boolean present() {
		return this.value != null;
	}

	public boolean is(Class<?> type) {
		return this.a == type;
	}
}
