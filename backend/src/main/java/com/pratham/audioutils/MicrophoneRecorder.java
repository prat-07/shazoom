package com.pratham.audioutils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class MicrophoneRecorder {

    private static final float SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;  //mono
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private final AudioFormat format;

    public MicrophoneRecorder() {
        format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );
    }

    public short[] record(int durationSeconds) throws LineUnavailableException {

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

        line.open(format);
        line.start();

        // 4096 bytes = 2048 samples for 16-bit mono audio
        byte[] buffer = new byte[4096];

        ByteArrayOutputStream audioBytes = new ByteArrayOutputStream();

        long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

        System.out.println("Recording...");

        try {
            while (System.currentTimeMillis() < endTime) {

                int count = line.read(
                        buffer,
                        0,
                        buffer.length
                );

                if (count > 0) {
                    audioBytes.write(buffer, 0, count);
                }
            }
        } finally {
            line.stop();
            line.close();
        }

        System.out.println("Recording finished.");

        return AudioConverter.convertToSamples(audioBytes.toByteArray());
    }
}
