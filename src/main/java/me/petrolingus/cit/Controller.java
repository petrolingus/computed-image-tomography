package me.petrolingus.cit;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
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

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("aqua256.jpg");
        if (resource == null) {
            return;
        }

        // Draw raw image
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        Image image = new Image(inputStream);
        imageView1.setImage(image);

        // Make monochrome image
        PixelReader pixelReader = image.getPixelReader();
        double[][] monochromePixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
//                int argb = pixelReader.getArgb(j, i);
//                double r = 255.0 / ((argb & 0xFF << 16) >> 16);
//                double g = 255.0 / ((argb & 0xFF << 8) >> 8);
//                double b = 255.0 / (argb & 0xFF);
//                monochromePixels[i][j] = 0.299 * r + 0.587 * g + 0.114 * b;
                monochromePixels[i][j] = pixelReader.getColor(j, i).getBrightness();
            }
        }
        imageView2.setImage(getImageFromPixels(monochromePixels));

        // Rotate image
        int angleSamples = 256;
        double h = 2.0 * Math.PI / (angleSamples - 1);
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
                    rotatedPixels[i][j] = monochromePixels[y][x];
                }
            }
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    processedPixels[k][i] += rotatedPixels[i][j];
                }
            }
        }
        normalize(processedPixels);
        imageView3.setImage(getImageFromPixels(processedPixels));

        // FFT
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[][] fftComplexPixels = new Complex[angleSamples][256];
        for (int i = 0; i < angleSamples; i++) {
            Complex[] transform = fft.transform(processedPixels[i], TransformType.FORWARD);
            for (int j = 0; j < 128; j++) {
                fftComplexPixels[i][j] = transform[128 + j];
                fftComplexPixels[i][j + 128] = transform[j];
            }
        }
        double[][] fftPixels = new double[angleSamples][256];
        for (int i = 0; i < angleSamples; i++) {
            for (int j = 0; j < 256; j++) {
                fftPixels[i][j] = Math.log10(fftComplexPixels[i][j].abs());
            }
        }
        normalize(fftPixels);
        imageView4.setImage(getImageFromPixels(fftPixels));

        // Rotate FFT
        Complex[][] rotatedFftComplexPixels = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rotatedFftComplexPixels[i][j] = Complex.ZERO;
            }
        }
        for (int k = 0; k < 256; k++) {
            double angle = -k * h;
            for (int i = 0; i < 256; i++) {
                int x = (int) Math.round(-(i - 128) * Math.sin(angle)) + 128;
                int y = (int) Math.round(+(i - 128) * Math.cos(angle)) + 128;
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                // TODO: Think about it
//                rotatedFftComplexPixels[y][x] = rotatedFftComplexPixels[y][x].add(fftComplexPixels[k][i]);
                rotatedFftComplexPixels[y][x] = fftComplexPixels[k][i];
            }
        }
        double[][] rotatedFftPixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rotatedFftPixels[i][j] = Math.log1p(rotatedFftComplexPixels[i][j].abs());
            }
        }
        normalize(rotatedFftPixels);
        imageView5.setImage(getImageFromPixels(rotatedFftPixels));

        // Backward 2d FFT
        Complex[][] temp = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            temp[i] = fft.transform(rotatedFftComplexPixels[i], TransformType.INVERSE);
        }

        Complex[][] temp2 = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            Complex[] col = new Complex[256];
            for (int j = 0; j < 256; j++) {
                col[j] = temp[j][i];
            }
            Complex[] transform = fft.transform(col, TransformType.INVERSE);
            for (int j = 0; j < 256; j++) {
                temp2[j][i] = transform[j];
            }
        }

        double[][] result = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                result[i][j] = temp2[i][j].abs();
            }
        }
        normalize(result);
        imageView6.setImage(getImageFromPixels(result));

    }

    private Image getImageFromPixels(double[][] pixels) {
        int[] buffer = new int[256 * 256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int y = (int) Math.round(255 * pixels[i][j]);
                buffer[256 * i + j] = 0xFF << 24 | y << 16 | y << 8 | y;
            }
        }
        WritableImage image = new WritableImage(256, 256);
        PixelWriter pixelWriter = image.getPixelWriter();
        pixelWriter.setPixels(0, 0, 256, 256, PixelFormat.getIntArgbInstance(), buffer, 0, 256);
        return image;
    }

    private void normalize(double[][] matrix) {
        int w = matrix[0].length;
        int h = matrix.length;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                max = Math.max(max, matrix[i][j]);
                min = Math.min(min, matrix[i][j]);
            }
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                matrix[i][j] = (matrix[i][j] - min) / (max - min);
            }
        }
    }
}
