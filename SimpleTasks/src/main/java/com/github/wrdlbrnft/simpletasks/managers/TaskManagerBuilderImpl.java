package com.github.wrdlbrnft.simpletasks.managers;

import com.github.wrdlbrnft.simpletasks.caches.Cache;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */

class TaskManagerBuilderImpl<K, T> implements TaskManager.Builder<K, T> {

    private final TaskManager.Worker<K, T> mWorker;

    private Cache<K, T> mCache;
    private TaskRunner mTaskRunner;
    private TaskManager.ModificationCallback<K, T> mModificationCallback;

    TaskManagerBuilderImpl(TaskManager.Worker<K, T> worker) {
        mWorker = worker;
    }

    @Override
    public TaskManager.Builder<K, T> useCache(Cache<K, T> cache) {
        mCache = cache;
        return this;
    }

    @Override
    public TaskManager.Builder<K, T> runOn(TaskRunner runner) {
        mTaskRunner = runner;
        return this;
    }

    @Override
    public TaskManager.Builder<K, T> onModification(TaskManager.ModificationCallback<K, T> callback) {
        mModificationCallback = callback;
        return this;
    }

    @Override
    public TaskManager<K, T> build() {
        return new TaskManagerImpl<>(
                mWorker,
                mTaskRunner,
                mCache,
                mModificationCallback
        );
    }
}
