package com.pratham.audioutils;

public class AudioConverter {
    /**
     * Converts 16-bit signed little-endian PCM bytes
     * into short samples.
     *
     * Every sample consists of 2 bytes.
     */
    public static short[] convertToSamples(byte[] bytes) {

        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "PCM data must contain an even number of bytes"
            );
        }

        short[] samples =
                new short[bytes.length / 2];

        for (int i = 0; i < samples.length; i++) {

            int low =
                    bytes[2 * i] & 0xFF;

            int high =
                    bytes[2 * i + 1];

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
     * Converts interleaved PCM samples into a single
     * mono channel.
     *
     * Mono:
     *
     *     M = L
     *
     * Stereo:
     *
     *     M = (L + R) / 2
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
