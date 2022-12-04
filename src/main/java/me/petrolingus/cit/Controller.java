package me.petrolingus.cit;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.TransformType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static me.petrolingus.cit.Algorithm.normalize;
import static me.petrolingus.cit.Algorithm.swap;

public class Controller {

    public ImageView imageView;
    public ImageView imageView2;

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("simple256.jpg");
        if (resource == null) {
            return;
        }

        // Draw raw image 1
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        Image image = new Image(inputStream);

        // Make monochrome image 2
        PixelReader pixelReader = image.getPixelReader();
        double[][] monochromePixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                monochromePixels[i][j] = pixelReader.getColor(j, i).getBrightness();
            }
        }

        imageView.setImage(getImageFromPixels(monochromePixels));
        process(monochromePixels);
    }

    private double[][] radon(double[][] monochromePixels) {

        Image image = getImageFromPixels(monochromePixels);

        Canvas canvas = new Canvas(256, 256);
        GraphicsContext context = canvas.getGraphicsContext2D();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.BLACK);

        double[][] radon = new double[180][256];

        for (int i = 0; i < 180; i++) {
            context.save();
            context.translate(128, 128);
            context.rotate(i);
            context.drawImage(image, -128, -128);
            context.restore();
            PixelReader pixelReader = canvas.snapshot(parameters, null).getPixelReader();
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    radon[i][j] += pixelReader.getColor(k, j).getBrightness();
                }
            }
        }

        return normalize(radon);
    }

    private void process(double[][] monochromePixels) {

        double[][] radon = radon(monochromePixels);

        Complex[][] complexRadon = new Complex[180][256];
        for (int i = 0; i < 180; i++) {
            Complex[] transform = ImageFourier.fft.transform(radon[i], TransformType.FORWARD);
            for (int j = 0; j < 128; j++) {
                complexRadon[i][j] = transform[j + 128];
                complexRadon[i][j + 128] = transform[j];
            }
//            complexRadon[i] = ImageFourier.fft.transform(radon[i], TransformType.FORWARD);
        }
//        for (int i = 0; i < 180; i++) {
//            complexRadon[i][255] = complexRadon[i][0];
//        }

        Complex[][] spectrum = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                spectrum[i][j] = Complex.ZERO;
            }
        }
        for (int i = 0; i < 180; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (int) Math.floor((j - 127.5) * Math.cos(Math.toRadians(-i)) + 127.5);
                int y = (int) Math.floor((j - 127.5) * Math.sin(Math.toRadians(-i)) + 127.5);
                if (x < 0 || y < 0 || x > 255 || y > 255) {
                    continue;
                }
//                Complex complex = complexRadon[i][j];
//                double re = complex.abs() * Math.cos(Math.toRadians(-i));
//                double im = complex.abs() * Math.sin(Math.toRadians(-i));
//                spectrum[y][x] = new Complex(re, im);
                spectrum[y][x] = complexRadon[i][j];
            }
        }

        spectrum = swap(spectrum);

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                if (i < 128 && j < 128) {
//                    spectrum[i][j] = new Complex(spectrum[i][j].getReal(), 0);
                } else {
                    spectrum[i][j] = new Complex(0, spectrum[i][j].getImaginary());
                }
            }
        }

        Complex[][] recover = ImageFourier.ifft(spectrum);

        double[][] realSpectrum = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
//                realSpectrum[i][j] = Math.log1p(spectrum[i][j].abs());
                realSpectrum[i][j] = recover[i][j].abs();
            }
        }

        realSpectrum = swap(realSpectrum);

        imageView2.setImage(getImageFromPixels(normalize(realSpectrum)));
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
}
