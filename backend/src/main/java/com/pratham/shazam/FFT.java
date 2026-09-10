package com.pratham.shazam;

public class FFT {
    public static Complex[] fft(double[] input){
        //convert double input array to complex
        Complex[] complexArray = new Complex[input.length];

        for(int i=0; i<input.length; i++)
            complexArray[i] = new Complex(input[i], 0);

        //Make a copy
        Complex[] fftResult = complexArray.clone();

        return recursiveFft(fftResult);
    }

    private static Complex[] recursiveFft(Complex[] complexArray){
        int N = complexArray.length;
        if(N <= 1)
            return complexArray;

        Complex[] even = new Complex[N/2];
        Complex[] odd = new Complex[N/2];

        for(int i=0; i<N/2; i++){
            even[i] = complexArray[2*i];
            odd[i] = complexArray[2*i + 1];
        }

        even = recursiveFft(even);
        odd = recursiveFft(odd);

        //combine
        Complex[] fftResult = new Complex[N];

        for(int k=0; k<N/2; k++){
            double angle = -2 * Math.PI * k/N;

            Complex t = new Complex(Math.cos(angle), Math.sin(angle));
            Complex tOdd = t.multiply(odd[k]);
            fftResult[k] = even[k].add(tOdd);
            fftResult[k + N/2] = even[k].subtract(tOdd);
        }

        return fftResult;

    }
}
