package net.auoeke.reflect;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 @since 4.0.0
 */
sealed abstract class CacheMap<K, V> {
	private SoftReference<Map<K, V>> map = new SoftReference<>(null);

	static <K, V> CacheMap<K, V> identity() {
		return new Identity<>();
	}

	static <K, V> CacheMap<K, V> hash() {
		return new Hash<>();
	}

	abstract Map<K, V> construct();

	final V get(K key) {
		return this.map().get(key);
	}

	final V put(K key, V value) {
		return this.map().put(key, value);
	}

	final void putAll(CacheMap<? extends K, ? extends V> map) {
		this.map().putAll(map.map());
	}

	final <T extends K> V computeIfAbsent(T key, Function<? super T, ? extends V> computer) {
		return this.map().computeIfAbsent(key, (Function<? super K, ? extends V>) computer);
	}

	private Map<K, V> map() {
		return (this.map.refersTo(null) ? this.map = new SoftReference<>(this.construct()) : this.map).get();
	}

	static final class Identity<K, V> extends CacheMap<K, V> {
		@Override Map<K, V> construct() {
			return new IdentityHashMap<>();
		}
	}

	static final class Hash<K, V> extends CacheMap<K, V> {
		@Override Map<K, V> construct() {
			return new HashMap<>();
		}
	}
}
