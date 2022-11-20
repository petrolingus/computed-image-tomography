package me.petrolingus.cit;

import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;

public class Foo {

    public static void main(String[] args) {

        int n = 4;
        Complex[][] image = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                image[i][j] = new Complex(Math.cos(2 * Math.PI * i / n), Math.sin(2 * Math.PI * j / n));
            }
        }
        drawImage(image);

        Complex[][] spectrum = imageDft(image);
        drawImage(spectrum);

    }

    private static Complex[][] imageDft(Complex[][] image) {

        int n = image.length;

        Complex[][] temp = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            temp[i] = transform(image[i], true);
        }

        Complex[][] spectrum = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            Complex[] col = new Complex[n];
            for (int j = 0; j < n; j++) {
                col[j] = temp[j][i];
            }
            col = transform(col, true);
            for (int j = 0; j < n; j++) {
                spectrum[j][i] = col[j];
            }
        }

        return spectrum;
    }

    private static void drawImage(Complex[][] image) {
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                System.out.printf("(%+5.5f,%5.5f) ", image[i][j].getReal(), image[i][j].getImaginary());
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void testTransform() {
        Complex[] x = new Complex[8];
        for (int i = 0; i < x.length; i++) {
            x[i] = new Complex(Math.cos(i), 0);
        }
        Arrays.stream(x).forEach(System.out::println);

        Complex[] y = transform(x, true);
        System.out.println();
        Arrays.stream(y).forEach(System.out::println);

        Complex[] z = transform(y, false);
        System.out.println();
        Arrays.stream(z).forEach(System.out::println);
    }

    private static Complex[] transform(Complex[] vector, boolean isForward) {
        final int length = vector.length;
        Complex[] result = new Complex[length];
        for (int i = 0; i < length; i++) {
            result[i] = new Complex(0, 0);
        }
        for (int k = 0; k < length; k++) {
            for (int n = 0; n < length; n++) {
                double alpha = 2.0 * Math.PI * k * n / length;
                if (isForward) {
                    result[k] = result[k].add(vector[n].multiply(new Complex(Math.cos(alpha), -Math.sin(alpha))));
                } else {
                    result[k] = result[k].add(vector[n].multiply(new Complex(Math.cos(alpha), Math.sin(alpha))));
                }
            }
        }
        if (!isForward) {
            for (int i = 0; i < length; i++) {
                result[i] = result[i].divide(length);
            }
        }
        return result;
    }
}
