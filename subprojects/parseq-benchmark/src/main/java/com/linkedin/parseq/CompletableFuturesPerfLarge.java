package com.linkedin.parseq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class CompletableFuturesPerfLarge extends AbstractFuturesBenchmark {

    private ExecutorService threadpool;
    private static final int taskCount = 20;

    public static void main(String[] args) throws Exception {
        ConstantThroughputBenchmarkConfig cfg = new ConstantThroughputBenchmarkConfig();
        cfg.CONCURRENCY_LEVEL = Integer.MAX_VALUE;
        cfg.events = 1000;
        new CompletableFuturesPerfLarge().runExample(cfg);
    }

    @Override
    public void initializeExecutionThreadpool(ExecutorService threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    TaskMonitor createPlan() {
        return new TaskMonitorImpl(createComputeOnlyPlan(), System.nanoTime());
    }

    private CompletableFuture<?> createComputeOnlyPlan() {
        CompletableFuture<String> task = CompletableFuture
            .completedFuture("kldfjlajflskjflsjfslkajflkasj");
        for (int i = 0; i < taskCount; i++) {
            task.thenComposeAsync(this::createComputeOnlyTask);
        }
        return task;
    }

    private CompletableFuture<?> createIOPlan() {
        CompletableFuture<?>[] tasks = new CompletableFuture[taskCount];
        for (int i = 0; i < taskCount; i++) {
            tasks[i] = createIOTask();
        }
        return CompletableFuture.allOf(tasks);

    }

    private CompletableFuture<String> createComputeOnlyTask(String input) {
        return CompletableFuture.supplyAsync(() -> input, threadpool)
            .thenApply(s -> s.length()).thenApply(l -> l + 1)
            .thenApply(l -> l + 2).thenApply(l -> l + 3)
            .thenCompose(x -> CompletableFuture.completedFuture(x * 40)).thenApply(x -> x - 10)
            .thenApply(String::valueOf);
    }

    private CompletableFuture<?> createIOTask() {
        return CompletableFuture.supplyAsync(() -> "kldfjlajflskjflsjfslkajflkasj", threadpool)
                .thenCompose(s -> AsyncIOTask.getAsyncIOCompletableFuture())
                .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
                .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
                .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
                .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
                .thenComposeAsync(x -> CompletableFuture.completedFuture(x * 40), threadpool)
                .thenApply(x -> x - 10);
    }

    static class TaskMonitorImpl implements TaskMonitor {
        private long startNs;
        private long endNs;
        private CompletableFuture task;

        public TaskMonitorImpl(CompletableFuture task, long startNs) {
            this.startNs = startNs;
            this.task = task;
            task.whenComplete((r, e) -> {
                endNs = System.nanoTime();
            });
        }

        @Override
        public void await() {
            task.join();
        }

        public long getStartNs() {
            return startNs;
        }

        public long getEndNs() {
            return endNs;
        }
    }
}
