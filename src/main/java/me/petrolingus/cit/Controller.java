package me.petrolingus.cit;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.TransformType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import static me.petrolingus.cit.Algorithm.normalize;
import static me.petrolingus.cit.Algorithm.swap;

public class Controller {

    public ImageView imageView;
    public ImageView imageView2;
    public ImageView imageView3;
    public ImageView imageView4;
    public ImageView imageView5;

    private static final int IMAGE_SIZE = 512;
    private static final int HALF_IMAGE_SIZE = 256;

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("phantom.png");
        if (resource == null) {
            return;
        }

        // Draw raw image 1
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        Image image = new Image(inputStream);

        // Make monochrome image 2
        PixelReader pixelReader = image.getPixelReader();
        double[][] monochromePixels = new double[IMAGE_SIZE][IMAGE_SIZE];
        for (int i = 0; i < IMAGE_SIZE; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                monochromePixels[i][j] = pixelReader.getColor(j, i).getBrightness();
            }
        }

        imageView.setImage(getImageFromPixels(monochromePixels));
        process(monochromePixels);
    }

    private double[][] radon(double[][] monochromePixels) {
        Image image = getImageFromPixels(monochromePixels);

        Canvas canvas = new Canvas(IMAGE_SIZE, IMAGE_SIZE);
        GraphicsContext context = canvas.getGraphicsContext2D();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.BLACK);

        double[][] radon = new double[179][IMAGE_SIZE];

        for (int i = 0; i < 179; i++) {
            context.save();
            context.translate(HALF_IMAGE_SIZE, HALF_IMAGE_SIZE);
            context.rotate(i);
            context.drawImage(image, -HALF_IMAGE_SIZE, -HALF_IMAGE_SIZE);
            context.restore();
            PixelReader pixelReader = canvas.snapshot(parameters, null).getPixelReader();
            for (int j = 0; j < IMAGE_SIZE; j++) {
                for (int k = 0; k < IMAGE_SIZE; k++) {
                    radon[i][j] += pixelReader.getColor(k, j).getBrightness();
                }
            }
        }
        imageView2.setImage(getImageFromPixels(normalize(radon)));
        return radon;
    }

    private void process(double[][] monochromePixels) {

        double[][] radon = radon(monochromePixels);

        Complex[][] complexRadon = new Complex[179][IMAGE_SIZE];
        for (int i = 0; i < 179; i++) {
            Complex[] transform = ImageFourier.fft.transform(radon[i], TransformType.FORWARD);
            complexRadon[i] = swap(transform);
        }
        drawComplex(complexRadon, imageView3, true);


        Complex[][] spectrum = new Complex[IMAGE_SIZE][IMAGE_SIZE];
        for (int i = 0; i < IMAGE_SIZE; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                spectrum[i][j] = Complex.ZERO;
            }
        }

//        for (int i = 0; i < IMAGE_SIZE; i++) {
//            spectrum[HALF_IMAGE_SIZE][i] = complexRadon[0][i];
////            spectrum[i][HALF_IMAGE_SIZE] = complexRadon[90][i];
//            spectrum[i][HALF_IMAGE_SIZE] = new Complex(complexRadon[90][i].getImaginary(), complexRadon[90][i].getReal());
//        }

        for (int i = 0; i < 90; i+=5) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                int x = (int) Math.round((j - HALF_IMAGE_SIZE) * Math.cos(Math.toRadians(-i)) + HALF_IMAGE_SIZE);
                int y = (int) Math.round((j - HALF_IMAGE_SIZE) * Math.sin(Math.toRadians(-i)) + HALF_IMAGE_SIZE);
                if (x < 0 || y < 0 || x > IMAGE_SIZE-1 || y > IMAGE_SIZE-1) {
                    continue;
                }
                if (spectrum[y][x].equals(Complex.ZERO)) {
                    double re = complexRadon[i][j].getReal();
                    double im = complexRadon[i][j].getImaginary();
                    double nre = re * Math.cos(Math.toRadians(-i)) - im * Math.sin(Math.toRadians(-i));
                    double nim = re * Math.sin(Math.toRadians(-i)) + im * Math.cos(Math.toRadians(-i));
//                    double nre = complexRadon[i][j].abs() * Math.cos(Math.toRadians(90));
//                    double nim = complexRadon[i][j].abs() * Math.sin(Math.toRadians(90));
                    spectrum[y][x] = new Complex(nre, nim);
                }
            }
        }

//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                double dx = (j - 127.5);
//                double dy = (i - 127.5);
//                int r = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
//                int phi = (int) Math.round(Math.toDegrees(Math.atan2(j - 128, i - 128) + Math.PI));
//                if (phi > 178) {
//                    continue;
//                }
//                spectrum[i][j] = complexRadon[phi][r];
//            }
//        }
        spectrum = swap(spectrum);
        drawComplex(swap(spectrum), imageView4, true);

        Complex[][] recover = ImageFourier.ifft(spectrum);
        drawComplex(recover, imageView5, false);
    }

    private void drawComplex(Complex[][] data, ImageView imageView, boolean isLog) {
        int w = data[0].length;
        int h = data.length;
        double[][] temp = new double[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (isLog) {
                    temp[i][j] = Math.log1p(data[i][j].abs());
                } else {
                    temp[i][j] = data[i][j].abs();
                }
            }
        }
        imageView.setImage(getImageFromPixels(normalize(temp)));
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
