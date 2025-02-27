import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Agent {
  private final String strategy; // *UPDATED*: "AC", "AD", or "GTFT" (instead of C or D only)
  private final AtomicInteger score; // *UPDATED*: type is now AtomicInteger

  // -------------------------------- Update --------------------------------
  // Added new related variables
  private final AtomicInteger generosityLevel; // *ADDED*: For GTFT agents (range: 1 ... maxGenerosity)
  private final int maxGenerosity; // *ADDED*: Maximum generosity level (k, e.g., 5)
  private final AtomicInteger maxGRV; // *ADDED*: For dynamic size counting: stores the current maximum GRV
  private final AtomicInteger resetTimer; // *ADDED*: Countdown timer for GRV reset

  private static final Random rand = new Random();
  private static final int TAU = 10; // *ADDED*: Constant factor for timer reset period
  // ------------------------------------------------------------------------

  // -------------------------------- Update --------------------------------
  // 1. Added GTFT strategy with generosity level.
  // 2. Introduced maxGRV and resetTimer for dynamic size counting.
  // 3. Used AtomicInteger for thread safety.
  public Agent(String strategy, int maxGenerosity) {
    this.strategy = strategy;
    this.score = new AtomicInteger(0);
    this.maxGenerosity = maxGenerosity;
    if (strategy.equals("GTFT")) {
      // Initialize GTFT agents with a mid-range generosity level.
      this.generosityLevel = new AtomicInteger((maxGenerosity + 1) / 2);
    } else {
      this.generosityLevel = new AtomicInteger(-1); // Not applicable for AC and AD.
    }
    // Sample initial GRV.
    int initialGRV = sampleGRV();
    this.maxGRV = new AtomicInteger(initialGRV);
    // Initialize reset timer to TAU * (current maxGRV)
    this.resetTimer = new AtomicInteger(TAU * initialGRV);
  }
  // ------------------------------------------------------------------------

  // -------------------------------- Update --------------------------------
  // New method to sample a geometric random variable (GRV):
  // number of coin flips until heads.
  private int sampleGRV() {
    int count = 1;
    while (rand.nextDouble() > 0.5) {
      count++;
    }
    return count;
  }
  // ------------------------------------------------------------------------

  public String getStrategy() {
    return strategy;
  }

  public int getScore() {
    return score.get();
  }

  public int getGenerosityLevel() {
    return generosityLevel.get();
  }

  public int getMaxGRV() {
    return maxGRV.get();
  }

  // -------------------------------- Update --------------------------------
  // Expanded cooperation logic:
  // Determines whether the agent cooperates:
  // - AC always cooperates.
  // - AD always defects.
  // - GTFT cooperates probabilistically based on generosity level.
  public boolean cooperate() {
    if (strategy.equals("AC")) {
      return true;
    } else if (strategy.equals("AD")) {
      return false;
    } else if (strategy.equals("GTFT")) {
      double p = (double) generosityLevel.get() / maxGenerosity;
      return rand.nextDouble() < p;
    }
    return false;
  }
  // ------------------------------------------------------------------------

  // -------------------------------- Update --------------------------------
  // New method to decrement timer and reset GRV if needed.
  // If the timer expires, reset the GRV.
  public void checkAndReset() {
    int newTimer = resetTimer.decrementAndGet();
    if (newTimer <= 0) {
      int newSample = sampleGRV();
      // Update maxGRV: take the maximum of current maxGRV and newSample.
      maxGRV.updateAndGet(x -> Math.max(x, newSample));
      // Reset the timer based on the new maxGRV value.
      resetTimer.set(TAU * maxGRV.get());
    }
  }
  // ------------------------------------------------------------------------

  // -------------------------------- Update --------------------------------
  // Expanded interaction logic between agents:
  // 1. Play the donation game using parameters b and c.
  // 2. Update scores and apply simplified k-IGT dynamics for GTFT agents.
  // 3. Exchange maxGRV values for dynamic size counting.
  // 4. Check and possibly reset the GRV based on a countdown timer.
  public void interact(Agent other, int b, int c) {
    // --- Game Dynamics ---
    boolean myCoop = this.cooperate();
    boolean otherCoop = other.cooperate();
    int payoff1, payoff2;
    if (myCoop && otherCoop) {
      payoff1 = b - c;
      payoff2 = b - c;
    } else if (myCoop && !otherCoop) {
      payoff1 = -c;
      payoff2 = b;
    } else if (!myCoop && otherCoop) {
      payoff1 = b;
      payoff2 = -c;
    } else {
      payoff1 = 0;
      payoff2 = 0;
    }
    this.score.addAndGet(payoff1);
    other.score.addAndGet(payoff2);

    // Simplified k-IGT dynamics for GTFT agents:
    if (this.strategy.equals("GTFT")) {
      if (other.getStrategy().equals("AC") || other.getStrategy().equals("GTFT")) {
        generosityLevel.updateAndGet(x -> (x < maxGenerosity ? x + 1 : x));
      } else if (other.getStrategy().equals("AD")) {
        generosityLevel.updateAndGet(x -> (x > 1 ? x - 1 : x));
      }
    }
    if (other.getStrategy().equals("GTFT")) {
      if (this.getStrategy().equals("AC") || this.getStrategy().equals("GTFT")) {
        other.generosityLevel.updateAndGet(x -> (x < maxGenerosity ? x + 1 : x));
      } else if (this.getStrategy().equals("AD")) {
        other.generosityLevel.updateAndGet(x -> (x > 1 ? x - 1 : x));
      }
    }

    // --- Dynamic Size Counting ---
    // Both agents adopt the maximum GRV seen.
    int newGRV = Math.max(this.getMaxGRV(), other.getMaxGRV());
    this.maxGRV.updateAndGet(x -> Math.max(x, newGRV));
    other.maxGRV.updateAndGet(x -> Math.max(x, newGRV));

    // Check and reset the GRV if needed (simulate phase clock behavior).
    this.checkAndReset();
    other.checkAndReset();
  }
  // ------------------------------------------------------------------------
}
