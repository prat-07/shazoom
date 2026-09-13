package com.pratham.backend.engine.model;

public class Complex {
    private final double real;
    private final double imaginary;

    public Complex(double real, double imaginary){
        this.real = real;
        this.imaginary = imaginary;
    }

    public double getImaginary() {
        return imaginary;
    }

    public double getReal() {
        return real;
    }

    @Override
    public String toString() {
        return (real + " + "  + imaginary + "i");
    }

    //add
    public Complex add(Complex other){
        return new Complex(this.real + other.real, this.imaginary + other.imaginary);
    }

    //subtract
    public Complex subtract(Complex other){
        return new Complex(this.real - other.real, this.imaginary - other.imaginary);
    }

    //multiply
    public Complex multiply(Complex other){
        double realPart = this.real * other.real - this.imaginary * other.imaginary;
        double imaginaryPart = this.real * other.imaginary + this.imaginary * other.real;

        return new Complex(realPart, imaginaryPart);
    }

    //magnitude
    public double magnitude(){
        return Math.sqrt(real*real + imaginary*imaginary);
    }
}
