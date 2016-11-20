package com.github.wrdlbrnft.simplerest.tasks;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016
 */
public interface ResultCallback<T> {

    /**
     * Called when the {@link Task} is completed successfully.
     *
     * @param result The result of the {@link Task}.
     */
    void onResult(T result);
}
