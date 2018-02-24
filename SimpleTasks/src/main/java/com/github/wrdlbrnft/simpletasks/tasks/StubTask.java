package com.github.wrdlbrnft.simpletasks.tasks;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */
public interface StubTask<T> extends Task<T> {

    void notifyResult(T result);
    void notifyError(Throwable throwable);

    static <T> StubTask<T> create() {
        return StubTaskImpl.newInstance();
    }
}
