package com.github.wrdlbrnft.simpletasks.caches;

import android.support.v4.util.ArrayMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 * <p>
 * Basic implementation of the {@link Cache} interface backed by a {@link Map}. This implementation
 * is thread safe and synchronizes on the {@link Map} on each call to the methods defined in
 * the {@link Cache} interface.
 *
 * @param <K> Type of the keys used to identify values in the {@link Cache}.
 * @param <T> Type of the values stored in the {@link Cache}.
 */
public class MapCache<K, T> implements Cache<K, T> {

    private final Map<K, T> mMap = Collections.synchronizedMap(new ArrayMap<K, T>());

    @Override
    public void put(K key, T item) {
        mMap.put(key, item);
    }

    @Override
    public T get(K key) {
        return mMap.get(key);
    }

    @Override
    public void evict(K key) {
        mMap.remove(key);
    }

    @Override
    public void clear() {
        mMap.clear();
    }
}
