## How to add it to your project

Just add this dependency to your build.gradle file:

```groovy
compile 'com.github.wrdlbrnft:simple-tasks:0.3.0.13'
```

## How to use it

The API is designed to be used with lambda expression, so you should use at least build tools version `26.0.0` or above and set your language level to Java 8.

```java
final TaskRunner runner = TaskRunner.runOn(Executors.newCachedThreadPool());
final Task<Result> task = runner.queue(() -> {
    return doSomeWork();
}).onResult(result -> {
    consumeResult(result);
}).onError(exception -> {
    handleException(exception);
}).onCanceled(() -> {
    handleCancelation();
});
```

This will execute a `Task` asynchronously. When the `Task` is completed the result callback will be called. If the execution fails (when an `Exception` is thrown) the error callback is called. Should you cancel a `Task` the cancel callback is called.

You can cancel a `Task` at any point by calling `cancel()`:

```java
task.cancel();
```

This will also interrupt the execution of the `Task` if it is currently running. 

If you want to wait for the result of a `Task` synchronously you can use `await()` to do so:

```java
final TaskRunner runner = TaskRunner.runOn(Executors.newCachedThreadPool());
final Task<Result> task = runner.queue(() -> {
    return doSomeWork();
});

final Result result = task.await();
```

However in this case if the execution fails a `TaskExecutionException` is thrown! Should you cancel the `Task` a `TaskCanceledException` will be thrown.

You can also specify a timeout in milliseconds when using `await()`:

```java
final TaskRunner runner = new ExecutorTaskRunner(Executors.newCachedThreadPool());
final Task<Result> task = runner.queue(() -> {
    return doSomeWork();
});

final Result result = task.await(10000L);
```

If the timeout is reached before the `Task` completes a `TaskTimeoutException` is thrown, however if that happens the `Task` execution will not be stopped and it will continue to execute like normal. Callbacks that have been added to the `Task` will still be called when the `Task` finally completes, fails or is canceled.

For more information refer to the Javadoc!