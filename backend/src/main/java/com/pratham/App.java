package com.pratham;

import com.pratham.audioutils.AudioExporter;
import com.pratham.audioutils.MicrophoneRecorder;
import com.pratham.shazam.Peak;
import com.pratham.shazam.PeakExtractor;
import com.pratham.shazam.Spectrogram;

import java.util.List;

public class App {

    private static final int SAMPLE_RATE = 44100;
    private static final int RECORDING_DURATION_SECONDS = 10;

    public static void main(String[] args) throws Exception {

        // 1. Record audio
        MicrophoneRecorder recorder = new MicrophoneRecorder();
        short[] audioData = recorder.record(RECORDING_DURATION_SECONDS);
        System.out.println( "Recorded " + audioData.length + " samples." );

        // 2. Save raw audio
        AudioExporter.save( audioData, "src/main/assets/audio.csv" );
        System.out.println( "Saved audio data." );


        // 3. Convert short[] -> double[]
        double[] samples = new double[audioData.length];
        for (int i = 0; i < audioData.length; i++)
            samples[i] = audioData[i];

        // 4. Generate spectrogram
        List<double[]> spectrogram =
                Spectrogram.spectrogram(
                        samples,
                        SAMPLE_RATE
                );
        System.out.println( "Generated spectrogram: " + spectrogram.size() + " frames." );


        // 5. Save spectrogram
        Plotter.saveSpectrogram( spectrogram, "src/main/assets/spectrogram.csv" );


        // 6. Extract peaks
        double audioDuration = (double) audioData.length / SAMPLE_RATE;
        List<Peak> peaks = PeakExtractor.extractPeaks(
                        spectrogram,
                        audioDuration,
                        SAMPLE_RATE
                );
        System.out.println( "Extracted " + peaks.size() + " peaks." );
        // 7. Save peaks
        Plotter.savePeaks( peaks, "src/main/assets/peaks.csv" );


        // 8. Generate all visualizations
        Plotter.plotAll();
        System.out.println( "All visualizations created." );
    }
}