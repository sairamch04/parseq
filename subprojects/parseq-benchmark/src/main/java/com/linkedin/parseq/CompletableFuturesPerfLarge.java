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
        int taskCount = 20;
        CompletableFuture[] tasks = new CompletableFuture[taskCount];
        for (int i = 0; i < taskCount; i++) {
            tasks[i] = createTask();
        }
        return new TaskMonitorImpl(CompletableFuture.allOf(tasks));
    }

    private CompletableFuture createTask() {
        return CompletableFuture.supplyAsync(() -> "kldfjlajflskjflsjfslkajflkasj", threadpool)
                        .thenApply(s -> s.length()).thenApply(l -> l + 1)
                        .thenApply(l -> l + 2).thenApply(l -> l + 3)
                        .thenCompose(x -> CompletableFuture.completedFuture(x * 40)).thenApply(x -> x - 10);
    }

    static class TaskMonitorImpl implements TaskMonitor {
        private long startNs;
        private long endNs;
        private CompletableFuture task;

        public TaskMonitorImpl(CompletableFuture task) {
            this.startNs = System.nanoTime();
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
