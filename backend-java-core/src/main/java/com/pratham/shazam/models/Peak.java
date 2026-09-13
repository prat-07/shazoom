package com.pratham.shazam.models;

//Helper class to store high magnitude frequencies and the time they occur.
public class Peak {

    private final double frequency;
    private final double time;

    public Peak(double frequency, double time) {
        this.frequency = frequency;
        this.time = time;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Peak{" +
                "frequency=" + frequency +
                ", time=" + time +
                '}';
    }
}
