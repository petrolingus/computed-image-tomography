package me.petrolingus.cit;

import javafx.scene.image.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

public class Controller {

    public ImageView imageView1;
    public ImageView imageView2;
    public ImageView imageView3;
    public ImageView imageView4;
    public ImageView imageView5;
    public ImageView imageView6;

    public ImageView imageView11;
    public ImageView imageView21;
    public ImageView imageView31;
    public ImageView imageView41;
    public ImageView imageView51;
    public ImageView imageView61;

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("squares256.png");
//        URL resource = Main.class.getResource("aqua256.jpg");
        if (resource == null) {
            return;
        }

        // Draw raw image 1
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        Image image = new Image(inputStream);
        imageView1.setImage(image);

        // Make monochrome image 2
        PixelReader pixelReader = image.getPixelReader();
        double[][] monochromePixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                monochromePixels[i][j] = pixelReader.getColor(j, i).getBrightness();
            }
        }
        monochromePixels = normalize(monochromePixels);
        imageView2.setImage(getImageFromPixels(monochromePixels));

        process1(monochromePixels);
        process2(monochromePixels);
    }

    private void process3(double[][] monochromePixels) {

        double[][] rotate = new double[256][256];
        Complex[][] twofft = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                twofft[i][j] = Complex.ZERO;
            }
        }
        int n = 256;
        double h = Math.PI / (n - 1);
        for (int a = 0; a < n; a++) {
            double angle = a * h;
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) Math.floor((j - 128) * Math.cos(angle) + (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) Math.floor(-(j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
                    if (x < 0 || x > 255 || y < 0 || y > 255) {
                        continue;
                    }
                    rotate[i][j] = monochromePixels[y][x];
                }
            }
            imageView3.setImage(getImageFromPixels(rotate));
            double[] xrays = new double[256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    xrays[i] += rotate[j][i];
                }
            }
            Complex[] fftxrays = ImageFourier.fft.transform(xrays, TransformType.FORWARD);
            Complex[] fftxrays2 = new Complex[256];
            for (int i = 0; i < 128; i++) {
                fftxrays2[i] = fftxrays[i + 128];
                fftxrays2[i + 128] = fftxrays[i];
            }
            for (int j = 0; j < 256; j++) {
                int x = (int) Math.floor((j - 128) * Math.cos(angle) + 128);
                int y = (int) Math.floor((j - 128) * Math.sin(angle) + 128);
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                double re = fftxrays2[j].getReal() * Math.cos(angle);
                double im = -fftxrays2[j].getImaginary() * Math.sin(angle);
                twofft[y][x] = twofft[y][x].add(new Complex(im, re)).divide(2);
            }
//                for (int i = 0; i < 256; i++) {
//                    for (int j = 0; j < 256; j++) {
//                        double re = twofft[i][j].getReal() * Math.cos(twofft[i][j].getArgument());
//                        double im = twofft[i][j].getImaginary() * Math.sin(twofft[i][j].getArgument());
//                        twofft[i][j] = new Complex(re, im);
//                    }
//                }
            double[][] res = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    res[i][j] = Math.log1p(twofft[i][j].abs());
                }
            }
            imageView4.setImage(getImageFromPixels(normalize(res)));




//            Complex[][] ifft = ImageFourier.ifft(temp);
//            double[][] res2 = new double[256][256];
//            for (int i = 0; i < 256; i++) {
//                for (int j = 0; j < 256; j++) {
//                    int x = (j < 128) ? 128 + j : j - 128;
//                    int y = (i < 128) ? 128 + i : i - 128;
//                    res2[i][j] = ifft[y][x].abs();
//                }
//            }
//            imageView5.setImage(getImageFromPixels(normalize(res2)));
//
//            double[][] res3 = new double[256][256];
//            for (int i = 0; i < 256; i++) {
//                for (int j = 0; j < 256; j++) {
//                    res3[i][j] = ifft[i][j].abs();
//                }
//            }
//            imageView6.setImage(getImageFromPixels(normalize(res3)));
        }

    }

    private void process2(double[][] monochromePixels) {

        Complex[][] fft = ImageFourier.fft(monochromePixels);
        Complex[][] fft2 = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? 128 + j : -128 + j;
                int y = (i < 128) ? 128 + i : -128 + i;
                fft2[i][j] = fft[y][x];
            }
        }

        double[][] abs = new double[256][256];
        double[][] real = new double[256][256];
        double[][] imaginary = new double[256][256];
        double[][] argument = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                Complex complex = fft2[i][j];
                abs[i][j] = Math.log1p(complex.abs());
                real[i][j] = complex.getReal();
                imaginary[i][j] = complex.getImaginary();
                argument[i][j] = complex.getArgument();
            }
        }
        imageView11.setImage(getImageFromPixels(normalize(abs)));
        imageView21.setImage(getImageFromPixels(normalize(real)));
        imageView31.setImage(getImageFromPixels(normalize(imaginary)));
        imageView41.setImage(getImageFromPixels(normalize(argument)));

