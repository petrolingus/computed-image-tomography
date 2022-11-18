package me.petrolingus.cit;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.*;
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

    public AreaChart<Number, Number> chart;
    public AreaChart<Number, Number> chart2;

    public void initialize() throws FileNotFoundException {

        // Load image
        URL resource = Main.class.getResource("squares256.png");
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
        process(monochromePixels);
    }

    private void process(double[][] monochromePixels) {

        int n = 180;
        double h = Math.PI / (n - 1);
        AtomicInteger counter = new AtomicInteger();
        Complex[][] radon2 = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                radon2[i][j] = new Complex(0, 0);
            }
        }

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(() -> {

            double angle = h * counter.get();
            counter.getAndIncrement();
            if (counter.get() == n) {
                service.shutdown();
            }

            double[][] rotate = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) Math.round((j - 128) * Math.cos(angle) - (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) Math.round((j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
                    if (x < 0 || x > 255 || y < 0 || y > 255) {
                        continue;
                    }
                    rotate[i][j] = monochromePixels[y][x];
                }
            }

            double[] sensors = new double[256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    sensors[i] += rotate[j][i];
                }
            }
            double[] finalSensors = normalize(sensors);

            Complex[] fft = ImageFourier.fft.transform(finalSensors, TransformType.FORWARD);
            Complex[] fft2 = swap(fft);

            double[] absfft = new double[256];
            for (int i = 0; i < 256; i++) {
                absfft[i] = fft2[i].abs();
            }
            double[] finalAbsfft = normalize(absfft);

            for (int j = 0; j < 256; j++) {
                int x = (int) Math.round((j - 128) * Math.cos(angle) + 128);
                int y = (int) Math.round((j - 128) * Math.sin(angle) + 128);
                if (x < 0 || x > 255 || y < 0 || y > 255) {
                    continue;
                }
//                double re = fft2[j].getReal() * Math.cos(angle);
//                double im = fft2[j].getImaginary() * Math.sin(angle);
//                radon2[y][x] = new Complex(re, im);
                radon2[y][x] = fft2[j];
            }
//            Complex[][] shitfRadon = swap(radon2);
//            for (int i = 0; i < 256; i++) {
//                for (int j = 0; j < 256; j++) {
//                    if (i < 128 && j < 128) {
//                        shitfRadon[i][j] = new Complex(shitfRadon[i][j].getReal(), 0);
//                    } else {
//                        shitfRadon[i][j] = new Complex(shitfRadon[i][j].getImaginary(), 0);
//                    }
//                }
//            }

            double[][] abs = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    abs[i][j] = Math.log1p(radon2[i][j].abs());
                }
            }

            Complex[][] ifft = ImageFourier.ifft(radon2);
            double[][] result = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    result[i][j] = ifft[i][j].abs();
                }
            }
            double[][] result2 = swap(result);

            Platform.runLater(() -> {
                imageView1.setImage(getImageFromPixels(normalize(rotate)));
                imageView2.setImage(getImageFromPixels(normalize(abs)));
                imageView3.setImage(getImageFromPixels(normalize(result)));
                imageView4.setImage(getImageFromPixels(normalize(result2)));

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                for (int i = 0; i < 256; i++) {
                    series.getData().add(new XYChart.Data<>(i, finalSensors[i]));
                }
                if(chart.getData().size() != 0) {
                    chart.getData().clear();
                }
                chart.getData().add(series);

                XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
                for (int i = 0; i < 256; i++) {
                    series2.getData().add(new XYChart.Data<>(i, finalAbsfft[i]));
                }
                if(chart2.getData().size() != 0) {
                    chart2.getData().clear();
                }
                chart2.getData().add(series2);
            });
        }, 0, 32, TimeUnit.MILLISECONDS);
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
