import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MppRunner {
  public static void main(String[] args) {
    int size = 500;
    int iteretions = 10000;
    int[] threadCounts = { 1, 2, 4, 8, 16, 32, 64, 128 };

    try (PrintWriter writer = new PrintWriter(new FileWriter("results_summary.txt"))) {
      for (int threads : threadCounts) {
        ParallelPopulationProtocol population = new ParallelPopulationProtocol(size, threads);
        String filename = "results_threads_" + threads + ".txt";
        long timeTaken = population.simulate(iteretions, filename, true);
        String result = threads + " Threads |" + timeTaken + " ms";
        writer.println(result);
        System.out.println(result);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}