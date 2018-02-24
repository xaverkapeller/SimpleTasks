package com.github.wrdlbrnft.simpletasks.tasks;

import android.arch.lifecycle.Lifecycle;

import com.github.wrdlbrnft.simpletasks.exceptions.TaskCanceledException;
import com.github.wrdlbrnft.simpletasks.exceptions.TaskExecutionException;
import com.github.wrdlbrnft.simpletasks.exceptions.TaskTimeoutException;
import com.github.wrdlbrnft.simpletasks.runners.TaskRunner;

import java.util.concurrent.Callable;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016<br>
 * <p>
 * Represents an operation executed by a {@link TaskRunner}.
 */
public interface Task<T> {

    /**
     * Adds a {@link ResultCallback} to this {@link Task} which is called when the {@link Task}
     * completes successfully.
     * <p>
     * The {@link ResultCallback} is executed on the applications main thread.
     *
     * @param callback The {@link ResultCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onResult(ResultCallback<T> callback);

    /**
     * Adds a lifecycle aware {@link ResultCallback} to this {@link Task} which is called when the {@link Task}
     * completes successfully. If the owner of the supplied lifecycle is destroyed the callback is
     * detached automatically.
     * <p>
     * The {@link ResultCallback} is executed on the applications main thread.
     *
     * @param lifecycle The {@link Lifecycle} which should be observed for this callback.
     * @param callback  The {@link ResultCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onResult(Lifecycle lifecycle, ResultCallback<T> callback);

    /**
     * Adds an {@link ErrorCallback} to this {@link Task} which is called when the {@link Task}
     * throws an {@link Exception}.
     * <p>
     * The {@link ErrorCallback} is executed on the applications main thread.
     *
     * @param callback The {@link ErrorCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onError(ErrorCallback callback);

    /**
     * Adds a lifecycle aware {@link ErrorCallback} to this {@link Task} which is called when the {@link Task}
     * throws an {@link Exception}. If the owner of the supplied lifecycle is destroyed the callback is
     * detached automatically.
     * <p>
     * The {@link ErrorCallback} is executed on the applications main thread.
     *
     * @param lifecycle The {@link Lifecycle} which should be observed for this callback.
     * @param callback  The {@link ErrorCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onError(Lifecycle lifecycle, ErrorCallback callback);

    /**
     * Adds a {@link CancelCallback} to this {@link Task} which is called when the {@link Task}
     * is canceled.
     * <p>
     * The {@link CancelCallback} is executed on the applications main thread.
     *
     * @param callback The {@link CancelCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onCanceled(CancelCallback callback);

    /**
     * Adds a lifecycle aware {@link CancelCallback} to this {@link Task} which is called when the {@link Task}
     * is canceled. If the owner of the supplied lifecycle is destroyed the callback is
     * detached automatically.
     * <p>
     * The {@link CancelCallback} is executed on the applications main thread.
     *
     * @param lifecycle The {@link Lifecycle} which should be observed for this callback.
     * @param callback  The {@link CancelCallback} that will be attached to the {@link Task}.
     * @return Returns the same {@link Task} instance to enable call chaining.
     */
    Task<T> onCanceled(Lifecycle lifecycle, CancelCallback callback);

    /**
     * Waits for the {@link Task} to complete and then returns the result.
     * <p>
     * If the {@link Task} throws an {@link Exception} while executing than
     * a {@link TaskExecutionException} is thrown.
     * </p>
     * <p>
     * If the {@link Task} is canceled than a {@link TaskCanceledException} is thrown.
     * </p>
     *
     * @return Returns the result of the {@link Task}.
     * @throws TaskExecutionException Thrown if the {@link Task} throws an
     *                                {@link Exception} while executing.
     * @throws TaskCanceledException  Thrown if the {@link Task} is canceled.
     */
    T await() throws TaskExecutionException, TaskCanceledException;

    /**
     * Waits for the {@link Task} to complete for the supplied amount of time.
     * <p>
     * If the {@link Task} is complete before the timeout then result of the
     * {@link Task} will be returned.
     * </p>
     * <p>
     * If the timeout is reached then a {@link TaskTimeoutException} is thrown.
     * </p>
     * <p>
     * If the {@link Task} throws an {@link Exception} while executing than
     * a {@link TaskExecutionException} is thrown.
     * </p>
     * <p>
     * If the {@link Task} is canceled than a {@link TaskCanceledException} is thrown.
     * </p>
     *
     * @param timeout The amount of time in milliseconds which should be waited for
     *                the {@link Task} to be completed.
     * @return Returns the result of the {@link Task}.
     * @throws TaskExecutionException Thrown if the {@link Task} throws an
     *                                {@link Exception} while executing.
     * @throws TaskCanceledException  Thrown if the {@link Task} is canceled.
     * @throws TaskTimeoutException   Thrown if the timeout is reached before the
     *                                {@link Task} is completed.
     */
    T await(long timeout) throws TaskExecutionException, TaskCanceledException, TaskTimeoutException;

    /**
     * Cancels the {@link Task}.
     */
    void cancel();

    /**
     * Returns a {@link Runnable} which encapsulates this {@link Task}.
     *
     * @return Returns this {@link Task} as a {@link Runnable}.
     */
    Runnable asRunnable();

    /**
     * Creates a new {@link Task} from a {@link Callable}.
     *
     * @param callable The {@link Callable} which contains the work to be executed in the {@link Task}.
     * @param <T>      The type of the result of the {@link Task}.
     * @return Returns a new {@link Task} instance which will run the supplied {@link Callable} when executed.
     */
    static <T> Task<T> from(Callable<T> callable) {
        return new TaskImpl<>(callable);
    }

    /**
     * Creates a new - already completed - {@link Task} with a specific result. Attaching a
     * {@link ResultCallback} or calling {@link Task#await()} yields the supplied result immediately.
     * <p>
     * Aside from not needing to be executed this task supports all features of normal
     * {@link Task Tasks} - like lifecycle aware callbacks - and behaves exactly like a real
     * {@link Task} created by a {@link TaskRunner}.
     *
     * @param result The result of the {@link Task}.
     * @param <T>    The type of the result.
     * @return Returns a new - already completed - {@link Task} which yields the supplied result.
     */
    static <T> Task<T> withResult(T result) {
        final StubTask<T> task = StubTask.create();
        task.notifyResult(result);
        return task;
    }

    /**
     * Creates a new - already completed - {@link Task} with a specific result. Attaching a
     * {@link ErrorCallback} or calling {@link Task#await()} yields the supplied error immediately.
     * <p>
     * Aside from not needing to be executed this task supports all features of normal
     * {@link Task Tasks} - like lifecycle aware callbacks - and behaves exactly like a real
     * {@link Task} created by a {@link TaskRunner}.
     *
     * @param throwable The error which should be thrown by this {@link Task}.
     * @param <T>       The type of the result of the {@link Task}.
     * @return Returns a new - already completed - {@link Task} which yields the supplied error.
     */
    static <T> Task<T> withError(Throwable throwable) {
        final StubTask<T> task = StubTask.create();
        task.notifyError(throwable);
        return task;
    }
}
