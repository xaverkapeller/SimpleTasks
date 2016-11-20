package com.github.wrdlbrnft.simplerest.runners;

import android.os.Handler;

import com.github.wrdlbrnft.simplerest.tasks.Task;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 20/11/2016<br>
 * <p>
 * A {@link TaskRunner} implementation which executes the {@link Task Tasks} on a {@link Handler}.
 */

public class HandlerTaskRunner extends BaseTaskRunner {

    private final Handler mHandler;

    public HandlerTaskRunner(Handler handler) {
        mHandler = handler;
    }

    @Override
    protected void executeTask(Runnable runnable) {
        mHandler.post(runnable);
    }
}
