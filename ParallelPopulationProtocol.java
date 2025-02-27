import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParallelPopulationProtocol {
  private static final Random random = new Random();
  private final List<Agent> agents; // *UPDATED*: Immutable list of agents.
  private final int threadCount; // *UPDATED*: Unmodifiable Number of threads used for parallel simulation.

  // --------------------------- Update ---------------------------
  // Added new related variables

  // Donation game parameters:
  private final int b = 5; // *ADDED*: Benefit parameter for the donation game.
  private final int c = 2; // *ADDED*: Cost parameter for the donation game.

  // Proportions for each agent strategy:
  // For example: 30% AC, 30% AD, and 40% GTFT.
  private final double proportionAC = 0.3; // *ADDED*: Proportion for Always Cooperate (AC) agents.
  private final double proportionAD = 0.3; // *ADDED*: Proportion for Always Defect (AD) agents.

  // Maximum generosity level for GTFT agents (k in k-IGT dynamics).
  private final int maxGenerosity = 5;
  // --------------------------------------------------------------

  public ParallelPopulationProtocol(int size, int threadCount) {
    this.threadCount = threadCount;
    List<Agent> tempAgents = new ArrayList<>();
    int countAC = (int) (size * proportionAC);
    int countAD = (int) (size * proportionAD);
    int countGTFT = size - countAC - countAD;

    for (int i = 0; i < countAC; i++) {
      tempAgents.add(new Agent("AC", maxGenerosity));
    }
    for (int i = 0; i < countAD; i++) {
      tempAgents.add(new Agent("AD", maxGenerosity));
    }
    for (int i = 0; i < countGTFT; i++) {
      tempAgents.add(new Agent("GTFT", maxGenerosity));
    }
    Collections.shuffle(tempAgents);
    this.agents = Collections.unmodifiableList(tempAgents);
  }

  // --------------------------- Update ---------------------------
  // Run the simulation for the specified number of iterations.
  // Interactions update:
  // for both game dynamics and dynamic size counting concurrently.
  public long simulate(int iterations, String filename, boolean verbose, List<ConcurrentLinkedQueue<String>> logsList) {
    Thread[] threads = new Thread[threadCount];
    long startTime = System.currentTimeMillis();
    // Use a lock-free concurrent queue for logs.
    ConcurrentLinkedQueue<String> percentLogs = new ConcurrentLinkedQueue<>();
    percentLogs.add(String.format("%n%d threadCount:%n", threadCount));
    for (int t = 0; t < threadCount; t++) {
      threads[t] = new Thread(() -> {
        for (int i = 0; i < iterations / threadCount; i++) {
          int indexA = random.nextInt(agents.size());
          int indexB = random.nextInt(agents.size());
          if (indexA != indexB) {
            agents.get(indexA).interact(agents.get(indexB), b, c);
          }
          // Log every 100 iterations:
          if (verbose && i % 100 == 0) {
            int total = agents.size();
            // AC and AD are fixed types.
            long countAC = agents.stream().filter(a -> a.getStrategy().equals("AC")).count();
            // For GTFT, expected cooperation is based on generosity level.
            double sumGTFTCoop = agents.stream()
                .filter(a -> a.getStrategy().equals("GTFT"))
                .mapToDouble(a -> a.getGenerosityLevel() / (double) maxGenerosity)
                .sum();
            double expectedCoop = countAC + sumGTFTCoop;
            double cPercentage = (expectedCoop / total) * 100;
            double dPercentage = 100 - cPercentage;
            int currentMaxGRV = getGlobalMaxGRV();
            long estimatedPopulation = (long) Math.pow(2, currentMaxGRV);
            percentLogs.add(String.format("C: %.2f%%, D: %.2f%% | Est. Population: %d%n",
                cPercentage, dPercentage, estimatedPopulation)); // *UPDATE*: print the change in percentage between cooperators and defectors
          }
        }
      });
      threads[t].start();
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    logsList.add(percentLogs); // *UPDATED*: print all logs externally after the simulation
    
    return System.currentTimeMillis() - startTime;
  }
  // --------------------------------------------------------------

  // --------------------------- Update ---------------------------
  // Get the maximum GRV among all agents (for dynamic size estimation).
  private int getGlobalMaxGRV() {
    return agents.stream().mapToInt(Agent::getMaxGRV).max().orElse(0);
  }
  // ----------------------------------------------------------------
}
