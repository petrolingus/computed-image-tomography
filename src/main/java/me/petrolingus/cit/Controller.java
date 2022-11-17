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
        imageView2.setImage(getImageFromPixels(monochromePixels));

        process1(monochromePixels);
        process2(monochromePixels);
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
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                abs[i][j] = Math.log1p(fft2[i][j].abs());
            }
        }
        imageView11.setImage(getImageFromPixels(normalize(abs)));

        Complex[][] rows = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rows[i][j] = Complex.ZERO;
            }
        }
        double h = Math.PI / 255;
        for (int i = 0; i < 256; i++) {
            double angle = i * h;
            for (int j = 0; j < 256; j++) {
                int x = (int) Math.round((j - 128) * Math.cos(angle)) + 128;
                int y = (int) Math.round((j - 128) * Math.sin(angle)) + 128;
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                rows[i][j] = fft2[y][x];
            }
        }

        Complex[][] rows2 = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 128; j++) {
                rows2[i][j] = rows[i][128 + j];
                rows2[i][j + 128] = rows[i][j];
            }
        }

        double[][] temp1 = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                temp1[i][j] = Math.log1p(rows[i][j].abs());
            }
        }
        imageView21.setImage(getImageFromPixels(normalize(temp1)));

        FastFourierTransformer fa = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[][] radon = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            radon[i] = fa.transform(rows2[i], TransformType.INVERSE);
        }
        double[][] rad = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rad[i][j] = radon[i][j].abs();
//                rad[i][j] = Math.log(radon[i][j].getReal());
//                rad[i][j] = Math.log1p(radon[i][j].getReal());
            }
        }
        imageView31.setImage(getImageFromPixels(normalize(rad)));

    }

    private void process1(double[][] monochromePixels) {

        // Rotate image 3
        int angleSamples = 256;
        double h = Math.PI / (angleSamples - 1);
        double[][] processedPixels = new double[angleSamples][256];
        for (int k = 0; k < angleSamples; k++) {
            double angle = k * h;
            double[][] rotatedPixels = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) Math.round((j - 128) * Math.cos(angle) - (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) Math.round((j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
                    if (x < 0 || x > 255 || y < 0 || y > 255) {
                        continue;
                    }
                    rotatedPixels[y][x] = monochromePixels[i][j];
                }
            }
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    processedPixels[k][i] += rotatedPixels[i][j];
                }
            }
        }
        imageView3.setImage(getImageFromPixels(normalize(processedPixels)));

        // FFT 4
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[][] fftComplexPixels = new Complex[angleSamples][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                fftComplexPixels[i][j] = Complex.ZERO;
            }
        }
        for (int i = 0; i < angleSamples; i++) {
            Complex[] tmp = new Complex[256];
            for (int j = 0; j < 256; j++) {
                tmp[j] = new Complex(processedPixels[i][j], -1);
            }
            Complex[] transform = fft.transform(tmp, TransformType.FORWARD);
            for (int j = 0; j < 128; j++) {
                fftComplexPixels[i][j] = transform[128 + j];
                fftComplexPixels[i][j + 128] = transform[j];
            }
        }
        double[][] fftPixels = new double[angleSamples][256];
        for (int i = 0; i < angleSamples; i++) {
            for (int j = 0; j < 256; j++) {
//                fftPixels[i][j] = fftComplexPixels[i][j].abs();
//                fftPixels[i][j] = Math.log(fftComplexPixels[i][j].abs());
                fftPixels[i][j] = Math.log1p(fftComplexPixels[i][j].abs());
            }
        }
        imageView4.setImage(getImageFromPixels(normalize(fftPixels)));


        // Rotate FFT 5
        Complex[][] rotatedFftComplexPixels = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rotatedFftComplexPixels[i][j] = Complex.ZERO;
            }
        }
        for (int k = 0; k < angleSamples; k++) {
            double angle = k * h + Math.PI / 2.0;
            for (int i = 0; i < 256; i++) {
                int x = (int) Math.round((i - 128) * Math.cos(angle)) + 128;
                int y = (int) Math.round((i - 128) * Math.sin(angle)) + 128;
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                rotatedFftComplexPixels[y][x] = fftComplexPixels[k][i];
            }
        }
        double[][] rotatedFftPixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
//                rotatedFftPixels[i][j] = wtfPixels[i][j].abs();
//                rotatedFftPixels[i][j] = Math.log(wtfPixels[i][j].abs());
                rotatedFftPixels[i][j] = Math.log1p(rotatedFftComplexPixels[i][j].abs());
            }
        }
        imageView5.setImage(getImageFromPixels(normalize(rotatedFftPixels)));

        // Backward 2d FFT 6
        Complex[][] ifft = ImageFourier.ifft(rotatedFftComplexPixels);
        double maxRe = Double.MIN_VALUE;
        double maxIm = Double.MIN_VALUE;
        double[][] result = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? 128 + j : -128 + j;
                int y = (i < 128) ? 128 + i : -128 + i;
//                x = j;
//                y = i;
                result[i][j] = ifft[y][x].abs();
                maxRe = Math.max(maxRe, ifft[y][x].getReal());
                maxIm = Math.max(maxIm, ifft[y][x].getImaginary());
            }
        }

        System.out.println("Max Re: " + maxRe);
        System.out.println("Max Im: " + maxIm);
        imageView6.setImage(getImageFromPixels(normalize(result)));


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
