import re
import matplotlib.pyplot as plt


def parse_line(line):
    """
    Parse a line of the form "1 Threads | 74 ms" and return (threads, time).
    """
    match = re.match(r"(\d+)\s+Threads\s*\|\s*(\d+)\s+ms", line)
    if match:
        threads = int(match.group(1))
        time = int(match.group(2))
        return threads, time
    return None


def read_files(file_list):
    """
    Read multiple files and aggregate times by thread count.
    Returns a dictionary where keys are thread counts and values are lists of times.
    """
    data = {}  # {thread_count: [time1, time2, ...]}
    for file_name in file_list:
        with open(file_name, 'r') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                result = parse_line(line)
                if result:
                    threads, time = result
                    if threads not in data:
                        data[threads] = []
                    data[threads].append(time)
    return data


def calculate_avg(data):
    """
    Given a dictionary of times for each thread count, return a dictionary of averages.
    """
    avg_data = {}
    for threads, times in data.items():
        avg_data[threads] = sum(times) / len(times)
    return avg_data


def plot_data(avg_data):
    """
    Plot a graph of average time (y-axis) for each number of threads (x-axis).
    The x-axis is equally spaced and shows only the actual thread counts.
    """
    # Sort by thread count
    sorted_threads = sorted(avg_data.keys())
    avg_times = [avg_data[t] for t in sorted_threads]

    # Create equally spaced positions for each thread count
    x_positions = range(len(sorted_threads))

    plt.figure(figsize=(8, 5))
    plt.plot(x_positions, avg_times, marker='o', linestyle='-', color='b')
    plt.xticks(x_positions, sorted_threads)  # show actual thread counts on x-axis
    plt.xlabel('Number of Threads')
    plt.ylabel('Average Time (ms)')
    plt.title('Average Time per Number of Threads')
    plt.grid(True)
    plt.tight_layout()
    plt.show()


def main():
    # Files are named res1.txt, res2.txt, ..., res5.txt
    file_list = [f"res{i}.txt" for i in range(1, 6)]

    data = read_files(file_list)
    avg_data = calculate_avg(data)

    # Optionally, print the average times for each thread count.
    print("Average run time for each thread count:")
    for threads, avg in sorted(avg_data.items()):
        print(f"{threads} Threads: {avg:.2f} ms")

    plot_data(avg_data)


if __name__ == "__main__":
    main()
