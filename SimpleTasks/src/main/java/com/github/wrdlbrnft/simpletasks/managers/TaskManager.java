package com.github.wrdlbrnft.simpletasks.managers;

import com.github.wrdlbrnft.simpletasks.caches.Cache;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.Task;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */

public interface TaskManager<K, T> {

    interface ModificationCallback<K, T> {
        void onModification(K key, T result);
    }

    interface Worker<K, T> {
        T perform(K value);
    }

    interface Builder<K, T> {
        Builder<K, T> useCache(Cache<K, T> cache);
        Builder<K, T> runOn(TaskRunner runner);
        Builder<K, T> onModification(ModificationCallback<K, T> callback);
        TaskManager<K, T> build();
    }

    Task<T> queue(K key);
    void invalidateCache(K key);
    void clearCache();

    static <K, T> Builder<K, T> with(Worker<K, T> worker) {
        return new TaskManagerBuilderImpl<>(worker);
    }
}
