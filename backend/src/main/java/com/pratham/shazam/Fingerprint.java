package com.pratham.shazam;

import com.pratham.shazam.models.Couple;
import com.pratham.shazam.models.Peak;

import java.util.HashMap;
import java.util.Map;

public class Fingerprint {

    private static final int TARGET_ZONE_SIZE = 5;
    private static final int MAX_FREQ_BITS = 9;
    private static final int MAX_DELTA_BITS = 14;


    /**Takes already generated peaks and convert them into hashes*/
    public static Map<Integer, Couple> generateFingerprint(
            Peak[] peaks,
            long songId
    ){
        Map<Integer, Couple> fingerprints = new HashMap<>();

        for(int i=0; i<peaks.length; i++){

            Peak anchor = peaks[i];
            for(int j=i+1;
                j < peaks.length && j<=i+TARGET_ZONE_SIZE;
                j++){

                Peak target = peaks[j];

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

        //TODO: complete this function after creating WavUtils and FileHandlingUtils
        //TODO: for converting audio files to .wav and processing them
        return new HashMap<>();
    }


    private static int createAddress(Peak anchor, Peak target){

        int anchorFreqBin = (int)(anchor.getFrequency() / 10);
        int targetFreqBin = (int)(target.getFrequency() / 10);

        int deltaMsRaw = (int)((target.getTime() - anchor.getTime()) * 1000);

        //Create Masks
        int anchorFreqBits = anchorFreqBin & (1 << (MAX_FREQ_BITS) - 1);
        int targetFreqBits = targetFreqBin & (1 << (MAX_FREQ_BITS) - 1);
        int deltaTimeBits = deltaMsRaw & (1 << (MAX_DELTA_BITS) - 1);

        // Combine into 32-bit address
        return (anchorFreqBits << 23) | (targetFreqBits << 14) | deltaTimeBits;
    }
}
