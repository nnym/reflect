package test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Assert {
	public static Assert truth(boolean boolea) {
		assert boolea;
		return null;
	}

	public static Assert truth(boolean boolea, Supplier<String> message) {
		assert boolea : message.get();
		return null;
	}

	public static Assert exception(String message, Runnable action) {
		try {
			action.run();
		} catch (Throwable throwable) {
			return null;
		}

		throw new AssertionError(message);
	}

	public static Assert exception(Runnable action) {
		return exception(null, action);
	}

	public static Assert success(String message, Runnable action) {
		try {
			action.run();
			return null;
		} catch (Throwable trouble) {
			throw new AssertionError(message, trouble);
		}
	}

	public static Assert success(Runnable action) {
		return success(null, action);
	}

	public static Assert success(String message, Supplier<?> action) {
		return success(message, (Runnable) action::get);
	}

	public static Assert success(Supplier<?> action) {
		return success(null, action);
	}

	public static Assert equal(Object... objects) {
		return truth(objects.length < 2 || Stream.of(objects).allMatch(Predicate.isEqual(objects[0])), () -> Arrays.deepToString(objects));
	}

	public static Assert nul(Object object) {
		return truth(object == null);
	}

	public static Assert notNull(Object object) {
		return truth(object != null);
	}

	public static Assert arraysEqual(Object[]... arrays) {
		return truth(arrays.length < 2 || Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)), () -> Arrays.deepToString(arrays));
	}

	public static Assert arraysEqual(byte[]... arrays) {
		return truth(Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)), () -> Arrays.deepToString(arrays));
	}

	public static Assert elementsEquivalent(Stream<?>... streams) {
		var lists = Stream.of(streams).map(Stream::toList).toList();
		var first = lists.get(0);
		return truth(lists.stream().allMatch(list -> list.size() == first.size() && list.containsAll(first)), lists::toString);
	}

	public static <T> Assert equalBy(Function<T, Object> key, T... objects) {
		return equal(Stream.of(objects).map(key).toArray());
	}

	public static Assert distinct(Object... objects) {
		return truth(Stream.of(objects).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
	}

	public static <T> Assert distinctBy(Function<T, Object> key, T... objects) {
		return truth(Stream.of(objects).map(key).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
	}
}
