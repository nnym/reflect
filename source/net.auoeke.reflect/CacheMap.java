package net.auoeke.reflect;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 @since 4.0.0
 */
final class CacheMap<K, V> {
	private final Supplier<? extends Map<K, V>> constructor;
	private SoftReference<Map<K, V>> map;

	CacheMap(Supplier<? extends Map<K, V>> constructor) {
		this.constructor = constructor;
		this.map = this.construct();
	}

	static <K, V> CacheMap<K, V> identity() {
		return new CacheMap<>(IdentityHashMap::new);
	}

	static <K, V> CacheMap<K, V> hash() {
		return new CacheMap<>(HashMap::new);
	}

	V get(K key) {
		return this.map().get(key);
	}

	V put(K key, V value) {
		return this.map().put(key, value);
	}

	void putAll(CacheMap<? extends K, ? extends V> map) {
		this.map().putAll(map.map());
	}

	<T extends K> V computeIfAbsent(T key, Function<? super T, ? extends V> computer) {
		return this.map().computeIfAbsent(key, (Function<? super K, ? extends V>) computer);
	}

	private Map<K, V> map() {
		return (this.map.get() == null ? this.map = this.construct() : this.map).get();
	}

	private SoftReference<Map<K, V>> construct() {
		return new SoftReference<>(this.constructor.get());
	}
}
