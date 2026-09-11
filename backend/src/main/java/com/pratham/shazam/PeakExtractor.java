package com.pratham.shazam;

import java.util.ArrayList;
import java.util.List;

public class PeakExtractor {

    private static final int DSP_FACTOR = 4;
    private static final int WINDOW_SIZE = 1024;
    private static final int[][] FREQ_BANDS = {
            {0, 10},
            {10, 20},
            {20, 40},
            {40, 80},
            {80, 160},
            {160, 512}
    };

    public static List<Peak> extractPeaks(
            List<double[]> spectrogram,
            double audioDuration,
            int originalSampleRate
    ){
        if(spectrogram.isEmpty())
            return new ArrayList<>();

        List<Peak> peaks = new ArrayList<>();

        double effectiveSampleRate =    //11,025 Hz
                (double) originalSampleRate / DSP_FACTOR;
        double freqResolution =         //10.77 Hz
                effectiveSampleRate / WINDOW_SIZE;

        //approx time duration of each audio frame
        double frameDuration =          //46.4 ms
                audioDuration / spectrogram.size();

        /**
         * Now we need to extract peaks:
         * For each frame:
             *  1. Divide 512 bins into 6 logarithmic bands
             *  2. Find bin with maximum magnitude from each band
             *  3. Take avgMagnitude of these 6 magnitudes
             *  4. Out of 6 magnitudes, the ones which are greater than the average - becomes the Peak for that particular frame
                    4.1 Now magnitudes not important
                    4.2 Store Peak as, (freq, time)
                    4.3 Time same as frame time.
         * Repeat this for all frames
         */

        for(int frameIdx = 0; frameIdx<spectrogram.size(); frameIdx++){

            double[] magnitudeSpectrum = spectrogram.get(frameIdx);     //freq magnitudes of ith frame
            List<Double> bandMaxMagnitudes = new ArrayList<>();        //max magnitude from each band; always of size 6
            List<Integer> bandMaxBins = new ArrayList<>();              //the freq bin which it belongs to; need for calculating its frequency

            //Find strongest freq MAGNITUDE and BIN for each band
            for(int[] band : FREQ_BANDS){

                int startBin = band[0];
                int endBin = band[1];

                double maxMagnitude = 0;
                int maxBin = startBin;

                for(int bin=startBin; bin<endBin; bin++){

                    double magnitude = magnitudeSpectrum[bin];
                    if(magnitude > maxMagnitude){
                        maxMagnitude = magnitude;
                        maxBin = bin;
                    }
                }

                bandMaxMagnitudes.add(maxMagnitude);
                bandMaxBins.add(maxBin);
            }

            //Find average of these 6 maximum magnitudes
            double avgMagnitude = 0;
            for(double mag : bandMaxMagnitudes)
                avgMagnitude += mag;
            avgMagnitude /= bandMaxMagnitudes.size();

            //Any band whose max is stronger than avg becomes a peak
            for(int i=0; i<bandMaxMagnitudes.size(); i++){

                double magnitude = bandMaxMagnitudes.get(i);
                if(magnitude > avgMagnitude){

                    int freqBin = bandMaxBins.get(i);
                    double peakFreq = freqBin * freqResolution; //convert freq bin -> corresponding freq
                    double peakTime = frameIdx * frameDuration;  //convert frame index -> time

                    peaks.add(new Peak(peakFreq, peakTime));
                }
            }

        }

        return peaks;
    }
}
