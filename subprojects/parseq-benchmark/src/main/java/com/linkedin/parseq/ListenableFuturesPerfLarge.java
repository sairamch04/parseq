package com.linkedin.parseq;

import com.google.common.util.concurrent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ListenableFuturesPerfLarge extends AbstractFuturesBenchmark {

    private ListeningExecutorService threadpool;
    private static final int taskCount = 20;

    public static void main(String[] args) throws Exception {
        ConstantThroughputBenchmarkConfig cfg = new ConstantThroughputBenchmarkConfig();
        cfg.CONCURRENCY_LEVEL = Integer.MAX_VALUE;
        cfg.events = 1000;
        new ListenableFuturesPerfLarge().runExample(cfg);
    }

    @Override
    public void initializeExecutionThreadpool(ExecutorService threadpool) {
        this.threadpool = MoreExecutors.listeningDecorator(threadpool);
    }

    @Override
    TaskMonitor createPlan() {
        return new TaskMonitorImpl(createComputeOnlyPlan(), System.nanoTime());
    }

    private FluentFuture<?> createComputeOnlyPlan() {
        FluentFuture<String> task = FluentFuture
            .from(this.threadpool.submit(() -> "kldfjlajflskjflsjfslkajflkasj"));
        for (int i = 0; i < taskCount; i++) {
            task.transform(val -> createComputeOnlyTask(val), MoreExecutors.directExecutor());
        }
        return task;
    }

    private FluentFuture<?> createIOPlan() {
        List<ListenableFuture<String>> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(createIOTask());
        }
        return FluentFuture.from(Futures.allAsList(tasks));
    }

    private FluentFuture<String> createComputeOnlyTask(String input) {
        return FluentFuture.from(this.threadpool.submit(() -> input))
            .transform(s -> s.length(), MoreExecutors.directExecutor())
            .transform(l -> l + 1, MoreExecutors.directExecutor())
            .transform(l -> l + 2, MoreExecutors.directExecutor())
            .transform(l -> l + 3, MoreExecutors.directExecutor())
            .transformAsync(x -> Futures.immediateFuture(x * 40), MoreExecutors.directExecutor())
            .transform(x -> x - 10, MoreExecutors.directExecutor())
            .transform(String::valueOf, MoreExecutors.directExecutor());
    }

    private FluentFuture<String> createIOTask() {
        return FluentFuture.from(this.threadpool.submit(() -> "kldfjlajflskjflsjfslkajflkasj"))
                .transformAsync(x -> AsyncIOTask.getAsyncIOListenableFuture(), MoreExecutors.directExecutor())
                .transformAsync(x -> AsyncIOTask.getAsyncIOListenableFuture(), threadpool)
                .transformAsync(x -> AsyncIOTask.getAsyncIOListenableFuture(), threadpool)
                .transformAsync(x -> AsyncIOTask.getAsyncIOListenableFuture(), threadpool)
                .transformAsync(x -> AsyncIOTask.getAsyncIOListenableFuture(), threadpool)
                .transformAsync(x -> Futures.immediateFuture(x * 40), threadpool)
                .transform(x -> x - 10, MoreExecutors.directExecutor())
                .transform(String::valueOf, MoreExecutors.directExecutor());
    }

    static class TaskMonitorImpl implements TaskMonitor {
        private long startNs;
        private long endNs;
        private ListenableFuture<?> task;

        public TaskMonitorImpl(FluentFuture<?> task, long startNs) {
            this.startNs = startNs;
            this.task = task;
            task.addCallback(new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    endNs = System.nanoTime();
                }

                @Override
                public void onFailure(Throwable t) {
                    endNs = System.nanoTime();
                }
            }, MoreExecutors.directExecutor());
        }

        public long getStartNs() {
            return startNs;
        }

        public long getEndNs() {
            return endNs;
        }

        @Override
        public void await() {
            try {
                task.get();
                if (endNs == 0) {
                    endNs = System.nanoTime();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
