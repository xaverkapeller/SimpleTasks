package com.github.wrdlbrnft.simplerest.runners;

import com.github.wrdlbrnft.simplerest.tasks.Task;

import java.util.concurrent.Callable;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 19/11/2016<br>
 * <p>
 * Queues {@link Callable Callables} to be executed as {@link Task Tasks}.
 * Execution can be controlled with the {@link TaskRunner#start()}
 * and {@link TaskRunner#stop()} methods.
 * <p>
 * If the {@link TaskRunner} is started then execution of these {@link Task Tasks}
 * will be started as soon as the implementation of the {@link TaskRunner} sees fit. If the
 * {@link TaskRunner} is stopped no new {@link Task Tasks} will be eligible for
 * execution until the {@link TaskRunner} is started again and they will instead be queued
 * for future execution.
 */
public interface TaskRunner {

    /**
     * Represents the state in which the {@link TaskRunner} is actively executing
     * {@link Task Tasks}.
     */
    int STATE_RUNNING = 0x01;

    /**
     * Represents the state in which the {@link TaskRunner} is not executing any new
     * queued {@link Callable Callables}, but saving them for future execution.
     */
    int STATE_STOPPED = 0x02;

    /**
     * Queues a {@link Callable} to be executed as a {@link Task}. When and how execution of the
     * {@link Callable} takes places is defined by the implementation of the {@link TaskRunner}.
     * <p>
     * If the {@link TaskRunner} is started then execution of the {@link Callable} will be started
     * as soon as the implementation of the {@link TaskRunner} sees fit. If the {@link TaskRunner}
     * is stopped no new {@link Callable Callables} will be eligible for execution until the
     * {@link TaskRunner} is started again.
     *
     * @param callable The {@link Callable} to be executed.
     * @param <T>      The result of the {@link Callable} and {@link Task}
     * @return Returns a {@link Task} instance representing the {@link Callable}.
     */
    <T> Task<T> queue(Callable<T> callable);

    /**
     * Returns the current state of the {@link TaskRunner}.
     * The returned states may be:
     * <table summary="">
     * <tr>
     * <th>State</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>{@link TaskRunner#STATE_RUNNING}</td>
     * <td>{@link TaskRunner} is actively executing new {@link Task Tasks}.</td>
     * </tr>
     * <tr>
     * <td>{@link TaskRunner#STATE_STOPPED}</td>
     * <td>
     * {@link TaskRunner} is not executing new {@link Task Tasks}, but
     * queueing them for future execution.
     * </td>
     * </tr>
     * </table>
     *
     * @return Returns the current state of the {@link TaskRunner}.
     */
    int getState();

    /**
     * Starts the {@link TaskRunner}. If any new {@link Task Tasks} have been queued while the
     * {@link TaskRunner} was stopped then they will be executed as soon as the implementation
     * of the {@link TaskRunner} sees fit.
     */
    void start();

    /**
     * Stops the {@link TaskRunner}. Does not cancel currently running {@link Task Tasks}.
     * New {@link Task Tasks} queued while the {@link TaskRunner} is stopped will not be executed
     * until the {@link TaskRunner} is started again.
     */
    void stop();
}
