import numpy as np
import matplotlib.pyplot as plt

SAMPLE_RATE = 44100

audio = np.loadtxt("src/main/assets/audio.csv")

time = np.arange(len(audio)) / SAMPLE_RATE

plt.figure(figsize=(12, 5))
plt.plot(time, audio)

plt.xlabel("Time (seconds)")
plt.ylabel("Amplitude")
plt.title("Microphone Audio Waveform")
plt.grid()

plt.tight_layout()

plt.savefig(
    "src/main/assets/visuals/waveform.png",
    dpi=150
)

print("Plot generated")