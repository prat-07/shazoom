package com.pratham;

import com.pratham.audioutils.AudioExporter;
import com.pratham.audioutils.MicrophoneRecorder;
import com.pratham.shazam.Complex;
import com.pratham.shazam.FFT;

public class App {
    public static void main( String[] args ) throws Exception {

        //Record audio
        MicrophoneRecorder recorder = new MicrophoneRecorder();
        short[] audioData = recorder.record(5);

        AudioExporter.save(audioData, "src/main/assets/audio.csv");
        // Automatically run Python file to generate plot
        Process process = new ProcessBuilder(
                ".venv/bin/python",
                "src/main/assets/python/plot_audio.py"
        ).inheritIO().start();
        process.waitFor();
        System.out.println("Created audio visual.");

    }
}
