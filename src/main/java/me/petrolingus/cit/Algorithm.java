package me.petrolingus.cit;

import org.apache.commons.math3.complex.Complex;

public class Algorithm {

    public static double[][] normalize(double[][] matrix) {
        int w = matrix[0].length;
        int h = matrix.length;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double[] row : matrix) {
            for (int j = 0; j < w; j++) {
                max = Math.max(max, row[j]);
                min = Math.min(min, row[j]);
            }
        }
        double[][] result = new double[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                result[i][j] = (matrix[i][j] - min) / (max - min);
            }
        }
        return result;
    }

    public static double[] normalize(double[] vector) {
        double min = vector[0];
        double max = vector[0];
        for (int i = 0; i < vector.length; i++) {
            min = Math.min(min, vector[i]);
            max = Math.max(max, vector[i]);
        }
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (vector[i] - min) / (max - min);
        }
        return result;
    }

    public static double[][] swap(double[][] matrix) {
        int w = matrix[0].length;
        int h = matrix.length;
        double[][] result = new double[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int x = (j < w/2) ? j + w/2 : j - w/2;
                int y = (i < h/2) ? i + h/2 : i - h/2;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    public static Complex[][] swap(Complex[][] matrix) {
        int w = matrix[0].length;
        int h = matrix.length;
        Complex[][] result = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int x = (j < w/2) ? j + w/2 : j - w/2;
                int y = (i < h/2) ? i + h/2 : i - h/2;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    @SuppressWarnings("DuplicatedCode")
    public static Complex[] swap(Complex[] vector) {
        int n = vector.length;
        Complex[] result = new Complex[n];
        for (int i = 0; i < n; i++) {
            int y = (i < n / 2) ? i + n / 2 : i - n / 2;
            result[i] = vector[y];
        }
        return result;
    }

    @SuppressWarnings("DuplicatedCode")
    public static double[] swap(double[] vector) {
        int n = vector.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            int y = (i < n / 2) ? i + n / 2 : i - n / 2;
            result[i] = vector[y];
        }
        return result;
    }
}
