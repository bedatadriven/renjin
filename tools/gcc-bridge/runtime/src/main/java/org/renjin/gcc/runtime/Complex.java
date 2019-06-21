package org.renjin.gcc.runtime;

public class Complex {

    public static double divideReal(double a, double b, double c, double d) {
        if (c == 0.0 && d == 0.0) {
            return Double.NaN;
        }

        if (Double.isInfinite(a) || Double.isInfinite(b) || Double.isInfinite(c) || Double.isInfinite(d)) {
            return 0;
        }

        if (Math.abs(c) < Math.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;

            return (a * q + b) / denominator;

        } else {
            double q = d / c;
            double denominator = d * q + c;

            return (b * q + a) / denominator;
        }
    }

    public static double divideImaginary(double a, double b, double c, double d) {
        if (c == 0.0 && d == 0.0) {
            return Double.NaN;
        }

        if (Double.isInfinite(a) || Double.isInfinite(b) || Double.isInfinite(c) || Double.isInfinite(d)) {
            return 0;
        }

        if (Math.abs(c) < Math.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;

            return (b * q - a) / denominator;

        } else {
            double q = d / c;
            double denominator = d * q + c;
            return (b - a * q) / denominator;
        }
    }
}
