package com.pratham.shazam;

import com.pratham.shazam.models.Complex;

import java.util.ArrayList;
import java.util.List;

public class Spectrogram {

    private static final int DSP_FACTOR = 4;
    private static final int WINDOW_SIZE = 1024;
    private static final double MAX_FREQ = 5000.0;          //5 kHz
    private static final int HOP_SIZE = WINDOW_SIZE / 2;      //50% overlap for time-frequency resolution

    //Main function
    public static List<double[]> spectrogram(
            double[] sample,
            int sampleRate
    ){
        //1. Low-pass filer
        double[] filterSample = lowPassFilter(
                MAX_FREQ,
                sampleRate,
                sample
        );

        //2. Downsample
        double[] downsampledSample = downsample(
                filterSample,
                sampleRate,
                sampleRate/DSP_FACTOR
        );

        //3. Create hanning window
        double[] window = new double[WINDOW_SIZE];
        for(int i=0; i<WINDOW_SIZE; i++){
            double theta = 2 * Math.PI * i / (WINDOW_SIZE-1);
            window[i] = 0.5 - 0.5*Math.cos(theta);
        }
        //4. Store spectrogram frames
        List<double[]> spectrogram = new ArrayList<>();

        //5. Perform STFT
        for(int start=0; start+WINDOW_SIZE <= downsampledSample.length; start+=HOP_SIZE){

            double[] frame = new double[WINDOW_SIZE];

            //need 1024 samples at a time; leftover discarded
            System.arraycopy(
                    downsampledSample,
                    start,
                    frame,
                    0,
                    WINDOW_SIZE
            );

            //6. Apply window
            for(int j=0; j<WINDOW_SIZE; j++)
                frame[j] *= window[j];

            //7. FFT
            Complex[] fftResult = FFT.fft(frame);

            //8. Keep first half and calculate magnitude
            double[] magnitude = new double[WINDOW_SIZE / 2];
            for(int j=0; j<magnitude.length; j++)
                magnitude[j] = fftResult[j].magnitude();

            //9. Add this frame to spectrogram
            spectrogram.add(magnitude);
        }

        return spectrogram;
    }

    private static double[] lowPassFilter(
            double cutoffFreq,
            double sampleRate,
            double[] input
    ) {
        double rc = 1 / (2 * Math.PI * cutoffFreq);
        double dt = 1 / sampleRate;
        double alpha = dt / (rc + dt);

        double[] filteredSignal = new double[input.length];
        double prevOutput = 0;

        for(int i=0; i<input.length; i++){
            double x = input[i];

            if(i == 0)
                filteredSignal[i] = x * alpha;
            else
                filteredSignal[i] = alpha*x + (1 - alpha) * prevOutput;

            prevOutput = filteredSignal[i];
        }
        return filteredSignal;
    }

    private static double[] downsample(double[] input, int originalSampleRate, int targetSampleRate) {
        if (targetSampleRate <= 0 || originalSampleRate <= 0)
            throw new IllegalArgumentException(
                    "Sample rates must be positive"
            );

        if (targetSampleRate > originalSampleRate)
            throw new IllegalArgumentException(
                    "Target sample rate must be less than or equal to original sample rate"
            );


        int ratio = originalSampleRate / targetSampleRate;

        if (ratio == 0)
            throw new IllegalArgumentException(
                    "Invalid ratio calculated from sample rates"
            );

        List<Double> resampled = new ArrayList<>();

        for(int i = 0; i< input.length; i+=ratio){
            int end = i+ratio;
            if(end > input.length)  end = input.length;

            double sum = 0;
            for(int j=i; j<end; j++)
                sum += input[j];

            double avg = sum / (end - i);
            resampled.add(avg);
        }
        //convert List<Double> to double[]
        double[] resampledResult = new double[resampled.size()];
        for(int i=0; i<resampled.size(); i++)
            resampledResult[i] = resampled.get(i);

        return resampledResult;
    }

}
