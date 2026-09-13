package com.pratham.backend.engine.algorithm;

import com.pratham.backend.engine.audio.AudioConverter;
import com.pratham.backend.engine.model.Couple;
import com.pratham.backend.engine.model.Peak;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FingerprintGenerator {

    private static final int TARGET_ZONE_SIZE = 5;
    private static final int MAX_FREQ_BITS = 9;
    private static final int MAX_DELTA_BITS = 14;


    /**Takes already generated peaks and convert them into hashes*/
    public static Map<Integer, Couple> generateFingerprint(
            List<Peak> peaks,
            long songId
    ){
        Map<Integer, Couple> fingerprints = new HashMap<>();

        for(int i=0; i<peaks.size(); i++){

            Peak anchor = peaks.get(i);
            for(int j=i+1;
                j < peaks.size() && j<=i+TARGET_ZONE_SIZE;
                j++){

                Peak target = peaks.get(j);

                int address = createAddress(anchor, target);
                long anchorTimeMs = (long)(anchor.getTime() * 1000);

                fingerprints.put(address, new Couple(songId, anchorTimeMs));
            }
        }

        return fingerprints;
    }


    /**Generate fingerprint for a complete audio from scratch*/
    public static Map<Integer, Couple> generateFingerprintForAudio(
            long songId,
            String songFilePath
    ) throws Exception{

        List<Peak> peaks;
        Map<Integer, Couple> fingerprints = new HashMap<>();

        File audioFile = new File(songFilePath);

        try(AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(audioFile)){

            AudioFormat originalFormat = audioStream.getFormat();
            int channels = originalFormat.getChannels();
            int sampleRate = (int) originalFormat.getSampleRate();


            if(channels!=1 && channels!=2)
                throw new IllegalArgumentException(
                        "Only mono and stereo audio supported"
                );

            //Convert audio to exact PCM format expected by AudioConverter
            AudioFormat pcmFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    originalFormat.getSampleRate(),
                    16,
                    channels,
                    channels*2,
                    originalFormat.getSampleRate(),
                    false
            );

            AudioInputStream pcmStream;

            if(AudioSystem.isConversionSupported(
                    pcmFormat,
                    originalFormat
            ))
                pcmStream =
                        AudioSystem.getAudioInputStream(
                                pcmFormat,
                                audioStream
                        );
            else
              throw new IllegalArgumentException(
                      "Audio format cannot be converted to 16-bit PCM"
              );

            try(pcmStream){
                long frameLength = pcmStream.getFrameLength();
                double duration = (double)frameLength / sampleRate;
                byte[] audioBytes = pcmStream.readAllBytes();

                double[] monoSamples =
                        AudioConverter.pcmToMonoSamples(
                                audioBytes,
                                channels
                        );

                List<double[]> spectrogram =
                        SpectrogramGenerator.spectrogram(
                                monoSamples,
                                sampleRate
                );
                peaks = PeakExtractor.extractPeaks(
                        spectrogram,
                        duration,
                        sampleRate
                );
            }
        }
        return generateFingerprint(peaks, songId);
    }


    private static int createAddress(Peak anchor, Peak target){

        int anchorFreqBin = (int)(anchor.getFrequency() / 10);
        int targetFreqBin = (int)(target.getFrequency() / 10);

        int deltaMsRaw = (int)((target.getTime() - anchor.getTime()) * 1000);

        //Create Masks
        int anchorFreqBits = anchorFreqBin & ((1 << (MAX_FREQ_BITS))  - 1);
        int targetFreqBits = targetFreqBin & ((1 << (MAX_FREQ_BITS))  - 1);
        int deltaTimeBits  = deltaMsRaw    & ((1 << (MAX_DELTA_BITS)) - 1);

        // Combine into 32-bit address
        return (anchorFreqBits << 23) | (targetFreqBits << 14) | deltaTimeBits;
    }


}