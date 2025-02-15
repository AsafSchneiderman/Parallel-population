import java.util.Map;

class Agent {
  private String strategy; // "Cooperator" or "Defector"
  private int score;

  public Agent(String strategy) {
    this.strategy = strategy;
    this.score = 0;
  }

  public String getStrategy() {
    return strategy;
  }

  public int getScore() {
    return score;
  }

  public void addScore(int points) {
    this.score += points;
  }

  public void interact(Agent other, Map<String, Map<String, int[]>> payoffMatrix) {
    int[] payoffs = payoffMatrix.get(this.strategy).get(other.strategy);
    this.addScore(payoffs[0]);
    other.addScore(payoffs[1]);
  }

  public void updateStrategy(Agent other) {
    if (other.getScore() > this.score) {
      this.strategy = other.getStrategy();
    }
  }
}