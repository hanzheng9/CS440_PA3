import argparse as ap
import matplotlib.pyplot as plt
import numpy as np
import os
import re                           # regular expressions
from typing import List, Tuple
from sklearn.linear_model import LinearRegression


LINE_PREAMBLE = "after cycle="
LINE_UTILITY_STR = "avg(utility)="
LINE_WINS_STR = "avg(num_wins)="

def load(path: str) -> np.ndarray:
    data: List[Tuple[int, float]] = list()
    line_counter = 0  # Counter for line numbers

    try:
        with open(path, "r") as f:
            for line in f:
                if LINE_PREAMBLE in line.strip().rstrip() and LINE_UTILITY_STR in line.strip().rstrip()\
                   and LINE_WINS_STR in line.strip().rstrip():
                    values_str = line.strip().rstrip().replace(LINE_PREAMBLE, "").replace(LINE_UTILITY_STR, "")\
                        .replace(LINE_WINS_STR, "")
                    phase_idx, avg_utility, avg_wins = re.sub(r'\s+', ' ', values_str.strip().rstrip()).strip()\
                        .rstrip().split(" ")
                    phase_idx_int = int(phase_idx)
                    data.append([line_counter + 1, float(avg_utility), float(avg_wins), phase_idx_int])
                    line_counter += 1  # Increment counter after processing valid line
    except:
        pass

    return np.array(data)


def standardize_data(values: np.ndarray) -> np.ndarray:
    """Standardize data to have mean=0 and std=1"""
    mean_val = np.mean(values)
    std_val = np.std(values)
    
    # Handle case where std is 0 (all values are the same)
    if std_val == 0:
        return np.zeros_like(values)
    else:
        return (values - mean_val) / std_val


