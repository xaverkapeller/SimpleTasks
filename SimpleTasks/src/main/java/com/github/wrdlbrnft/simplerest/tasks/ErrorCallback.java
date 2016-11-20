package com.github.wrdlbrnft.simplerest.tasks;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016
 */
public interface ErrorCallback {

    /**
     * Called when the {@link Task} throws an {@link Exception} while executing.
     *
     * @param exception The {@link Exception} that was thrown.
     */
    void onError(Throwable exception);
}
