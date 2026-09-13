import numpy as np
import matplotlib.pyplot as plt


# --------------------------------------------------
# Configuration
# --------------------------------------------------

ORIGINAL_SAMPLE_RATE = 44100
DSP_FACTOR = 4

FFT_SIZE = 1024
HOP_SIZE = FFT_SIZE // 2

EFFECTIVE_SAMPLE_RATE = (
    ORIGINAL_SAMPLE_RATE / DSP_FACTOR
)

MAX_FREQUENCY = 5000


# --------------------------------------------------
# Load spectrogram
# --------------------------------------------------

spectrogram = np.loadtxt(
    "src/main/assets/spectrogram.csv",
    delimiter=","
)

if spectrogram.ndim == 1:
    spectrogram = spectrogram[np.newaxis, :]


number_of_frames = spectrogram.shape[0]
number_of_bins = spectrogram.shape[1]


# --------------------------------------------------
# Frequency axis
# --------------------------------------------------

frequency_resolution = (
    EFFECTIVE_SAMPLE_RATE / FFT_SIZE
)

frequencies = (
    np.arange(number_of_bins)
    * frequency_resolution
)


# --------------------------------------------------
# Time axis
# --------------------------------------------------

times = (
    np.arange(number_of_frames)
    * HOP_SIZE
    / EFFECTIVE_SAMPLE_RATE
)


# --------------------------------------------------
# Convert magnitude → decibels
# --------------------------------------------------

magnitude_db = 20 * np.log10(
    spectrogram + 1e-10
)


# --------------------------------------------------
# Keep only 0–5 kHz
# --------------------------------------------------

frequency_mask = (
    frequencies <= MAX_FREQUENCY
)

frequencies = frequencies[frequency_mask]

magnitude_db = (
    magnitude_db[:, frequency_mask]
)


# --------------------------------------------------
# Dynamic range
# --------------------------------------------------

max_db = np.max(magnitude_db)

# Show 80 dB below the loudest frequency
MIN_DB = max_db - 80

magnitude_db = np.maximum(
    magnitude_db,
    MIN_DB
)


# --------------------------------------------------
# Plot
# --------------------------------------------------

plt.figure(
    figsize=(16, 8)
)

plt.pcolormesh(
    times,
    frequencies,
    magnitude_db.T,
    shading="auto",

    # Much easier to read than blue/green
    cmap="magma",

    # Explicit dynamic range
    vmin=MIN_DB,
    vmax=max_db
)


# --------------------------------------------------
# Labels
# --------------------------------------------------

plt.xlabel(
    "Time (seconds)",
    fontsize=13
)

plt.ylabel(
    "Frequency (Hz)",
    fontsize=13
)

plt.title(
    "Audio Spectrogram",
    fontsize=17
)


# --------------------------------------------------
# Frequency ticks
# --------------------------------------------------

plt.yticks(
    np.arange(
        0,
        MAX_FREQUENCY + 1,
        500
    )
)


# --------------------------------------------------
# Color bar
# --------------------------------------------------

colorbar = plt.colorbar()

colorbar.set_label(
    "Magnitude (dB)",
    fontsize=12
)


# --------------------------------------------------
# Grid
# --------------------------------------------------

plt.grid(
    alpha=0.15
)


# --------------------------------------------------
# Layout
# --------------------------------------------------

plt.tight_layout()


# --------------------------------------------------
# Save
# --------------------------------------------------

plt.savefig(
    "src/main/assets/visuals/spectrogram.png",
    dpi=200,
    bbox_inches="tight"
)

print(
    "Spectrogram saved to "
    "src/main/assets/visuals/spectrogram.png"
)