//        Complex[][] rows = new Complex[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                rows[i][j] = Complex.ZERO;
//            }
//        }
//        double h = Math.PI / 255;
//        for (int i = 0; i < 256; i++) {
//            double angle = i * h;
//            for (int j = 0; j < 256; j++) {
//                int x = (int) Math.round((j - 128) * Math.cos(angle)) + 128;
//                int y = (int) Math.round((j - 128) * Math.sin(angle)) + 128;
//                if (x < 0 || x > 255 || y < 0 || y > 255) {
//                    continue;
//                }
//                rows[i][j] = fft2[y][x];
//            }
//        }
//
//        Complex[][] rows2 = new Complex[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 128; j++) {
//                rows2[i][j] = rows[i][128 + j];
//                rows2[i][j + 128] = rows[i][j];
//            }
//        }
//
//        double[][] temp1 = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                temp1[i][j] = Math.log1p(rows[i][j].abs());
//            }
//        }
//        imageView21.setImage(getImageFromPixels(normalize(temp1)));
//
//        FastFourierTransformer fa = new FastFourierTransformer(DftNormalization.STANDARD);
//        Complex[][] radon = new Complex[256][256];
//        for (int i = 0; i < 256; i++) {
//            radon[i] = fa.transform(rows2[i], TransformType.INVERSE);
//        }
//        double[][] rad = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                rad[i][j] = radon[i][j].abs();
////                rad[i][j] = Math.log(radon[i][j].getReal());
////                rad[i][j] = Math.log1p(radon[i][j].getReal());
//            }
//        }
//        imageView31.setImage(getImageFromPixels(normalize(rad)));

    }

    private void process1(double[][] monochromePixels) {

        int n = 10000;
        double h = Math.PI / (n - 1);

        // Create radon image
        double[][] radon = new double[n][256];
        for (int k = 0; k < n; k++) {
            double angle = k * h;
            double[][] rotatedPixels = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) Math.floor((j - 128) * Math.cos(angle) + (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) Math.floor(-(j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
                    if (x < 0 || x > 255 || y < 0 || y > 255) {
                        continue;
                    }
                    rotatedPixels[i][j] = monochromePixels[y][x];
                }
            }
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    radon[k][i] += rotatedPixels[i][j];
                }
            }
        }

        // Calculate FFT of radon image
        Complex[][] fft4 = new Complex[n][256];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 256; j++) {
                fft4[i][j] = Complex.ZERO;
            }
        }
        for (int i = 0; i < n; i++) {
            Complex[] transform = ImageFourier.fft.transform(radon[i], TransformType.FORWARD);
            for (int j = 0; j < 128; j++) {
                fft4[i][j] = transform[j + 128];
                fft4[i][j + 128] = transform[j];
            }
        }

        // Create spectrum
        Complex[][] fft5 = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                fft5[i][j] = Complex.ZERO;
            }
        }
        for (int k = 0; k < n; k++) {
            double angle = k * h;
            for (int i = 0; i < 256; i++) {
                int x = (int) Math.round((i - 128) * Math.cos(angle)) + 128;
                int y = (int) Math.round((i - 128) * Math.sin(angle)) + 128;
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                fft5[y][x] = fft4[k][i];
            }
        }

        // Visualization of FFT spectrum
        double[][] abs = new double[256][256];
        double[][] real = new double[256][256];
        double[][] imaginary = new double[256][256];
        double[][] argument = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                Complex complex = fft5[i][j];
                abs[i][j] = Math.log1p(complex.abs());
                real[i][j] = complex.getReal();
                imaginary[i][j] = complex.getImaginary();
                argument[i][j] = complex.getArgument();
            }
        }
        imageView3.setImage(getImageFromPixels(normalize(abs)));
        imageView4.setImage(getImageFromPixels(normalize(real)));
        imageView5.setImage(getImageFromPixels(normalize(imaginary)));
        imageView6.setImage(getImageFromPixels(normalize(argument)));
    }

    private Image getImageFromPixels(double[][] pixels) {
        int w = pixels[0].length;
        int h = pixels.length;
        int[] buffer = new int[w * h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int y = (int) Math.round(255 * pixels[i][j]);
                buffer[w * i + j] = 0xFF << 24 | y << 16 | y << 8 | y;
            }
        }
        WritableImage image = new WritableImage(w, h);
        PixelWriter pixelWriter = image.getPixelWriter();
        pixelWriter.setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), buffer, 0, w);
        return image;
    }

    private double[][] normalize(double[][] matrix) {
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
}
