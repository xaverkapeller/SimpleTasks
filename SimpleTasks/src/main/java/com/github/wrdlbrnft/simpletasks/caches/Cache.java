package com.github.wrdlbrnft.simpletasks.caches;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */
public interface Cache<K, T> {
    void put(K key, T item);
    T get(K key);
    void evict(K key);
    void clear();
}
