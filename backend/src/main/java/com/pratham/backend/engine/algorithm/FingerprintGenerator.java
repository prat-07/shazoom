package com.pratham.backend.engine.algorithm;

import com.pratham.backend.engine.audio.AudioConverter;
import com.pratham.backend.engine.model.Couple;
import com.pratham.backend.engine.model.Peak;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintGenerator {

    private static final int TARGET_ZONE_SIZE = 5;
    private static final int MAX_FREQ_BITS = 9;
    private static final int MAX_DELTA_BITS = 14;

    /**
     * Takes generated peaks and converts them into hashes.
     */
    public static Map<Integer, Couple> generateFingerprint(
            List<Peak> peaks,
            long songId
    ) {

        Map<Integer, Couple> fingerprints =
                new HashMap<>();

        for (int i = 0; i < peaks.size(); i++) {

            Peak anchor =
                    peaks.get(i);

            for (
                    int j = i + 1;
                    j < peaks.size()
                            && j <= i + TARGET_ZONE_SIZE;
                    j++
            ) {

                Peak target =
                        peaks.get(j);

                int address =
                        createAddress(anchor, target);

                long anchorTimeMs =
                        (long) (anchor.getTime() * 1000);

                fingerprints.put(
                        address,
                        new Couple(
                                songId,
                                anchorTimeMs
                        )
                );
            }
        }

        return fingerprints;
    }

    /**
     * Generates a fingerprint from an arbitrary audio file.
     *
     * Every input is first normalized to:
     *
     *      Mono
     *      44,100 Hz
     *      16-bit signed PCM
     *
     * This guarantees that both ingestion and recognition
     * use exactly the same sample-rate basis.
     */
    public static Map<Integer, Couple> generateFingerprintForAudio(
            long songId,
            InputStream audioInputStream
    ) throws Exception {

        /*
         * BUG 1 FIX:
         *
         * Do NOT use Java Sound to process the original
         * sample rate.
         *
         * ffmpeg converts every input to:
         *
         *      44100 Hz
         *      mono
         *      16-bit PCM
         */
        byte[] pcm =
                AudioConverter.decodeToMono44k(
                        audioInputStream
                );

        double[] monoSamples =
                AudioConverter.normalize(
                        AudioConverter.convertToSamples(pcm)
                );

        if (monoSamples.length == 0) {
            return new HashMap<>();
        }

        /*
         * Duration is calculated from the actual number
         * of decoded samples.
         *
         * This is more reliable than AudioInputStream
         * getFrameLength(), especially for compressed
         * formats and streamed input.
         */
        double duration =
                monoSamples.length
                        / (double) AudioConverter.SAMPLE_RATE;

        /*
         * From this point onwards the DSP pipeline receives
         * exactly the same sample rate for every file.
         */
        List<double[]> spectrogram =
                SpectrogramGenerator.spectrogram(
                        monoSamples,
                        AudioConverter.SAMPLE_RATE
                );

        List<Peak> peaks =
                PeakExtractor.extractPeaks(
                        spectrogram,
                        duration,
                        AudioConverter.SAMPLE_RATE
                );

        return generateFingerprint(
                peaks,
                songId
        );
    }

    /**
     * Creates the 32-bit fingerprint address.
     *
     * Layout:
     *
     * 9 bits  -> anchor frequency
     * 9 bits  -> target frequency
     * 14 bits -> time delta
     */
    private static int createAddress(
            Peak anchor,
            Peak target
    ) {

        int anchorFreqBin =
                (int) (
                        anchor.getFrequency() / 10
                );

        int targetFreqBin =
                (int) (
                        target.getFrequency() / 10
                );

        int deltaMsRaw =
                (int) (
                        (target.getTime()
                                - anchor.getTime()) * 1000
                );

        int anchorFreqBits =
                anchorFreqBin
                        & ((1 << MAX_FREQ_BITS) - 1);

        int targetFreqBits =
                targetFreqBin
                        & ((1 << MAX_FREQ_BITS) - 1);

        int deltaTimeBits =
                deltaMsRaw
                        & ((1 << MAX_DELTA_BITS) - 1);

        return (anchorFreqBits << 23)
                | (targetFreqBits << 14)
                | deltaTimeBits;
    }
}