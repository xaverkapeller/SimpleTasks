package com.github.wrdlbrnft.simplerest.exceptions;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 20/11/2016
 */
public class TaskCanceledException extends RuntimeException {

    public TaskCanceledException(String message) {
        super(message);
    }

    public TaskCanceledException(String message, Throwable cause) {
        super(message, cause);
    }
}
