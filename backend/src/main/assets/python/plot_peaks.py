import numpy as np
import matplotlib.pyplot as plt

# --------------------------------------------------
# Load peaks
# --------------------------------------------------

peaks = np.loadtxt(
    "src/main/assets/peaks.csv",
    delimiter=",",
    skiprows=1
)

# Handle case where there is only one peak
if peaks.ndim == 1:
    peaks = peaks.reshape(1, -1)

frequencies = peaks[:, 0]
times = peaks[:, 1]

# --------------------------------------------------
# Plot peaks
# --------------------------------------------------

plt.figure(figsize=(14, 7))

plt.scatter(
    times,
    frequencies,
    s=12
)

plt.xlabel("Time (seconds)")
plt.ylabel("Frequency (Hz)")
plt.title("Extracted Audio Peaks")

plt.grid(True)

plt.tight_layout()

plt.savefig(
    "src/main/assets/visuals/peaks.png",
    dpi=150
)