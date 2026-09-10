package com.pratham;

import com.pratham.audioutils.AudioExporter;
import com.pratham.audioutils.MicrophoneRecorder;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws Exception {

        MicrophoneRecorder recorder = new MicrophoneRecorder();
        short[] audioData = recorder.record(5);
//        System.out.println("Samples: " + audioData.length);
//        for(int i=0; i<10; i++){
//            System.out.println(audioData[i]);
//        }

        AudioExporter.save(audioData, "src/main/assets/audio.csv");
        // Automatically run Python
        Process process = new ProcessBuilder(
                ".venv/bin/python",
                "src/main/assets/python/plot_audio.py"
        ).inheritIO().start();
        process.waitFor();
        System.out.println("Created audio visual.");
    }
}
