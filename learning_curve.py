import argparse as ap
import matplotlib.pyplot as plt
import numpy as np
import os
import re                           # regular expressions


LINE_PREAMBLE = "after cycle="
LINE_UTILITY_STR = "avg(utility)="
LINE_WINS_STR = "avg(num_wins)="

def load(path: str) -> np.ndarray:
    data: List[Tuple[int, float]] = list()

    try:
        with open(path, "r") as f:
            for line in f:
                if LINE_PREAMBLE in line.strip().rstrip() and LINE_UTILITY_STR in line.strip().rstrip()\
                   and LINE_WINS_STR in line.strip().rstrip():
                    values_str = line.strip().rstrip().replace(LINE_PREAMBLE, "").replace(LINE_UTILITY_STR, "")\
                        .replace(LINE_WINS_STR, "")
                    phase_idx, avg_utility, avg_wins = re.sub(r'\s+', ' ', values_str.strip().rstrip()).strip()\
                        .rstrip().split(" ")
                    data.append([float(phase_idx), float(avg_utility), float(avg_wins)])
    except:
        pass

    return np.array(data)


def main() -> None:
    parser = ap.ArgumentParser()
    parser.add_argument("logfile", type=str, help="path to logfile containing eval outputs")
    args = parser.parse_args()

    if not os.path.exists(args.logfile):
        raise Exception("ERROR: logfile [%s] does not exist!" % args.logfile)

    data: np.ndarray = load(args.logfile)
    
    # Calculate running average
    running_avg = np.cumsum(data[:, 1]) / np.arange(1, len(data[:, 1]) + 1)
    
    # Find the largest value
    max_idx = np.argmax(data[:, 1])
    max_cycle = data[max_idx, 0]
    max_value = data[max_idx, 1]
    
    print(f"Largest utility value: {max_value:.4f} at cycle {max_cycle}")
    
    # Plot both original data and running average
    plt.plot(data[:, 0], data[:, 1], label='Raw Utility')
    plt.plot(data[:, 0], running_avg, label='Running Average', linestyle='--', linewidth=2)
    
    # Mark the largest point
    plt.scatter([max_cycle], [max_value], color='red', s=100, zorder=5, 
                label=f'Max: {max_value:.2f} at {max_cycle}')
    
    # Add a horizontal line at the maximum value
    plt.axhline(y=max_value, color='red', linestyle=':', alpha=0.5, linewidth=1)
    
    plt.xlabel('Cycle')
    plt.ylabel('Utility')
    plt.title('Utility over Time with Running Average')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.show()


if __name__ == "__main__":
    main()