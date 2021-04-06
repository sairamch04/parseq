/* $Id$ */
package com.linkedin.parseq;

import java.util.ArrayList;
import java.util.List;

import com.linkedin.parseq.Task;
import com.linkedin.parseq.Tasks;


/**
 * @author Jaroslaw Odzga (jodzga@linkedin.com)
 */
public class PerfLarge extends AbstractBenchmark {

  public static void main(String[] args) throws Exception {
//    FullLoadBenchmarkConfig cfg = new FullLoadBenchmarkConfig();
    ConstantThroughputBenchmarkConfig cfg = new ConstantThroughputBenchmarkConfig();
    cfg.CONCURRENCY_LEVEL = Integer.MAX_VALUE;
    cfg.events = 1000;
    new PerfLarge().runExample(cfg);
  }

  @Override
  Task<?> createPlan() {
    return computeOnlyPlan();
  }

  private Task<?> computeOnlyPlan() {
    Task<String> stringTask = Task.value("kldfjlajflskjflsjfslkajflkasj");
    for (int i = 0; i < 20; i++) {
      stringTask = stringTask.flatMap(this::task);
    }
    return stringTask;
  }

  private Task<?> createParallelIOPlan() {
    List<Task<?>> l = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      l.add(ioTask());
    }
    return Tasks.par(l);

  }

  private Task<?> createParallelIOWithComputePlan() {
    List<Task<?>> l = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      l.add(ioTask().flatMap(x -> Task.value(x * 40))
          .map(x -> x - 10));
    }
    return Tasks.par(l);

  }


  private Task<String> task(String input) {
    return Task.value(input).map("length", s -> s.length()).map("+1", s -> s + 1)
        .map("+2", s -> s + 2).map("+3", s -> s + 3)
        .flatMap(x -> Task.value(x * 40)).map(x -> x -10).map(String::valueOf);
  }

  private Task<Integer> ioTask() {
    return Task.value("kldfjlajflskjflsjfslkajflkasj")
        .flatMap("IO", x -> AsyncIOTask.getAsyncIOTask())
        .flatMap("IO2", x -> AsyncIOTask.getAsyncIOTask())
        .flatMap("IO3", x -> AsyncIOTask.getAsyncIOTask())
        .flatMap("IO4", x -> AsyncIOTask.getAsyncIOTask())
        .flatMap("IO5", x -> AsyncIOTask.getAsyncIOTask());
  }
}
