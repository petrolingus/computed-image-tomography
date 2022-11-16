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

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("simple256.jpg");
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

        // Rotate image 3
        int angleSamples = 256;
        double h = Math.PI / (angleSamples - 1);
        double[][] processedPixels = new double[angleSamples][256];
        for (int k = 0; k < angleSamples; k++) {
            double angle = k * h;
            double[][] rotatedPixels = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) ((j - 128) * Math.cos(angle) - (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) ((j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
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

        // FFT 4
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
//                fftPixels[i][j] = fftComplexPixels[i][j].abs();
//                fftPixels[i][j] = Math.log(fftComplexPixels[i][j].abs());
                fftPixels[i][j] = Math.log1p(fftComplexPixels[i][j].abs());
            }
        }
        normalize(fftPixels);
        imageView4.setImage(getImageFromPixels(fftPixels));

        // Rotate FFT 5
        Complex[][] rotatedFftComplexPixels = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                rotatedFftComplexPixels[i][j] = Complex.ZERO;
            }
        }
        for (int k = 0; k < angleSamples; k++) {
            double angle = -k * h + Math.PI / 2;
            for (int i = 0; i < 256; i++) {
                int x = (int) (-(i - 128) * Math.sin(angle)) + 128;
                int y = (int) (+(i - 128) * Math.cos(angle)) + 128;
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
                // TODO: Think about it
//                rotatedFftComplexPixels[y][x] = rotatedFftComplexPixels[y][x].add(fftComplexPixels[k][i]);
                rotatedFftComplexPixels[y][x] = fftComplexPixels[k][i];
            }
        }

        Complex[][] wtfPixels = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (i < 128) ? 128 + i : -128 + i;
                int y = (j < 128) ? 128 + j : -128 + j;
                wtfPixels[i][j] = rotatedFftComplexPixels[y][x];
//                wtfPixels[i][j] = rotatedFftComplexPixels[i][j];
            }
        }

        double[][] rotatedFftPixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
//                rotatedFftPixels[i][j] = wtfPixels[i][j].abs();
//                rotatedFftPixels[i][j] = Math.log(wtfPixels[i][j].abs());
                rotatedFftPixels[i][j] = Math.log1p(wtfPixels[i][j].abs());
            }
        }
        normalize(rotatedFftPixels);
        imageView5.setImage(getImageFromPixels(rotatedFftPixels));

        // Backward 2d FFT 6
        Complex[][] ifft = ImageFourier.ifft(wtfPixels);

        double maxRe = Double.MIN_VALUE;
        double maxIm = Double.MIN_VALUE;

        double[][] result = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (i < 128) ? 128 + i : -128 + i;
                int y = (j < 128) ? 128 + j : -128 + j;
//                x = j;
//                y = i;
                result[i][j] = ifft[y][x].getReal();
                maxRe = Math.max(maxRe, ifft[y][x].getReal());
                maxIm = Math.max(maxIm, ifft[y][x].getImaginary());
            }
        }
        System.out.println("Max Re: " + maxRe);
        System.out.println("Max Im: " + maxIm);
        normalize(result);
        imageView6.setImage(getImageFromPixels(result));

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
