package com.pratham;

import com.pratham.shazam.Peak;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Plotter {

    private static final String PYTHON = ".venv/bin/python";
    private static final String PYTHON_DIRECTORY = "src/main/assets/python";
    private static final String VISUAL_DIRECTORY = "src/main/assets/visuals";


    // Save spectrogram
    public static void saveSpectrogram(
            List<double[]> spectrogram,
            String filePath
    ) throws IOException {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(filePath)
                     )) {

            for (double[] frame : spectrogram) {
                for (int i = 0; i < frame.length; i++) {

                    if (i > 0)
                        writer.write(",");
                    writer.write( Double.toString(frame[i]) );

                }
                writer.newLine();
            }
        }

        System.out.println( "Saved spectrogram CSV." );
    }


    // Save peaks
    public static void savePeaks(
            List<Peak> peaks,
            String filePath
    ) throws IOException {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(filePath)
                     )) {

            // CSV header
            writer.write("frequency,time");
            writer.newLine();

            for (Peak peak : peaks) {

                writer.write(
                        Double.toString(
                                peak.getFrequency()
                        )
                );

                writer.write(",");

                writer.write(
                        Double.toString(
                                peak.getTime()
                        )
                );

                writer.newLine();
            }
        }

        System.out.println( "Saved peaks CSV." );
    }


    // Run all Python plotting scripts
    public static void plotAll()
            throws IOException, InterruptedException {

        System.out.println( "\nGenerating visualizations..." );

        // 1. Raw audio waveform
        runPythonScript( PYTHON_DIRECTORY + "/plot_audio.py" );
        // 2. Spectrogram
        runPythonScript( PYTHON_DIRECTORY + "/plot_spectrogram.py" );
        // 3. Extracted peaks
        runPythonScript( PYTHON_DIRECTORY + "/plot_peaks.py" );

        System.out.println( "Visualization pipeline complete." );
    }


    // Run one Python script
    private static void runPythonScript(
            String scriptPath
    ) throws IOException, InterruptedException {

        System.out.println( "Running: " + scriptPath );

        Process process =
                new ProcessBuilder(
                        PYTHON,
                        scriptPath
                )
                        .inheritIO()
                        .start();

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new RuntimeException(
                    "Python script failed: "
                            + scriptPath
                            + " (exit code "
                            + exitCode
                            + ")"
            );
        }

        System.out.println( "Completed: " + scriptPath );
    }
}