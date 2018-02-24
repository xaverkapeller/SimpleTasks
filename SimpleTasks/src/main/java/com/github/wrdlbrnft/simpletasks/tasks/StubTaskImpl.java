package com.github.wrdlbrnft.simpletasks.tasks;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 */

class StubTaskImpl<T> extends TaskImpl<T> implements StubTask<T> {

    static <T> StubTaskImpl<T> newInstance() {
        return new StubTaskImpl<>(new ResultCallable<>());
    }

    private final ResultCallable<T> mCallable;

    private StubTaskImpl(ResultCallable<T> callable) {
        super(callable);
        mCallable = callable;
    }

    @Override
    public void notifyResult(T result) {
        mCallable.notifyResult(result);
        if (!mCallable.isInProgressOrDone()) {
            run();
        }
    }

    @Override
    public void notifyError(Throwable throwable) {
        mCallable.notifyError(throwable);
        if (!mCallable.isInProgressOrDone()) {
            run();
        }
    }

    private static class ResultCallable<T> implements Callable<T> {

        private final BlockingQueue<ResultWrapper<T>> mQueue = new ArrayBlockingQueue<>(1);

        private volatile boolean mInProgressOrDone = false;

        @Override
        public T call() throws Exception {
            mInProgressOrDone = true;
            final ResultWrapper<T> result = mQueue.take();
            if (result.isSuccess()) {
                return result.getResult();
            }

            throw new ExecutionException(result.getThrowable());
        }

        public boolean isInProgressOrDone() {
            return mInProgressOrDone;
        }

        public void notifyResult(T result) {
            mQueue.add(new ResultWrapper<>(result, null, true));
        }

        public void notifyError(Throwable throwable) {
            mQueue.add(new ResultWrapper<>(null, throwable, false));
        }
    }

    private static class ResultWrapper<T> {

        private final T mResult;
        private final Throwable mThrowable;
        private final boolean mSuccess;

        private ResultWrapper(T result, Throwable throwable, boolean success) {
            mResult = result;
            mThrowable = throwable;
            mSuccess = success;
        }

        public boolean isSuccess() {
            return mSuccess;
        }

        public T getResult() {
            return mResult;
        }

        public Throwable getThrowable() {
            return mThrowable;
        }
    }
}
