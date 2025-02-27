import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MppRunner {
  public static void main(String[] args) {
    int size = 500; // Total number of agents.
    int iterations = 10000; // Total number of interactions.
    int[] threadCounts = { 1, 2, 4, 8, 16, 32, 64, 128 };
    List<ConcurrentLinkedQueue<String>> logsList = new ArrayList<>(); // *ADDED*: Print all percentage logs externally from the simulation
    
    ArrayList<String> TimeLogs = new ArrayList<>();
	for (int threads : threadCounts) {
	  ParallelPopulationProtocol population = new ParallelPopulationProtocol(size, threads);
	  String filename = "results_threads_" + threads + ".txt";
	  long timeTaken = population.simulate(iterations, filename, true, logsList);
	  TimeLogs.add(threads + " Threads | " + timeTaken + " ms\n");
	}

    // *UPDATED*: Print all percentage results into console instead of text files
	for (ConcurrentLinkedQueue<String> logs : logsList) {
	    for (String log : logs) {
	      System.out.print(log);
	    }
	}
	
	// *UPDATED*: Print timings results after all percentage results
	System.out.println("\nTimings results:");
    for (String timeLog : TimeLogs) {
        System.out.print(timeLog);
    }
  }
}
