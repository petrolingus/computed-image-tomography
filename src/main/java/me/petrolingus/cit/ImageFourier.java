package me.petrolingus.cit;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class ImageFourier {

    public static final FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

    public static Complex[][] fft(Complex[][] pixels) {

        int w = pixels[0].length;
        int h = pixels.length;

        Complex[][] temp1 = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            temp1[i] = fft.transform(pixels[i], TransformType.FORWARD);
        }

        Complex[][] temp2 = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            Complex[] col = new Complex[h];
            for (int j = 0; j < 256; j++) {
                col[j] = temp1[j][i];
            }
            Complex[] transform = fft.transform(col, TransformType.FORWARD);
            for (int j = 0; j < 256; j++) {
                temp2[j][i] = transform[j];
            }
        }

        return temp2;
    }

    public static Complex[][] fft(double[][] pixels) {

        int w = pixels[0].length;
        int h = pixels.length;

        Complex[][] temp = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                temp[i][j] = new Complex(pixels[i][j]);
            }
        }

        return fft(temp);
    }

    public static Complex[][] ifft(Complex[][] pixels) {

        int w = pixels[0].length;
        int h = pixels.length;

//        Complex[][] temp1 = new Complex[h][w];
//        for (int i = 0; i < w; i++) {
//            Complex[] col = new Complex[w];
//            for (int j = 0; j < h; j++) {
//                col[j] = pixels[j][i];
//            }
//            Complex[] transform = fft.transform(col, TransformType.INVERSE);
//            for (int j = 0; j < 256; j++) {
//                temp1[j][i] = transform[j];
//            }
//        }
//
//        Complex[][] temp2 = new Complex[h][w];
//        for (int i = 0; i < h; i++) {
//            temp2[i] = fft.transform(temp1[i], TransformType.INVERSE);
//        }

        Complex[][] temp1 = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            temp1[i] = fft.transform(pixels[i], TransformType.INVERSE);
        }

        Complex[][] temp2 = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            Complex[] col = new Complex[h];
            for (int j = 0; j < 256; j++) {
                col[j] = temp1[j][i];
            }
            Complex[] transform = fft.transform(col, TransformType.INVERSE);
            for (int j = 0; j < 256; j++) {
                temp2[j][i] = transform[j];
            }
        }

        return temp2;
    }


}
