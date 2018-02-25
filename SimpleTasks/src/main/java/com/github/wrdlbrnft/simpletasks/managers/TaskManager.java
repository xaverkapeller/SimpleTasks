package com.github.wrdlbrnft.simpletasks.managers;

import com.github.wrdlbrnft.simpletasks.caches.Cache;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;
import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.util.concurrent.Callable;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 * <p>
 * A {@link TaskManager} can be used to:
 * <ul>
 * <li>Prevent multiple instances of the same task running at once.</li>
 * <li>Cache results and to stop the relevant task to run again until the cache is invalidated.</li>
 * </ul>
 * <p>
 * A {@link TaskManager} can be created by using the static {@link TaskManager#with(Worker)} method.
 * Example:
 * <pre>
 *     TaskManager&#60;String, Model&#62; taskManager = TaskManager
 *             .&#60;String, Model&#62;with(key -&#62; {
 *                 return loadModel(key);
 *             })
 *             .runOn(taskRunner)
 *             .useCache(new MapCache&#60;&#62;())
 *             .onModification((key, model) -&#62; {
 *                 notifyResultChanged(key, model);
 *             })
 *             .build();
 *
 *     Task&#60;Model&#62; task = taskManager.key("string input");
 * </pre>
 *
 * @param <K> Type of the keys/inputs for {@link Task Tasks} started by the {@link TaskManager}.
 * @param <T> Type of the results produced by {@link Task Tasks} started by the {@link TaskManager}.
 */
public interface TaskManager<K, T> {

    /**
     * This callback can be set in a {@link TaskManager.Builder} instance and is used to
     * listen for changes in the internal state of the {@link TaskManager} independently of any
     * {@link Task} run by the {@link TaskManager}. It is called every time a {@link Task} triggered
     * by the {@link TaskManager} finishes or if a new entry is set in the {@link Cache}.
     *
     * @param <K> Type of the keys/inputs for {@link Task Tasks} started by the {@link TaskManager}.
     * @param <T> Type of the results produced by {@link Task Tasks} started by the {@link TaskManager}.
     */
    interface ModificationCallback<K, T> {

        /**
         * This method is called every time a {@link Task} triggered by the {@link TaskManager}
         * finishes or if a new entry is set in the {@link Cache}.
         *
         * @param key    The key used to queue the {@link Task} in the {@link TaskManager}.
         * @param result The result of the {@link Task}.
         */
        void onModification(K key, T result);
    }

    /**
     * This interface is used in the {@link TaskManager} to perform the actual work. It works
     * just like the {@link Callable} instances passed to a {@link TaskRunner} when queuing a
     * {@link Task} - the only difference being that a {@link Worker} accepts an input value as
     * parameter while the usual {@link Callable Callables} do not.
     *
     * @param <K> The type of the inputs passed to the {@link Worker} instance.
     * @param <T> The type of the results produced by the {@link Worker} instance.
     */
    interface Worker<K, T> {
        T perform(K value);
    }

    /**
     * This class is used to construct new {@link TaskManager} instances.
     *
     * @param <K> The type of the keys/inputs used to trigger {@link Task Tasks} in the {@link TaskManager}.
     * @param <T> The result type of {@link Task Tasks} triggered by this {@link TaskManager}.
     */
    interface Builder<K, T> {

        /**
         * With this method you can set the {@link Cache} instance used to cache results in the
         * {@link TaskManager}. If no {@link Cache} is set then no results will be cached.
         *
         * @param cache The {@link Cache} you want to use to cache results.
         * @return Returns the same {@link Builder} instance to enable call chaining.
         */
        Builder<K, T> useCache(Cache<K, T> cache);

        /**
         * With the this method you can set the {@link TaskRunner} which will be used to run the
         * underlying {@link Task Tasks}. If no {@link TaskRunner} is set an exception will be
         * thrown when {@link Builder#build()} is called.
         *
         * @param runner The {@link TaskRunner} used to run {@link Task Tasks} in the {@link TaskManager}.
         * @return Returns the same {@link Builder} instance to enable call chaining.
         */
        Builder<K, T> runOn(TaskRunner runner);

        /**
         * With this method you can set a {@link ModificationCallback} which will be called each
         * time any {@link Task} created by the {@link TaskManager} finishes and/or when the
         * {@link Cache} gets a new entry. It can be used to listen for changes in the internal s
         * tate of the {@link TaskManager} independently of any {@link Task} run by the
         * {@link TaskManager}.
         *
         * @param callback The {@link ModificationCallback} instance you want to attach to the {@link TaskManager}
         * @return Returns the same {@link Builder} instance to enable call chaining.
         */
        Builder<K, T> onModification(ModificationCallback<K, T> callback);

        /**
         * Creates a new {@link TaskManager} instance with the options set on this {@link Builder}.
         *
         * @return Returns a new {@link TaskManager} with the options set on this {@link Builder}.
         */
        TaskManager<K, T> build();
    }

    /**
     * Queues a new {@link Task} for the supplied key.
     * <p>
     * If a task for this key is already running then the running task will be returned.
     * <p>
     * If a result associated with this key is cached then already completed {@link Task} will
     * be returned.
     *
     * @param key Input for the underlying {@link Worker} and key for caching the result.
     * @return Returns new {@link Task} for the supplied key.
     */
    Task<T> queue(K key);

    /**
     * Invalidates a specific entry in the {@link Cache} associated with this {@link TaskManager}.
     * If the {@link TaskManager} has no {@link Cache} the nothing happens.
     *
     * @param key The key identifying the value in the {@link Cache} which should be invalidated.
     */
    void invalidateCache(K key);

    /**
     * Clears the {@link Cache} associated with this {@link TaskManager}. If the {@link TaskManager}
     * has no {@link Cache} the nothing happens.
     */
    void clearCache();

    /**
     * Creates a {@link Builder} instance used to construct new {@link TaskManager TaskManagers}.
     * <p>
     * The supplied {@link Worker} instance supplied is akin to the {@link Callable} supplied to a
     * {@link TaskRunner} when queuing a {@link Task}, it performs the work to load a result.
     *
     * @param worker The {@link Worker} instance used to perform the work in the {@link TaskManager}.
     * @param <K>    Type of input data for the {@link TaskManager}.
     * @param <T>    Type of the result produced by the {@link TaskManager}
     * @return Returns a {@link Builder} instance which can be used to construct a new {@link TaskManager}.
     */
    static <K, T> Builder<K, T> with(Worker<K, T> worker) {
        return new TaskManagerBuilderImpl<>(worker);
    }
}
