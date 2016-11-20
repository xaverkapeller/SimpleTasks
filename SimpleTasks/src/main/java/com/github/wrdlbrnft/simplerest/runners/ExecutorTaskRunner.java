package com.github.wrdlbrnft.simplerest.runners;

import com.github.wrdlbrnft.simplerest.tasks.Task;

import java.util.concurrent.Executor;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016<br>
 * <p>
 * A {@link TaskRunner} implementation which executes the {@link Task Tasks} on an {@link Executor}.
 */
public class ExecutorTaskRunner extends BaseTaskRunner {

    private final Executor mExecutor;

    public ExecutorTaskRunner(Executor executor) {
        mExecutor = executor;
    }

    @Override
    protected void executeTask(Runnable runnable) {
        mExecutor.execute(runnable);
    }
}
