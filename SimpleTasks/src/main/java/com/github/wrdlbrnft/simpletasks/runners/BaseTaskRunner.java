package com.github.wrdlbrnft.simpletasks.runners;

import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016<br>
 * <p>
 * A {@link TaskRunner} implementation meant to facilitate easy implementation of new
 * {@link TaskRunner TaskRunners.}. Takes care of state and queue management as well as
 * implementation specific threading concerns.
 */

public abstract class BaseTaskRunner implements TaskRunner {

    private final Queue<Runnable> mTaskQueue = new ArrayDeque<>();
    private int mState = STATE_RUNNING;

    @Override
    public final synchronized <T> Task<T> queue(Callable<T> callable) {
        final TaskImpl<T> task = new TaskImpl<>(callable);
        if (mState == STATE_RUNNING) {
            executeTask(task);
        } else {
            mTaskQueue.add(task);
        }
        return task;
    }

    protected abstract void executeTask(Runnable runnable);

    @Override
    public final synchronized int getState() {
        return mState;
    }

    @Override
    public final synchronized void start() {
        mState = STATE_RUNNING;

        Runnable runnable;
        while ((runnable = mTaskQueue.poll()) != null) {
            executeTask(runnable);
        }
    }

    @Override
    public final synchronized void stop() {
        mState = STATE_STOPPED;
    }
}
