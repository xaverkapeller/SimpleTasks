# SimpleTasks

A lightweight, versatile and straight forward library for asynchronous execution of tasks on Android.

## How to add it to your project

Just add this dependency to your build.gradle file:

```groovy
compile 'com.github.wrdlbrnft:simple-tasks:0.2.0.5'
```

## How to use it

The API is designed to be used with lambda expression, so you should use [**Retrolambda**](https://github.com/evant/gradle-retrolambda) or the [**Jack Compiler**](https://developer.android.com/guide/platform/j8-jack.html) in your project. If can't or don't want to use either of them you can still use normal anonymous classes to implement each callback. However the following examples rely on lambda expressions:

```java
final TaskRunner runner = new ExecutorTaskRunner(Executors.newCachedThreadPool());
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
final TaskRunner runner = new ExecutorTaskRunner(Executors.newCachedThreadPool());
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
