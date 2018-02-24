package com.github.wrdlbrnft.simpletasks.runners;

import com.github.wrdlbrnft.simpletasks.tasks.Task;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018<br>
 * <p>
 * A {@link TaskRunner} implementation meant to facilitate easy implementation of new
 * {@link TaskRunner TaskRunners.}. Takes care of state and queue management as well as
 * implementation specific threading concerns.
 */
public class SimpleTaskRunner implements TaskRunner {

    public interface RunnableExecutor {
        void run(Runnable runnable);
    }

    private final Queue<Runnable> mTaskQueue = new ArrayDeque<>();
    private final RunnableExecutor mRunnableExecutor;

    private volatile int mState = STATE_RUNNING;

    public SimpleTaskRunner(RunnableExecutor consumer) {
        mRunnableExecutor = consumer;
    }

    @Override
    public final synchronized <T> Task<T> queue(Callable<T> callable) {
        final Task<T> task = Task.from(callable);
        final Runnable runnable = task.asRunnable();
        if (mState == STATE_RUNNING) {
            mRunnableExecutor.run(runnable);
        } else {
            mTaskQueue.add(runnable);
        }
        return task;
    }

    @Override
    public final synchronized int getState() {
        return mState;
    }

    @Override
    public final synchronized void start() {
        mState = STATE_RUNNING;

        Runnable runnable;
        while ((runnable = mTaskQueue.poll()) != null) {
            mRunnableExecutor.run(runnable);
        }
    }

    @Override
    public final synchronized void stop() {
        mState = STATE_STOPPED;
    }
}
