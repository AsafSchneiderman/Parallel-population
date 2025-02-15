import os
import matplotlib.pyplot as plt
import re

def extract_convergence_iteration(file_path):
    """Extracts the first iteration where Defector reaches 100%."""
    with open(file_path, "r") as file:
        for i, line in enumerate(file):
            match = re.search(r"Defector: (\d+\.\d+)%", line)
            if match and float(match.group(1)) == 100.0:
                return i  # Return iteration number
    return None  # Return None if convergence is not reached

def plot_convergence(directory):
    thread_counts = []
    convergence_iterations = []

    # Look for all result files
    for filename in sorted(os.listdir(directory)):
        if filename.startswith("results_threads_") and filename.endswith(".txt"):
            thread_count = int(re.search(r"results_threads_(\d+)\.txt", filename).group(1))
            iteration = extract_convergence_iteration(os.path.join(directory, filename))

            if iteration is not None:
                thread_counts.append(thread_count)
                convergence_iterations.append(iteration)

    # Sort based on thread counts (to ensure correct order in plot)
    sorted_data = sorted(zip(thread_counts, convergence_iterations))
    thread_counts, convergence_iterations = zip(*sorted_data)

    # Plot results
    plt.figure(figsize=(10, 6))
    plt.plot(thread_counts, convergence_iterations, marker='o', linestyle='-', markersize=8, linewidth=2, label="Convergence Iteration")
    plt.xlabel("Number of Threads")
    plt.ylabel("Iteration Number (Convergence)")
    plt.title("Convergence Speed vs Number of Threads")
    plt.grid(True, linestyle="--", alpha=0.7)
    plt.xscale("log")  # Use logarithmic scale if thread counts increase exponentially
    plt.xticks(thread_counts, thread_counts)
    plt.legend()
    plt.show()

# Example usage:
plot_convergence(".")  # Use current directory or change accordingly
