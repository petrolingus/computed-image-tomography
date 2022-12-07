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
    public ImageView imageView6;

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
//        test1(monochromePixels);
        process(monochromePixels);
    }

    private void test1(double[][] monochromePixels) {

        Complex[][] fft = ImageFourier.fft(monochromePixels);
        fft = swap(fft);
        drawComplex(fft, imageView2, true);

        Complex[][] temp = createComplexMatrix(512, 180);

        Complex[][] fft2 = createComplexMatrix(512, 512);
        for (int i = 0; i < 180; i++) {
            double angle = Math.toRadians(i);
            for (int j = 0; j < 512; j++) {
                double x = j - 256;
                double y = 256 - 256;
                int x1 = (int) Math.round(x * Math.cos(angle) - y * Math.sin(angle));
                int y1 = (int) Math.round(y * Math.cos(angle) + x * Math.sin(angle));
                x1 += 256;
                y1 += 256;
                if (y1 < 0 || x1 < 0 || y1 > 511 || x1 > 511) {
                    continue;
                }
                temp[i][j] = fft[y1][x1];
            }
        }
        drawComplex(temp, imageView3, true);

        Complex[][] recover = createComplexMatrix(512, 512);
        for (int i = 0; i < 180; i++) {
            double angle = Math.toRadians(i);
            for (int j = 0; j < 512; j++) {
                double x = j - 256;
                double y = 256 - 256;
                int x1 = (int) Math.round(x * Math.cos(angle) - y * Math.sin(angle));
                int y1 = (int) Math.round(y * Math.cos(angle) + x * Math.sin(angle));
                x1 += 256;
                y1 += 256;
                if (y1 < 0 || x1 < 0 || y1 > 511 || x1 > 511) {
                    continue;
                }
                recover[y1][x1] = temp[i][j];
            }
        }
        drawComplex(recover, imageView4, true);

        Complex[][] recover2 = ImageFourier.ifft(recover);
        drawComplex(recover2, imageView5, false);
    }

    private Complex[][] createComplexMatrix(int w, int h) {
        Complex[][] result = new Complex[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                result[i][j] = new Complex(0, 0);
            }
        }
        return result;
    }

    private double[][] radon(double[][] monochromePixels) {
        Image image = getImageFromPixels(monochromePixels);

        Canvas canvas = new Canvas(IMAGE_SIZE, IMAGE_SIZE);
        GraphicsContext context = canvas.getGraphicsContext2D();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.BLACK);

        double[][] radon = new double[180][IMAGE_SIZE];

        for (int i = 0; i < 180; i++) {
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
//        imageView2.setImage(getImageFromPixels(normalize(radon)));
        return radon;
    }

    private Complex[][] radon2(double[][] monochromePixels) {

        Complex[][] fft = ImageFourier.fft(monochromePixels);
        fft = swap(fft);
        drawComplex(fft, imageView2, true);

        Complex[][] complexRadon = new Complex[180][IMAGE_SIZE];
        for (int i = 0; i < 180; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                complexRadon[i][j] = Complex.ZERO;
            }
        }
        for (int i = 0; i < 180; i += 1) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                int x = (int) Math.round((j - HALF_IMAGE_SIZE - 0.5) * Math.cos(Math.toRadians(-i)) + HALF_IMAGE_SIZE);
                int y = (int) Math.round((j - HALF_IMAGE_SIZE - 0.5) * Math.sin(Math.toRadians(-i)) + HALF_IMAGE_SIZE);
                if (x < 0 || y < 0 || x > IMAGE_SIZE - 1 || y > IMAGE_SIZE - 1) {
                    continue;
                }
//               complexRadon[i][j] = complexRadon[i][j].add(fft[y][x]);
               complexRadon[i][j] = fft[y][x];
            }
        }
//        for (int i = 0; i < IMAGE_SIZE; i++) {
//            for (int j = 0; j < IMAGE_SIZE; j++) {
//                int dx = i - 256;
//                int dy = j - 256;
//                int r = (int) (Math.round(Math.sqrt(dx * dx + dy * dy)) + 256) % 512;
//                int phi = (int) Math.round(Math.toDegrees(Math.atan2(i - 256, j - 256) + Math.PI));
//                if (phi < 180) {
//                    complexRadon[phi][r] = fft[i][j];
//                } else if (phi < 360) {
//                    complexRadon[phi % 180][r] = fft[i][j];
//                }
//            }
//        }
//        drawComplex(complexRadon, imageView3, true);



//        double[][] radon = new double[180][IMAGE_SIZE];
//        for (int i = 0; i < 180; i++) {
//            Complex[] complex = ImageFourier.fft.transform(complexRadon[i], TransformType.INVERSE);
//            for (int j = 0; j < 256; j++) {
//                radon[i][j] = complex[j].abs();
//            }
//        }
//        drawComplex(complexRadon, imageView2, true);

        return complexRadon;
    }

    private void process(double[][] monochromePixels) {

        double[][] radon = radon(monochromePixels);

        Complex[][] complexRadon = new Complex[180][IMAGE_SIZE];
        for (int i = 0; i < 180; i++) {
            Complex[] transform = ImageFourier.fft.transform(radon[i], TransformType.FORWARD);
            complexRadon[i] = swap(transform);
        }
        drawComplex(complexRadon, imageView3, true);


        Complex[][] spectrum = createComplexMatrix(512, 512);
        for (int i = 0; i < 180; i++) {
            double angle = Math.toRadians(i);
            for (int j = 0; j < 512; j++) {
                double x = j - 256;
                double y = 256 - 256;
                int x1 = (int) Math.round(x * Math.cos(angle) - y * Math.sin(angle));
                int y1 = (int) Math.round(y * Math.cos(angle) + x * Math.sin(angle));
                x1 += 256;
                y1 += 256;
                if (y1 < 0 || x1 < 0 || y1 > 511 || x1 > 511) {
                    continue;
                }
                spectrum[y1][x1] = complexRadon[i][j];
            }
        }
        drawComplex(spectrum, imageView4, true);

        Complex[][] recover = ImageFourier.ifft(spectrum);
        drawComplex(recover, imageView5, false);
        drawComplex(swap(recover), imageView6, false);
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
