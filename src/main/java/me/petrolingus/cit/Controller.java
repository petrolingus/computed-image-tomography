package me.petrolingus.cit;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.StackedAreaChart;
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
//        URL resource = Main.class.getResource("simple256.jpg");
//        URL resource = Main.class.getResource("aqua256.jpg");
        if (resource == null) {
            return;
        }

        // Draw raw image 1
        FileInputStream inputStream = new FileInputStream(resource.getPath());
        Image image = new Image(inputStream);
//        imageView1.setImage(image);

        // Make monochrome image 2
        PixelReader pixelReader = image.getPixelReader();
        double[][] monochromePixels = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                monochromePixels[i][j] = pixelReader.getColor(j, i).getBrightness();
            }
        }
        monochromePixels = normalize(monochromePixels);
//        imageView2.setImage(getImageFromPixels(monochromePixels));

//        process1(monochromePixels);
//        process2(monochromePixels);
        process3(monochromePixels);

//        double[][] test1 = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                if (j < 128) {
//                    if (i < 128) {
//                        test1[i][j] = 0;
//                    } else {
//                        test1[i][j] = 0.25;
//                    }
//                } else {
//                    if (i < 128) {
//                        test1[i][j] = 0.5;
//                    } else {
//                        test1[i][j] = 0.75;
//                    }
//                }
//            }
//        }
//        imageView1.setImage(getImageFromPixels(test1));
//
//        double[][] test2 = new double[256][256];
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                int x = (j < 128) ? j + 128 : j - 128;
//                int y = (i < 128) ? i + 128 : i - 128;
//                test2[i][j] = test1[y][x];
//            }
//        }
//        imageView2.setImage(getImageFromPixels(test2));
    }

    private void process3(double[][] monochromePixels) {

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
            Complex[][] shitfRadon = swap(radon2);
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
                    abs[i][j] = Math.log1p(shitfRadon[i][j].abs());
                }
            }

            Complex[][] ifft = ImageFourier.ifft(shitfRadon);
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

//        Complex[][] fft3 = ImageFourier.ifft(fft2);

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

        int n = 100;
        double h = 0.9 * Math.PI / (n - 1);

        // Create radon image
        double[][] radon = new double[n][256];
        for (int k = 0; k < n; k++) {
            double angle = k * h;
            double[][] rotatedPixels = new double[256][256];
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    int x = (int) Math.round((j - 128) * Math.cos(angle) + (i - 128) * Math.sin(angle)) + 128;
                    int y = (int) Math.round(-(j - 128) * Math.sin(angle) + (i - 128) * Math.cos(angle)) + 128;
                    if (x < 0 || x > 255 || y < 0 || y > 255) {
                        continue;
                    }
                    rotatedPixels[i][j] = monochromePixels[y][x];
                }
            }
            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 256; j++) {
                    radon[k][i] += rotatedPixels[j][i];
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
                fft4[i][j] = transform[j + 127];
                fft4[i][j + 127] = transform[j];
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
                double re = fft4[k][i].getReal() * Math.cos(angle);
                double im = fft4[k][i].getImaginary() * Math.sin(angle);
                fft5[y][x] = new Complex(re, im);
            }
        }

//        Complex[][] fft6 = ImageFourier.ifft(fft5);

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
        imageView1.setImage(getImageFromPixels(normalize(abs)));
        imageView2.setImage(getImageFromPixels(normalize(real)));
        imageView3.setImage(getImageFromPixels(normalize(imaginary)));
        imageView4.setImage(getImageFromPixels(normalize(argument)));
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

    private double[] normalize(double[] vector) {
        double min = vector[0];
        double max = vector[0];
        for (int i = 0; i < vector.length; i++) {
            min = Math.min(min, vector[i]);
            max = Math.max(max, vector[i]);
        }
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (vector[i] - min) / (max - min);
        }
        return result;
    }

    private double[][] swap(double[][] matrix) {
        double[][] result = new double[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? j + 128 : j - 128;
                int y = (i < 128) ? i + 128 : i - 128;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    private Complex[][] swap(Complex[][] matrix) {
        Complex[][] result = new Complex[256][256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int x = (j < 128) ? j + 128 : j - 128;
                int y = (i < 128) ? i + 128 : i - 128;
                result[i][j] = matrix[y][x];
            }
        }
        return result;
    }

    private Complex[] swap(Complex[] vector) {
        Complex[] result = new Complex[256];
        for (int i = 0; i < 256; i++) {
            int y = (i < 128) ? i + 128 : i - 128;
            result[i] = vector[y];
        }
        return result;
    }
}
