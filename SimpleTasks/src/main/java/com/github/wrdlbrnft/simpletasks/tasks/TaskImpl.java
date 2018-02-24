package com.github.wrdlbrnft.simpletasks.tasks;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.wrdlbrnft.simpletasks.exceptions.TaskCanceledException;
import com.github.wrdlbrnft.simpletasks.exceptions.TaskExecutionException;
import com.github.wrdlbrnft.simpletasks.exceptions.TaskTimeoutException;
import com.github.wrdlbrnft.simpletasks.utils.TaskUtils;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016
 */

class TaskImpl<T> extends FutureTask<T> implements Task<T> {

    private static final String TAG = "TaskImpl";

    private final List<ResultCallback<T>> mResultCallbacks = new Vector<>();
    private final List<ErrorCallback> mErrorCallbacks = new Vector<>();
    private final List<CancelCallback> mCancelCallbacks = new Vector<>();

    private final Object mLock = new Object();

    private TaskResult<T> mResult;

    TaskImpl(Callable<T> callable) {
        super(callable);
    }

    @Override
    public Task<T> onResult(ResultCallback<T> callback) {
        synchronized (mLock) {
            if (isDone()) {
                final TaskResult<T> result = getResult();
                final T value = result.getResult();
                if (result.getState() == TaskResult.STATE_RESULT) {
                    callback.onResult(value);
                }
            } else {
                mResultCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    public Task<T> onResult(Lifecycle lifecycle, ResultCallback<T> callback) {
        synchronized (mLock) {
            if (isDone()) {
                final TaskResult<T> result = getResult();
                final T value = result.getResult();
                if (result.getState() == TaskResult.STATE_RESULT) {
                    callback.onResult(value);
                }
            } else {
                final LifecycleAwareResultCallback<T> lifecycleAwareResultCallback = new LifecycleAwareResultCallback<>(callback);
                mResultCallbacks.add(lifecycleAwareResultCallback);
                lifecycle.addObserver(lifecycleAwareResultCallback);
            }
        }
        return this;
    }

    @Override
    public Task<T> onError(ErrorCallback callback) {
        synchronized (mLock) {
            if (isDone()) {
                final TaskResult<T> result = getResult();
                if (result.getState() == TaskResult.STATE_ERROR) {
                    callback.onError(result.getException());
                }
            } else {
                mErrorCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    public Task<T> onError(Lifecycle lifecycle, ErrorCallback callback) {
        synchronized (mLock) {
            if (isDone()) {
                final TaskResult<T> result = getResult();
                if (result.getState() == TaskResult.STATE_ERROR) {
                    callback.onError(result.getException());
                }
            } else {
                final LifecycleAwareErrorCallback lifecycleAwareErrorCallback = new LifecycleAwareErrorCallback(callback);
                lifecycle.addObserver(lifecycleAwareErrorCallback);
                mErrorCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    public Task<T> onCanceled(CancelCallback callback) {
        synchronized (mLock) {
            if (isDone()) {
                if (isCancelled()) {
                    callback.onCanceled();
                }
            } else {
                mCancelCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    public Task<T> onCanceled(Lifecycle lifecycle, CancelCallback callback) {
        synchronized (mLock) {
            if (isDone()) {
                if (isCancelled()) {
                    callback.onCanceled();
                }
            } else {
                final LifecycleAwareCancelCallback lifecycleAwareCancelCallback = new LifecycleAwareCancelCallback(callback);
                lifecycle.addObserver(lifecycleAwareCancelCallback);
                mCancelCallbacks.add(callback);
            }
        }
        return this;
    }

    @Override
    protected void done() {
        super.done();

        synchronized (mLock) {
            final TaskResult<T> result = getResult();
            final int state = result.getState();
            switch (state) {

                case TaskResult.STATE_RESULT:
                    final T value = result.getResult();
                    notifyResultCallbacks(value);
                    break;

                case TaskResult.STATE_TIMEOUT:
                case TaskResult.STATE_ERROR:
                    final Throwable exception = result.getException();
                    notifyErrorCallbacks(exception);
                    break;

                case TaskResult.STATE_CANCELED:
                    notifyCancelCallbacks();
                    break;

                default:
                    throw new IllegalStateException("Unknown result state: " + state);
            }
        }
    }

    private void notifyResultCallbacks(final T value) {
        TaskUtils.MAIN_HANDLER.post(() -> {
            for (ResultCallback<T> callback : mResultCallbacks) {
                callback.onResult(value);
            }
        });
    }

    private void notifyErrorCallbacks(final Throwable exception) {
        TaskUtils.MAIN_HANDLER.post(() -> {
            for (ErrorCallback callback : mErrorCallbacks) {
                callback.onError(exception);
            }
        });
    }

    private void notifyCancelCallbacks() {
        TaskUtils.MAIN_HANDLER.post(() -> {
            for (CancelCallback callback : mCancelCallbacks) {
                callback.onCanceled();
            }
        });
    }

    public interface ResultResolver<T> {
        T get() throws InterruptedException, ExecutionException, CancellationException, TimeoutException;
    }

    @NonNull
    private TaskResult<T> parseResult(ResultResolver<T> resolver) {
        try {
            final T result = resolver.get();
            return new TaskResult<>(TaskResult.STATE_RESULT, result, null);
        } catch (InterruptedException e) {
            Log.v(TAG, "Exception while performing task.", e);
            return new TaskResult<>(TaskResult.STATE_ERROR, null, e);
        } catch (ExecutionException e) {
            Log.v(TAG, "Exception while performing task.", e);
            return new TaskResult<>(TaskResult.STATE_ERROR, null, e.getCause());
        } catch (CancellationException e) {
            Log.v(TAG, "Task was canceled.", e);
            return new TaskResult<>(TaskResult.STATE_CANCELED, null, e.getCause());
        } catch (TimeoutException e) {
            Log.v(TAG, "Exception while performing task.", e);
            return new TaskResult<>(TaskResult.STATE_TIMEOUT, null, e);
        }
    }

    private synchronized TaskResult<T> getResult() {
        if (mResult == null) {
            mResult = parseResult(this::get);
        }
        return mResult;
    }

    private synchronized TaskResult<T> getResult(long timeout) {
        if (mResult == null) {
            mResult = parseResult(() -> get(timeout, TimeUnit.MILLISECONDS));
        }
        return mResult;
    }

    @Override
    public T await() {
        final TaskResult<T> result = getResult();
        if (result.getState() == TaskResult.STATE_RESULT) {
            return result.getResult();
        } else if (result.getState() == TaskResult.STATE_CANCELED) {
            throw new TaskCanceledException("Task was canceled.");
        } else {
            throw new TaskExecutionException("Task could not be completed.", result.getException());
        }
    }

    @Override
    public T await(long timeout) {
        final TaskResult<T> result = getResult(timeout);
        if (result.getState() == TaskResult.STATE_RESULT) {
            return result.getResult();
        } else if (result.getState() == TaskResult.STATE_TIMEOUT) {
            throw new TaskTimeoutException("Timeout waiting for task to complete.", result.getException());
        } else if (result.getState() == TaskResult.STATE_CANCELED) {
            throw new TaskCanceledException("Task was canceled.");
        } else {
            throw new TaskExecutionException("Task could not be completed.", result.getException());
        }
    }

    @Override
    public void cancel() {
        cancel(true);
    }

    @Override
    public Runnable asRunnable() {
        return this;
    }

    private static class LifecycleAwareDelegate<D> implements LifecycleObserver {

        public interface Receiver<R> {
            void onReceive(R receiver);
        }

        private D mDelegate;

        public LifecycleAwareDelegate(D delegate) {
            mDelegate = delegate;
        }

        protected void withDelegate(@NonNull Receiver<D> receiver) {
            if (mDelegate != null) {
                receiver.onReceive(mDelegate);
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            mDelegate = null;
        }
    }

    private static class LifecycleAwareCancelCallback extends LifecycleAwareDelegate<CancelCallback> implements CancelCallback {

        public LifecycleAwareCancelCallback(CancelCallback delegate) {
            super(delegate);
        }

        @Override
        public void onCanceled() {
            withDelegate(CancelCallback::onCanceled);
        }
    }

    private static class LifecycleAwareResultCallback<R> extends LifecycleAwareDelegate<ResultCallback<R>> implements ResultCallback<R> {

        public LifecycleAwareResultCallback(ResultCallback<R> delegate) {
            super(delegate);
        }

        @Override
        public void onResult(R result) {
            withDelegate(receiver -> receiver.onResult(result));
        }
    }

    private static class LifecycleAwareErrorCallback extends LifecycleAwareDelegate<ErrorCallback> implements ErrorCallback {

        public LifecycleAwareErrorCallback(ErrorCallback delegate) {
            super(delegate);
        }

        @Override
        public void onError(Throwable exception) {
            withDelegate(receiver -> receiver.onError(exception));
        }
    }
}