def main() -> None:
    parser = ap.ArgumentParser()
    parser.add_argument("logfile", type=str, help="path to logfile containing eval outputs")
    args = parser.parse_args()

    if not os.path.exists(args.logfile):
        raise Exception("ERROR: logfile [%s] does not exist!" % args.logfile)

    data: np.ndarray = load(args.logfile)
    
    # Extract utility and wins values
    utility_values = data[:, 1]
    wins_values = data[:, 2]
    phase_indices = data[:, 3]
    
    # Vertical line at x = 1633 if it's within data range
    switch_line = 1633
    
    # Standardize both utility and wins
    standardized_utility = standardize_data(utility_values)
    standardized_wins = standardize_data(wins_values)
    
    # Calculate running averages for both
    running_avg_utility = np.cumsum(standardized_utility) / np.arange(1, len(standardized_utility) + 1)
    running_avg_wins = np.cumsum(standardized_wins) / np.arange(1, len(standardized_wins) + 1)
    
    # Find the largest values in standardized data
    max_utility_idx = np.argmax(standardized_utility)
    max_wins_idx = np.argmax(standardized_wins)
    
    max_utility_cycle = data[max_utility_idx, 0]
    max_wins_cycle = data[max_wins_idx, 0]
    
    max_utility_std = standardized_utility[max_utility_idx]
    max_wins_std = standardized_wins[max_wins_idx]
    
    max_utility_original = utility_values[max_utility_idx]
    max_wins_original = wins_values[max_wins_idx]
    
    # Calculate average of last 200 points (or fewer if data is shorter)
    last_n = min(50, len(data))
    avg_last_utility = np.mean(utility_values[-last_n:])
    avg_last_wins = np.mean(wins_values[-last_n:])
    avg_last_utility_std = np.mean(standardized_utility[-last_n:])
    avg_last_wins_std = np.mean(standardized_wins[-last_n:])
    
    # Print statistics
    print(f"Statistics for original utility values:")
    print(f"  Mean: {np.mean(utility_values):.4f}, Std: {np.std(utility_values):.4f}")
    print(f"  Min: {np.min(utility_values):.4f}, Max: {np.max(utility_values):.4f}")
    
    print(f"\nStatistics for original wins values:")
    print(f"  Mean: {np.mean(wins_values):.4f}, Std: {np.std(wins_values):.4f}")
    print(f"  Min: {np.min(wins_values):.4f}, Max: {np.max(wins_values):.4f}")
    
    print(f"\nLargest standardized utility value: {max_utility_std:.4f} at line {max_utility_cycle}")
    print(f"Largest standardized wins value: {max_wins_std:.4f} at line {max_wins_cycle}")
    print(f"\nCorresponding original values:")
    print(f"  Utility: {max_utility_original:.4f}")
    print(f"  Wins: {max_wins_original:.4f}")
    print(f"\nTotal valid data points: {len(data)}")
    print(f"Average of last {last_n} points:")
    print(f"  Utility (original): {avg_last_utility:.4f}")
    print(f"  Wins (original): {avg_last_wins:.4f}")
    print(f"  Utility (standardized): {avg_last_utility_std:.4f}")
    print(f"  Wins (standardized): {avg_last_wins_std:.4f}")
    
    # Create a figure with 4 subplots
    plt.figure(figsize=(15, 10))
    
    # Plot 1: Standardized Utility
    plt.subplot(2, 2, 1)
    plt.plot(data[:, 0], standardized_utility, label='Standardized Utility', color='blue')
    plt.plot(data[:, 0], running_avg_utility, label='Running Avg', linestyle='--', linewidth=2, color='orange')
    
    # Add line from first to average of last 200
    plt.plot([data[0, 0], data[-1, 0]], [standardized_utility[0], avg_last_utility_std], 
             color='black', linestyle=':', linewidth=2, label='First-Last200Avg Trend')
    plt.axhline(y=0, color='black', linestyle='-', linewidth=1, alpha=0.5, label='Zero Line')

    
    # Add vertical line at x = 1633 IF it's within range
    if switch_line >= data[0, 0] and switch_line <= data[-1, 0]:
        print(f"\nVertical line at x = {switch_line}")
        plt.axvline(x=switch_line, color='red', linestyle='--', linewidth=2, 
                   label=f'Agent Switch (x={switch_line})')
    
    plt.scatter([max_utility_cycle], [max_utility_std], color='red', s=100, zorder=5, 
                label=f'Max: {max_utility_std:.2f}')
    plt.axhline(y=max_utility_std, color='red', linestyle=':', alpha=0.5, linewidth=1)
    plt.xlabel('Line Number')
    plt.ylabel('Standardized Utility (z-score)')
    plt.title('Standardized Utility over Time')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    # Plot 2: Standardized Wins
    plt.subplot(2, 2, 2)
    plt.plot(data[:, 0], standardized_wins, label='Standardized Wins', color='green')
    plt.plot(data[:, 0], running_avg_wins, label='Running Avg', linestyle='--', linewidth=2, color='purple')
    
    # Add line from first to average of last 200
    plt.plot([data[0, 0], data[-1, 0]], [standardized_wins[0], avg_last_wins_std], 
             color='black', linestyle=':', linewidth=2, label='First-Last200Avg Trend')
    
    # Add vertical line at x = 1633 IF it's within range
    if switch_line >= data[0, 0] and switch_line <= data[-1, 0]:
        plt.axvline(x=switch_line, color='red', linestyle='--', linewidth=2, 
                   label=f'Agent Switch (x={switch_line})')
    
    plt.axhline(y=0, color='red', linestyle='-', linewidth=1, alpha=0.5, label='Zero Line')
    
    plt.scatter([max_wins_cycle], [max_wins_std], color='red', s=100, zorder=5, 
                label=f'Max: {max_wins_std:.2f}')
    plt.axhline(y=max_wins_std, color='red', linestyle=':', alpha=0.5, linewidth=1)
    plt.xlabel('Line Number')
    plt.ylabel('Standardized Wins (z-score)')
    plt.title('Standardized Wins over Time')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    # Plot 3: Original Utility
    plt.subplot(2, 2, 3)
    running_avg_utility_original = np.cumsum(utility_values) / np.arange(1, len(utility_values) + 1)
    plt.plot(data[:, 0], utility_values, label='Original Utility', color='blue', alpha=0.7)
    plt.plot(data[:, 0], running_avg_utility_original, label='Running Avg', linestyle='--', linewidth=2, color='orange')
    
    # Add line from first to average of last 200
    plt.plot([data[0, 0], data[-1, 0]], [utility_values[0], avg_last_utility], 
             color='black', linestyle=':', linewidth=2, label='First-Last200Avg Trend')
    
    # Add vertical line at x = 1633 IF it's within range
    if switch_line >= data[0, 0] and switch_line <= data[-1, 0]:
        plt.axvline(x=switch_line, color='red', linestyle='--', linewidth=2, 
                   label=f'Agent Switch (x={switch_line})')
    
    plt.scatter([max_utility_cycle], [max_utility_original], color='red', s=100, zorder=5, 
                label=f'Max: {max_utility_original:.2f}')
    plt.axhline(y=max_utility_original, color='red', linestyle=':', alpha=0.5, linewidth=1)
    plt.xlabel('Line Number')
    plt.ylabel('Original Utility')
    plt.title('Original Utility over Time')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    # Plot 4: Original Wins
    plt.subplot(2, 2, 4)
    running_avg_wins_original = np.cumsum(wins_values) / np.arange(1, len(wins_values) + 1)
    plt.plot(data[:, 0], wins_values, label='Original Wins', color='green', alpha=0.7)
    plt.plot(data[:, 0], running_avg_wins_original, label='Running Avg', linestyle='--', linewidth=2, color='purple')
    
    # Add line from first to average of last 200
    plt.plot([data[0, 0], data[-1, 0]], [wins_values[0], avg_last_wins], 
             color='black', linestyle=':', linewidth=2, label='First-Last200Avg Trend')
    
    # Add vertical line at x = 1633 IF it's within range
    if switch_line >= data[0, 0] and switch_line <= data[-1, 0]:
        plt.axvline(x=switch_line, color='red', linestyle='--', linewidth=2, 
                   label=f'Agent Switch (x={switch_line})')
    
    plt.scatter([max_wins_cycle], [max_wins_original], color='red', s=100, zorder=5, 
                label=f'Max: {max_wins_original:.2f}')
    plt.axhline(y=max_wins_original, color='red', linestyle=':', alpha=0.5, linewidth=1)
    plt.xlabel('Line Number')
    plt.ylabel('Original Wins')
    plt.title('Original Wins over Time')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.show()
    
    # Additional plot: Combined standardized view with first-last200avg line
    plt.figure(figsize=(12, 6))
    plt.plot(data[:, 0], standardized_utility, label='Standardized Utility', color='blue', linewidth=2)
    plt.plot(data[:, 0], standardized_wins, label='Standardized Wins', color='green', linewidth=2)
    
    # Add lines from first to average of last 200 for both metrics
    plt.plot([data[0, 0], data[-1, 0]], [standardized_utility[0], avg_last_utility_std], 
             color='purple', linestyle=':', linewidth=2, label='Utility Trend (First-Last200Avg)')
    plt.plot([data[0, 0], data[-1, 0]], [standardized_wins[0], avg_last_wins_std], 
             color='red', linestyle=':', linewidth=2, label='Wins Trend (First-Last200Avg)')
    
    # Add vertical line at x = 1633 IF it's within range
    if switch_line >= data[0, 0] and switch_line <= data[-1, 0]:
        plt.axvline(x=switch_line, color='black', linestyle='--', linewidth=2, 
                   label=f'Agent Switch (x={switch_line})')
    
    plt.xlabel('Line Number')
    plt.ylabel('Standardized Value (z-score)')
    plt.title('Comparison of Standardized Utility vs Wins with First-Last200Avg Trends')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.show()
    
    # NEW: Linear Regression plot - Utility vs Wins
    plt.figure(figsize=(12, 10))
    
    # Plot 1: Original Utility vs Wins scatter with regression line
    plt.subplot(2, 2, 1)
    X = utility_values.reshape(-1, 1)
    y = wins_values
    model = LinearRegression()
    model.fit(X, y)
    y_pred = model.predict(X)
    
    plt.scatter(utility_values, wins_values, alpha=0.5, s=20, label='Data points')
    plt.plot(utility_values, y_pred, color='red', linewidth=2, 
             label=f'LR: y = {model.coef_[0]:.3f}x + {model.intercept_:.3f}')
    
    plt.xlabel('Utility')
    plt.ylabel('Wins')
    plt.title('Linear Regression: Utility → Wins (Original)')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    # Calculate and print regression stats
    correlation = np.corrcoef(utility_values, wins_values)[0, 1]
    print(f"\nLinear Regression Analysis (Original):")
    print(f"  Correlation coefficient: {correlation:.4f}")
    print(f"  R-squared: {model.score(X, y):.4f}")
    print(f"  Slope: {model.coef_[0]:.4f}")
    print(f"  Intercept: {model.intercept_:.4f}")
    
    # Plot 2: Standardized Utility vs Wins scatter with regression line
    plt.subplot(2, 2, 2)
    X_std = standardized_utility.reshape(-1, 1)
    y_std = standardized_wins
    model_std = LinearRegression()
    model_std.fit(X_std, y_std)
    y_pred_std = model_std.predict(X_std)
    
    plt.scatter(standardized_utility, standardized_wins, alpha=0.5, s=20, label='Data points')
    plt.plot(standardized_utility, y_pred_std, color='red', linewidth=2, 
             label=f'LR: y = {model_std.coef_[0]:.3f}x + {model_std.intercept_:.3f}')
    
    plt.xlabel('Standardized Utility (z-score)')
    plt.ylabel('Standardized Wins (z-score)')
    plt.title('Linear Regression: Utility → Wins (Standardized)')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    correlation_std = np.corrcoef(standardized_utility, standardized_wins)[0, 1]
    print(f"\nLinear Regression Analysis (Standardized):")
    print(f"  Correlation coefficient: {correlation_std:.4f}")
    print(f"  R-squared: {model_std.score(X_std, y_std):.4f}")
    print(f"  Slope: {model_std.coef_[0]:.4f}")
    print(f"  Intercept: {model_std.intercept_:.4f}")
    
    # Plot 3: Residuals plot (Original)
    plt.subplot(2, 2, 3)
    residuals = wins_values - y_pred
    plt.scatter(utility_values, residuals, alpha=0.5, s=20)
    plt.axhline(y=0, color='red', linestyle='--', linewidth=1)
    plt.xlabel('Utility')
    plt.ylabel('Residuals (Wins - Predicted)')
    plt.title('Residuals Plot (Original)')
    plt.grid(True, alpha=0.3)
    
    # Plot 4: Time series of both metrics with correlation window
    plt.subplot(2, 2, 4)
    window_size = 100
    rolling_correlation = []
    
    for i in range(len(data) - window_size + 1):
        util_window = utility_values[i:i+window_size]
        wins_window = wins_values[i:i+window_size]
        corr = np.corrcoef(util_window, wins_window)[0, 1]
        rolling_correlation.append(corr)
    
    plt.plot(data[window_size-1:, 0], rolling_correlation, color='purple', linewidth=2)
    plt.axhline(y=0, color='gray', linestyle='--', linewidth=1, alpha=0.5)
    plt.xlabel('Line Number')
    plt.ylabel(f'Rolling Correlation (window={window_size})')
    plt.title(f'Rolling Correlation between Utility and Wins')
    plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.show()
    
    # Print trend information
    print(f"\nTrend Analysis (First to Average of Last {last_n}):")
    print(f"Utility Trend (Standardized): {standardized_utility[0]:.4f} → {avg_last_utility_std:.4f}")
    print(f"  Change: {avg_last_utility_std - standardized_utility[0]:.4f}")
    print(f"Wins Trend (Standardized): {standardized_wins[0]:.4f} → {avg_last_wins_std:.4f}")
    print(f"  Change: {avg_last_wins_std - standardized_wins[0]:.4f}")
    print(f"\nOriginal Values Trend (First to Average of Last {last_n}):")
    print(f"Utility Trend (Original): {utility_values[0]:.4f} → {avg_last_utility:.4f}")
    print(f"  Change: {avg_last_utility - utility_values[0]:.4f}")
    print(f"Wins Trend (Original): {wins_values[0]:.4f} → {avg_last_wins:.4f}")
    print(f"  Change: {avg_last_wins - wins_values[0]:.4f}")


if __name__ == "__main__":
    main()