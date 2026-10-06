package com.pratham.backend.engine.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AudioConverter {

    public static final int SAMPLE_RATE = 44100;

    /**
     * Decodes any audio format supported by ffmpeg into:
     *
     *  - Mono
     *  - 44.1 kHz
     *  - 16-bit signed PCM
     *  - Little-endian
     *
     * ffmpeg must be installed and available in PATH.
     */
    public static byte[] decodeToMono44k(
            InputStream input
    ) throws IOException, InterruptedException {

        Path source = Files.createTempFile(
                "shazoom-in-",
                ".audio"
        );

        try {
            Files.copy(
                    input,
                    source,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Process process = new ProcessBuilder(
                    "ffmpeg",
                    "-v", "error",
                    "-i", source.toString(),
                    "-vn",
                    "-ac", "1",
                    "-ar", String.valueOf(SAMPLE_RATE),
                    "-f", "s16le",
                    "pipe:1"
            )
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            byte[] pcm =
                    process.getInputStream().readAllBytes();

            int exitCode = process.waitFor();

            if (exitCode != 0 || pcm.length == 0) {
                throw new IllegalArgumentException(
                        "ffmpeg could not decode the audio file"
                );
            }

            if (pcm.length % 2 != 0) {
                throw new IllegalArgumentException(
                        "ffmpeg returned invalid 16-bit PCM data"
                );
            }

            return pcm;

        } finally {
            Files.deleteIfExists(source);
        }
    }

    /**
     * Converts 16-bit signed little-endian PCM bytes
     * into short samples.
     */
    public static short[] convertToSamples(byte[] bytes) {

        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "PCM data must contain an even number of bytes"
            );
        }

        short[] samples = new short[bytes.length / 2];

        for (int i = 0; i < samples.length; i++) {

            int low = bytes[2 * i] & 0xFF;
            int high = bytes[2 * i + 1];

            samples[i] =
                    (short) ((high << 8) | low);
        }

        return samples;
    }

    /**
     * Converts 16-bit PCM samples into normalized
     * floating-point samples in approximately [-1, 1].
     */
    public static double[] normalize(short[] samples) {

        double[] normalized =
                new double[samples.length];

        for (int i = 0; i < samples.length; i++) {
            normalized[i] =
                    samples[i] / 32768.0;
        }

        return normalized;
    }

    /**
     * Complete decoding pipeline:
     *
     * Audio file
     *      ↓
     * ffmpeg
     *      ↓
     * 44.1 kHz mono 16-bit PCM
     *      ↓
     * normalized double[]
     */
    public static double[] decodeToMono44kSamples(
            InputStream input
    ) throws IOException, InterruptedException {

        byte[] pcm =
                decodeToMono44k(input);

        return normalize(
                convertToSamples(pcm)
        );
    }

    /**
     * Converts already decoded PCM data into mono.
     *
     * Kept for compatibility with existing code.
     */
    public static double[] pcmToMonoSamples(
            byte[] audioBytes,
            int channels
    ) {

        short[] samples =
                convertToSamples(audioBytes);

        if (channels == 1) {
            return normalize(samples);
        }

        if (channels != 2) {
            throw new IllegalArgumentException(
                    "Only mono and stereo audio are supported"
            );
        }

        int frameCount =
                samples.length / 2;

        double[] mono =
                new double[frameCount];

        for (int i = 0; i < frameCount; i++) {

            short left =
                    samples[2 * i];

            short right =
                    samples[2 * i + 1];

            double leftNormalized =
                    left / 32768.0;

            double rightNormalized =
                    right / 32768.0;

            mono[i] =
                    (leftNormalized + rightNormalized)
                            / 2.0;
        }

        return mono;
    }
}