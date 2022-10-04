package net.auoeke.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 A wrapper of a result of some computation or errors that happened therein.

 @param <T> the type of the result of the computation
 @since 4.9.0
 */
public final class Result<T> {
	private boolean success;
	private Object value;
	private List<Throwable> suppressed;

	Result() {}

	static <T> Result<T> of(Supplier<T> action) {
		return new Result<T>().map(action);
	}

	static <T> Result<T> of(T value) {
		var result = new Result<T>();
		result.value = value;
		result.success = true;

		return result;
	}

	/**
	 If a value is present, then retrieve it; otherwise throw the exception.

	 @return the value if present
	 */
	public T value() {
		if (this.success) {
			return (T) this.value;
		}

		throw (Throwable) this.value;
	}

	public T or(T alternative) {
		return this.success ? (T) this.value : alternative;
	}

	public T orGet(Supplier<T> alternative) {
		return this.success ? (T) this.value : alternative.get();
	}

	public T orNull() {
		return this.or(null);
	}

	public boolean success() {
		return this.success;
	}

	public boolean failure() {
		return !this.success;
	}

	Result<T> and(Runnable action) {
		try {
			action.run();
		} catch (Throwable trouble) {
			if (this.success || this.value == null) {
				this.value = trouble;
			} else {
				((Throwable) this.value).addSuppressed(trouble);
			}

			this.success = false;
		}

		return this;
	}

	void andSuppress(Runnable action) {
		try {
			action.run();
		} catch (Throwable trouble) {
			if (this.success || this.value == null) {
				(this.suppressed == null ? this.suppressed = new ArrayList<>() : this.suppressed).add(trouble);
			} else {
				((Throwable) this.value).addSuppressed(trouble);
			}
		}
	}

	<A> A andSuppress(Supplier<A> action) {
		try {
			return action.get();
		} catch (Throwable trouble) {
			if (this.success || this.value == null) {
				(this.suppressed == null ? this.suppressed = new ArrayList<>() : this.suppressed).add(trouble);
			} else {
				((Throwable) this.value).addSuppressed(trouble);
			}

			return null;
		}
	}

	Result<T> map(Supplier<T> action) {
		try {
			this.value = action.get();
			this.success = true;
		} catch (Throwable trouble) {
			if (this.failure() && this.value != null) {
				trouble.addSuppressed((Throwable) this.value);
			}

			if (this.suppressed != null) {
				this.suppressed.forEach(trouble::addSuppressed);
				this.suppressed.clear();
			}

			this.value = trouble;
			this.success = false;
		}

		return this;
	}

	Result<T> flatMap(Result<T> result) {
		this.success = result.success;
		this.value = result.value;
		this.suppressed = result.suppressed;

		return this;
	}
}
