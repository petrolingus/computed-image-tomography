package me.petrolingus.cit;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Algorithm {


//    private void foo(double[][] monochromePixels) {
//
//
//
//        // FFT 4
//        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
//        Complex[][] fftComplexPixels = new Complex[angleSamples][256];
//        for (int i = 0; i < angleSamples; i++) {
//            Complex[] transform = fft.transform(processedPixels[i], TransformType.FORWARD);
//            for (int j = 0; j < 128; j++) {
//                fftComplexPixels[i][j] = transform[128 + j];
//                fftComplexPixels[i][j + 128] = transform[j];
//            }
//        }
//        double[][] fftPixels = new double[angleSamples][256];
//        for (int i = 0; i < angleSamples; i++) {
//            for (int j = 0; j < 256; j++) {
////                fftPixels[i][j] = fftComplexPixels[i][j].abs();
////                fftPixels[i][j] = Math.log(fftComplexPixels[i][j].abs());
//                fftPixels[i][j] = Math.log1p(fftComplexPixels[i][j].abs());
//            }
//        }
//        normalize(fftPixels);
//        imageView4.setImage(getImageFromPixels(fftPixels));
//
//        // Rotate FFT 5
//        Complex[][] rotatedFftComplexPixels = new Complex[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                rotatedFftComplexPixels[i][j] = Complex.ZERO;
//            }
//        }
//        for (int k = 0; k < angleSamples; k++) {
//            double angle = -k * h + Math.PI / 2;
//            for (int i = 0; i < 256; i++) {
//                int x = (int) (-(i - 128) * Math.sin(angle)) + 128;
//                int y = (int) (+(i - 128) * Math.cos(angle)) + 128;
//                if (x < 0 || x > 255 || y < 0 || y > 255) {
//                    continue;
//                }
//                // TODO: Think about it
////                rotatedFftComplexPixels[y][x] = rotatedFftComplexPixels[y][x].add(fftComplexPixels[k][i]);
//                rotatedFftComplexPixels[y][x] = fftComplexPixels[k][i];
//            }
//        }
//
//        Complex[][] wtfPixels = new Complex[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                int x = (i < 128) ? 128 + i : -128 + i;
//                int y = (j < 128) ? 128 + j : -128 + j;
//                wtfPixels[i][j] = rotatedFftComplexPixels[y][x];
////                wtfPixels[i][j] = rotatedFftComplexPixels[i][j];
//            }
//        }
//
//        double[][] rotatedFftPixels = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
////                rotatedFftPixels[i][j] = wtfPixels[i][j].abs();
////                rotatedFftPixels[i][j] = Math.log(wtfPixels[i][j].abs());
//                rotatedFftPixels[i][j] = Math.log1p(wtfPixels[i][j].abs());
//            }
//        }
//        normalize(rotatedFftPixels);
//        imageView5.setImage(getImageFromPixels(rotatedFftPixels));
//
//        // Backward 2d FFT 6
//        Complex[][] ifft = ImageFourier.ifft(wtfPixels);
//
//        double maxRe = Double.MIN_VALUE;
//        double maxIm = Double.MIN_VALUE;
//
//        double[][] result = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                int x = (i < 128) ? 128 + i : -128 + i;
//                int y = (j < 128) ? 128 + j : -128 + j;
////                x = j;
////                y = i;
//                result[i][j] = ifft[y][x].getReal();
//                maxRe = Math.max(maxRe, ifft[y][x].getReal());
//                maxIm = Math.max(maxIm, ifft[y][x].getImaginary());
//            }
//        }
//        System.out.println("Max Re: " + maxRe);
//        System.out.println("Max Im: " + maxIm);
//        normalize(result);
//        imageView6.setImage(getImageFromPixels(result));
//    }

}
