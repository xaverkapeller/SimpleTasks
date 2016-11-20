package com.github.wrdlbrnft.simpletasks.exceptions;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016
 */
public class TaskExecutionException extends RuntimeException {

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
