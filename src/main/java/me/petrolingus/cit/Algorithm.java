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
        double[][] result = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? j + 128 : j - 128;
                int y = (i < 128) ? i + 128 : i - 128;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    public static Complex[][] swap(Complex[][] matrix) {
        Complex[][] result = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? j + 128 : j - 128;
                int y = (i < 128) ? i + 128 : i - 128;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    public static Complex[] swap(Complex[] vector) {
        Complex[] result = new Complex[256];
        for (int i = 0; i < 256; i++) {
            int y = (i < 128) ? i + 128 : i - 128;
            result[i] = vector[y];
        }
        return result;
    }
}
