package com.github.wrdlbrnft.simpletasks.caches;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 *
 * Interface for the result cache used by {@link com.github.wrdlbrnft.simpletasks.managers.TaskManager TaskManagers}.
 *
 * @param <K> Type of the keys used to identify values in the cache.
 * @param <T> Type of the values in the cache.
 */
public interface Cache<K, T> {

    /**
     * Adds a new value to the cache. If a value with the supplied key already exists then it is overwritten.
     *
     * @param key Key for the value added to the cache.
     * @param value The value added to the cache.
     */
    void put(K key, T value);

    /**
     * Gets the value associated with the supplied key from the cache. If no value for the supplied
     * key exists then {@code null} is returned.
     *
     * @param key The key identifying the value you are looking for.
     * @return Returns the value corresponding to the supplied key or {@code null} if no such value exists.
     */
    T get(K key);

    /**
     * Removes the value associated with the supplied key from the cache. If no value for the supplied key
     * exists in the cache then nothing happens.
     *
     * @param key The key identifying the value to remove from the cache.
     */
    void evict(K key);

    /**
     * Clears the cache. Removes all values stored in the cache.
     */
    void clear();
}
