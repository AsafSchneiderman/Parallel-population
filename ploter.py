import matplotlib.pyplot as plt
import numpy as np

def plot_results(file_path):
    threads = []
    times = []
    
    with open(file_path, 'r') as file:
        for line in file:
            parts = line.strip().split("|")  # Split without extra spaces
            num_threads = int(parts[0].strip().split()[0])  # Extract integer thread count
            exec_time = int(parts[1].strip().split()[0])    # Extract integer execution time
            
            threads.append(num_threads)
            times.append(exec_time)
    
    # Create evenly spaced x positions
    x_positions = np.arange(len(threads))  # Evenly spaced indices
    
    plt.figure(figsize=(10, 6))
    plt.plot(x_positions, times, marker='o', linestyle='-', markersize=8, linewidth=2, label="Execution Time")
    
    # Set the x-axis labels to thread numbers but evenly space them
    plt.xticks(x_positions, threads)  
    
    plt.xlabel("Number of Threads")
    plt.ylabel("Execution Time (ms)")
    plt.title("Parallel Population Protocol Execution Time")
    plt.grid(True, linestyle="--", alpha=0.7)
    plt.legend()
    plt.show()

# Example usage:
plot_results("results_summary.txt")
