package com.linkedin.parseq;

import com.google.common.util.concurrent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ListenableFuturesPerfLarge extends AbstractFuturesBenchmark {

    private ListeningExecutorService threadpool;

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
        int taskCount = 20;
        List<ListenableFuture<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(createTask());
        }
        return new TaskMonitorImpl(FluentFuture.from(Futures.allAsList(tasks)));
    }

    private FluentFuture<Integer> createTask() {
        return FluentFuture.from(this.threadpool.submit(() -> "kldfjlajflskjflsjfslkajflkasj"))
                .transform(s -> s.length(), MoreExecutors.directExecutor())
                .transform(l -> l + 1, MoreExecutors.directExecutor())
                .transform(l -> l + 2, MoreExecutors.directExecutor())
                .transform(l -> l + 3, MoreExecutors.directExecutor())
                .transformAsync(x -> Futures.immediateFuture(x * 40), MoreExecutors.directExecutor())
                .transform(x -> x - 10, MoreExecutors.directExecutor());
    }

    static class TaskMonitorImpl implements TaskMonitor {
        private long startNs;
        private long endNs;
        private ListenableFuture<List<Integer>> task;

        public TaskMonitorImpl(FluentFuture<List<Integer>> task) {
            this.task = task;
            task.addCallback(new FutureCallback<List<Integer>>() {
                @Override
                public void onSuccess(List<Integer> result) {
                    endNs = System.nanoTime();
                }

                @Override
                public void onFailure(Throwable t) {
                    endNs = System.nanoTime();
                }
            }, MoreExecutors.directExecutor());
            this.startNs = System.nanoTime();
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
