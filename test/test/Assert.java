package test;

import net.auoeke.result.Result;
import net.gudenau.lib.unsafe.Unsafe;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Assert {
	public static Assert yes(boolean boolea) {
		assert boolea;
		return null;
	}

	public static Assert yes(boolean boolea, Supplier<String> message) {
		assert boolea : message.get();
		return null;
	}

	public static Assert no(boolean boolea) {
		return yes(!boolea);
	}

	public static Assert no(boolean boolea, Supplier<String> message) {
		return yes(!boolea, message);
	}

	public static Assert exception(String message, Class<? extends Throwable> type, Runnable action) {
		var result = Result.ofVoid(action::run);

		if (result.isSuccess() || type != null && !type.isInstance(result.cause().get())) {
			throw new AssertionError(message);
		}

		return null;
	}

	public static Assert exception(String message, Runnable action) {
		return exception(message, null, action);
	}

	public static Assert exception(Runnable action) {
		return exception(null, null, action);
	}

	public static Assert exception(Class<? extends Throwable> type, Runnable action) {
		return exception(null, type, action);
	}

	public static Assert success(String message, Runnable action) {
		Result.ofVoid(action::run).cause().ifPresent(Unsafe::throwException);
		return null;
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
		return yes(objects.length < 2 || Stream.of(objects).allMatch(Predicate.isEqual(objects[0])), () -> Arrays.deepToString(objects));
	}

	public static Assert nul(Object object) {
		return yes(object == null);
	}

	public static Assert notNull(Object object) {
		return yes(object != null);
	}

	public static Assert arraysEqual(Object[]... arrays) {
		return yes(arrays.length < 2 || Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)), () -> Arrays.deepToString(arrays));
	}

	public static Assert arraysEqual(byte[]... arrays) {
		return yes(Stream.of(arrays).allMatch(array -> Arrays.equals(arrays[0], array)), () -> Arrays.deepToString(arrays));
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public static Assert elementsEquivalent(Stream<?>... streams) {
		var lists = Stream.of(streams).map(Stream::toList).toList();
		var first = lists.get(0);
		return yes(lists.stream().allMatch(list -> list.size() == first.size() && list.containsAll(first)), lists::toString);
	}

	public static <T> Assert equalBy(Function<T, Object> key, T... objects) {
		return equal(Stream.of(objects).map(key).toArray());
	}

	public static <K, V, T, U> Assert entriesEqualBy(Function<K, T> key, Function<V, U> value, Map<K, V> map) {
		return yes(map.entrySet().stream().allMatch(entry -> Objects.equals(key.apply(entry.getKey()), value.apply(entry.getValue()))), map::toString);
	}

	public static <K, V, T> Assert entriesEqualBy(Function<K, T> key, Map<K, V> map) {
		return entriesEqualBy(key, Function.identity(), map);
	}

	public static Assert distinct(Object... objects) {
		return yes(Stream.of(objects).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
	}

	public static <T> Assert distinctBy(Function<T, Object> key, T... objects) {
		return yes(Stream.of(objects).map(key).distinct().count() == objects.length, () -> Arrays.deepToString(objects));
	}
}
