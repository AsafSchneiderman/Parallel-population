import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class ParallelPopulationProtocol {
  private static final Random random = new Random();
  private List<Agent> agents;
  private int threadCount;
  private ReentrantLock lock = new ReentrantLock();
  private final Map<String, Map<String, int[]>> payoffMatrix = Map.of(
      "Cooperator", Map.of(
          "Cooperator", new int[] { 3, 3 },
          "Defector", new int[] { 0, 5 }),
      "Defector", Map.of(
          "Cooperator", new int[] { 5, 0 },
          "Defector", new int[] { 1, 1 }));

  public ParallelPopulationProtocol(int size, int threadCount) {
    this.threadCount = threadCount;
    agents = Collections.synchronizedList(new ArrayList<>());
    for (int i = 0; i < size / 2; i++) {
      agents.add(new Agent("Cooperator"));
    }
    for (int i = size / 2; i < size; i++) {
      agents.add(new Agent("Defector"));
    }
    Collections.shuffle(agents);
  }

  public long simulate(int iterations, String filename, boolean verbose) {
    Thread[] threads = new Thread[threadCount];
    long startTime = System.currentTimeMillis();
    List<String> logs = Collections.synchronizedList(new ArrayList<>());

    for (int t = 0; t < threadCount; t++) {
      threads[t] = new Thread(() -> {
        for (int i = 0; i < iterations / threadCount; i++) {
          int a = random.nextInt(agents.size());
          int b = random.nextInt(agents.size());
          if (a != b) {
            lock.lock();
            try {
              agents.get(a).interact(agents.get(b), payoffMatrix);
              agents.get(a).updateStrategy(agents.get(b));
              agents.get(b).updateStrategy(agents.get(a));
            } finally {
              lock.unlock();
            }
          }
          if (verbose && i % 10 == 0) {
            double cooperatorPercentage = getCooperatorPercentage();
            synchronized (logs) {
              logs.add(String.format("{Cooperator: %.2f%%, Defector: %.2f%%}%n",
                  cooperatorPercentage, 100 - cooperatorPercentage));
            }
          }
        }
      });
      threads[t].start();
    }

    try {
      for (Thread thread : threads) {
        thread.join();
      }

      try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
        for (String log : logs) {
          writer.print(log);
        }
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return System.currentTimeMillis() - startTime;
  }

  private double getCooperatorPercentage() {
    long count = agents.stream().filter(a -> a.getStrategy().equals("Cooperator")).count();
    return (count * 100.0) / agents.size();
  }
}
