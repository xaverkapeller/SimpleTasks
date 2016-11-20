package com.github.wrdlbrnft.simplerest.runners;

import android.util.Log;

import com.github.wrdlbrnft.simplerest.exceptions.TaskCanceledException;
import com.github.wrdlbrnft.simplerest.exceptions.TaskExecutionException;
import com.github.wrdlbrnft.simplerest.exceptions.TaskTimeoutException;
import com.github.wrdlbrnft.simplerest.tasks.CancelCallback;
import com.github.wrdlbrnft.simplerest.tasks.ErrorCallback;
import com.github.wrdlbrnft.simplerest.tasks.ResultCallback;
import com.github.wrdlbrnft.simplerest.tasks.Task;
import com.github.wrdlbrnft.simplerest.utils.TaskUtils;

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
    private TaskResult<T> mResult;

    public TaskImpl(Callable<T> callable) {
        super(callable);
    }

    public TaskImpl(Runnable runnable, T result) {
        super(runnable, result);
    }

    @Override
    public Task<T> onResult(ResultCallback<T> callback) {
        if (isDone()) {
            final TaskResult<T> result = getResult();
            final T value = result.getResult();
            if (result.getState() == TaskResult.STATE_ERROR) {
                callback.onResult(value);
            }
        } else {
            mResultCallbacks.add(callback);
        }
        return this;
    }

    @Override
    public Task<T> onError(ErrorCallback callback) {
        if (isDone()) {
            final TaskResult<T> result = getResult();
            if (result.getState() == TaskResult.STATE_ERROR) {
                callback.onError(result.getException());
            }
        } else {
            mErrorCallbacks.add(callback);
        }
        return this;
    }

    @Override
    public Task<T> onCanceled(CancelCallback callback) {
        if (isDone()) {
            if (isCancelled()) {
                callback.onCanceled();
            }
        } else {
            mCancelCallbacks.add(callback);
        }
        return this;
    }

    @Override
    protected void done() {
        super.done();

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

    private void notifyResultCallbacks(final T value) {
        TaskUtils.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                for (ResultCallback<T> callback : mResultCallbacks) {
                    callback.onResult(value);
                }
            }
        });
    }

    private void notifyErrorCallbacks(final Throwable exception) {
        TaskUtils.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                for (ErrorCallback callback : mErrorCallbacks) {
                    callback.onError(exception);
                }
            }
        });
    }

    private void notifyCancelCallbacks() {
        TaskUtils.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                for (CancelCallback callback : mCancelCallbacks) {
                    callback.onCanceled();
                }
            }
        });
    }

    private synchronized TaskResult<T> getResult() {
        if (mResult == null) {
            try {
                final T result = get();
                mResult = new TaskResult<>(TaskResult.STATE_RESULT, result, null);
            } catch (InterruptedException e) {
                Log.v(TAG, "Exception while performing task.", e);
                mResult = new TaskResult<>(TaskResult.STATE_ERROR, null, e);
            } catch (ExecutionException e) {
                Log.v(TAG, "Exception while performing task.", e);
                mResult = new TaskResult<>(TaskResult.STATE_ERROR, null, e.getCause());
            } catch (CancellationException e) {
                Log.v(TAG, "Task was canceled.", e);
                mResult = new TaskResult<>(TaskResult.STATE_CANCELED, null, e.getCause());
            }
        }
        return mResult;
    }

    private synchronized TaskResult<T> getResult(long timeout) {
        if (mResult == null) {
            try {
                final T result = get(timeout, TimeUnit.MILLISECONDS);
                mResult = new TaskResult<>(TaskResult.STATE_RESULT, result, null);
            } catch (InterruptedException e) {
                Log.v(TAG, "Exception while performing task.", e);
                mResult = new TaskResult<>(TaskResult.STATE_ERROR, null, e);
            } catch (ExecutionException e) {
                Log.v(TAG, "Exception while performing task.", e);
                mResult = new TaskResult<>(TaskResult.STATE_ERROR, null, e.getCause());
            } catch (CancellationException e) {
                Log.v(TAG, "Task was canceled.", e);
                mResult = new TaskResult<>(TaskResult.STATE_CANCELED, null, e.getCause());
            } catch (TimeoutException e) {
                Log.v(TAG, "Exception while performing task.", e);
                return new TaskResult<>(TaskResult.STATE_TIMEOUT, null, e);
            }
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
}
