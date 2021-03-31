package com.linkedin.parseq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class CompletableFuturesPerfLarge extends AbstractFuturesBenchmark {

    private ExecutorService threadpool;

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
        return new TaskMonitorImpl(createParallelIOPlan(), System.nanoTime());
    }

    private CompletableFuture<?> createSerialComputeOnlyPlan() {
        CompletableFuture<String> task = CompletableFuture
            .completedFuture("kldfjlajflskjflsjfslkajflkasj");
        for (int i = 0; i < 20; i++) {
            task = task.thenComposeAsync(this::createComputeOnlyTask, threadpool);
        }
        return task;
    }

    private CompletableFuture<?> createParallelIOWithComputePlan() {
        CompletableFuture<?>[] tasks = new CompletableFuture[5];
        for (int i = 0; i <10; i++) {
            tasks[i] = createIOTask()
                .thenComposeAsync(x -> CompletableFuture.completedFuture(x * 40), threadpool)
                .thenApply(x -> x - 10);
        }
        return CompletableFuture.allOf(tasks);

    }

    private CompletableFuture<?> createParallelIOPlan() {
        CompletableFuture<?>[] tasks = new CompletableFuture[5];
        for (int i = 0; i < 10; i++) {
            tasks[i] = createIOTask();
        }
        return CompletableFuture.allOf(tasks);

    }

    private CompletableFuture<String> createComputeOnlyTask(String input) {
        return CompletableFuture.completedFuture(input)
            .thenApply(s -> s.length()).thenApply(l -> l + 1)
            .thenApply(l -> l + 2).thenApply(l -> l + 3)
            .thenCompose(x -> CompletableFuture.completedFuture(x * 40)).thenApply(x -> x - 10)
            .thenApply(String::valueOf);
    }

    private CompletableFuture<Integer> createIOTask() {
        return CompletableFuture.supplyAsync(() -> "kldfjlajflskjflsjfslkajflkasj", threadpool)
            .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
            .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
            .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool)
            .thenComposeAsync(s -> AsyncIOTask.getAsyncIOCompletableFuture(), threadpool);
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
