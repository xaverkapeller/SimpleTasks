package com.github.wrdlbrnft.simpletasks.managers;

import android.support.v4.util.ArrayMap;

import com.github.wrdlbrnft.simpletasks.caches.Cache;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.util.Map;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */

class TaskManagerImpl<K, T> implements TaskManager<K, T> {

    private final Map<K, Task<T>> mTaskMap = new ArrayMap<>();

    private final Worker<K, T> mWorker;
    private final TaskRunner mTaskRunner;
    private final Cache<K, T> mCache;
    private final ModificationCallback<K, T> mModificationCallback;

    TaskManagerImpl(Worker<K, T> worker, TaskRunner taskRunner, Cache<K, T> cache, ModificationCallback<K, T> modificationCallback) {
        mWorker = worker;
        mTaskRunner = taskRunner;
        mCache = cache;
        mModificationCallback = modificationCallback;
    }

    @Override
    public Task<T> queue(K key) {
        synchronized (mTaskMap) {
            if (mCache != null) {
                final T cachedValue = mCache.get(key);
                if (cachedValue != null) {
                    return Task.withResult(cachedValue);
                }
            }

            final Task<T> cachedTask = mTaskMap.get(key);
            if (cachedTask != null) {
                return cachedTask;
            }

            final Task<T> task = mTaskRunner.queue(() -> mWorker.perform(key)).onResult(result -> {
                synchronized (mTaskMap) {
                    try {
                        if (result == null) {
                            return;
                        }
                        if (mCache != null) {
                            mCache.put(key, result);
                        }

                        if (mModificationCallback != null) {
                            mModificationCallback.onModification(key, result);
                        }
                    } finally {
                        mTaskMap.remove(key);
                    }
                }
            }).onError(exception -> {
                synchronized (mTaskMap) {
                    mTaskMap.remove(key);
                }
            }).onCanceled(() -> {
                synchronized (mTaskMap) {
                    mTaskMap.remove(key);
                }
            });
            mTaskMap.put(key, task);
            return task;
        }
    }

    @Override
    public void invalidateCache(K id) {
        synchronized (mTaskMap) {
            if (mCache != null) {
                mCache.evict(id);
            }
            mTaskMap.remove(id);
        }
    }

    @Override
    public void clearCache() {
        synchronized (mTaskMap) {
            if (mCache != null) {
                mCache.clear();
            }
            mTaskMap.clear();
        }
    }
}
