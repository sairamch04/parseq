package com.linkedin.parseq;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.linkedin.parseq.promise.Promises;
import com.linkedin.parseq.promise.SettablePromise;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncIOTask {

  private static final int MILLIS_TO_NEXT_RESPONSE = 10;
  private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

  private static long millisToResponse() {
    return MILLIS_TO_NEXT_RESPONSE;
  }

  public static Task<Integer> getAsyncIOTask() {
    SettablePromise<Integer> promise = Promises.settable();
    scheduledExecutorService.schedule(new Runnable() {
      @Override
      public void run() {
        promise.done(2);
      }
    }, millisToResponse(), TimeUnit.MILLISECONDS);
    return Task.async(() -> promise);
  }

  public static CompletableFuture<Integer> getAsyncIOCompletableFuture() {
    CompletableFuture<Integer> future = new CompletableFuture<>();
    scheduledExecutorService.schedule(new Runnable() {
      @Override
      public void run() {
        future.complete(2);
      }
    }, millisToResponse(), TimeUnit.MILLISECONDS);
    return future;
  }

  public static ListenableFuture<Integer> getAsyncIOListenableFuture() {
    SettableFuture<Integer> future = SettableFuture.create();
    scheduledExecutorService.schedule(new Runnable() {
      @Override
      public void run() {
        future.set(2);
      }
    }, millisToResponse(), TimeUnit.MILLISECONDS);
    return future;
  }
}
