package mock;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClosableInputStream extends FilterInputStream {
	private boolean closed;

	public ClosableInputStream(InputStream parent) {
		super(parent);
	}

	@Override public void close() {
		super.close();
		this.closed = true;
	}

	public boolean closed() {
		return this.closed;
	}
}
