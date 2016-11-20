package com.github.wrdlbrnft.simpletasks.runners;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016
 */
class TaskResult<T> {

    public static final int STATE_RESULT = 0x01;
    public static final int STATE_ERROR = 0x02;
    public static final int STATE_TIMEOUT = 0x04;
    public static final int STATE_CANCELED = 0x08;

    private final int mState;
    private final T mResult;
    private final Throwable mError;

    TaskResult(int state, T result, Throwable error) {
        mState = state;
        mResult = result;
        mError = error;
    }

    public int getState() {
        return mState;
    }

    public T getResult() {
        return mResult;
    }

    public Throwable getException() {
        return mError;
    }
}
