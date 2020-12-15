package com.linkedin.parseq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class CompletableFuturesPerfLarge extends AbstractCompletableFuturesBenchmark {

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
    CompletableFuture createPlan() {
        int taskCount = 20;
        CompletableFuture[] tasks = new CompletableFuture[taskCount];
        for (int i = 0; i < taskCount; i++) {
            tasks[i] = createTask();
        }
        return new CompletableFutureMonitor(CompletableFuture.allOf(tasks));
    }

    private CompletableFuture createTask() {
        return CompletableFuture.supplyAsync(() -> "kldfjlajflskjflsjfslkajflkasj", threadpool)
                        .thenApply(s -> s.length()).thenApply(l -> l + 1)
                        .thenApply(l -> l + 2).thenApply(l -> l + 3)
                        .thenCompose(x -> CompletableFuture.completedFuture(x * 40)).thenApply(x -> x - 10);
    }

    static class CompletableFutureMonitor extends CompletableFuture {
        private long startNs;
        private long endNs;
        private CompletableFuture task;

        public CompletableFutureMonitor(CompletableFuture task) {
            this.startNs = System.nanoTime();
            this.task = task;
            task.whenComplete((r, e) -> {
                endNs = System.nanoTime();
            });
        }

        @Override
        public Object join() {
            return task.join();
        }

        public long getStartNs() {
            return startNs;
        }

        public long getEndNs() {
            return endNs;
        }
    }
}
