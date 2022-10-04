package reflect.misc;

public enum Enumeration implements GenericInterface<Object> {
	;
	public final double test;

	Enumeration(double test) {
		this.test = test;
	}

	@Override
	public void endOfBridge(Object thing) {

	}
}
