package me.petrolingus.cit;

public class Img {

    public static double interpolateARGB(double[][] img, int w, int h, final double xNormalized, final double yNormalized) {
        double xF = xNormalized * (w - 1);
        double yF = yNormalized * (h - 1);
        int x = (int) xF;
        int y = (int) yF;
        double c00 = img[y][x]; // getValue(x, y)
        double c01 = img[(y + 1 < h ? y + 1 : y)][x]; // getValue(x, (y + 1 < getHeight() ? y + 1 : y));
        double c10 = img[y][(x + 1 < w ? x + 1 : x)]; //getValue((x + 1 < getWidth() ? x + 1 : x), y);
        double c11 = img[(y + 1 < h ? y + 1 : y)][(x + 1 < w ? x + 1 : x)]; // getValue((x + 1 < getWidth() ? x + 1 : x), (y + 1 < getHeight() ? y + 1 : y));
        return interpolateColors(c00, c01, c10, c11, xF - x, yF - y);
    }

    private static double interpolateColors(final double c00, final double c01, final double c10, final double c11, final double mx, final double my) {
        return blend(blend(c00, c10, mx), blend(c01, c11, mx), my);
    }

    public static int argb_fast(final int a, final int r, final int g, final int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static double blend(final double channel1, final double channel2, final double m) {
        return ((channel2 * m) + (channel1 * (1.0 - m)));
    }

    public static final int a(final int color) {
        return (color >> 24) & 0xff;
    }

    public static final int b(final int color) {
        return (color) & 0xff;
    }

    public static final int g(final int color) {
        return (color >> 8) & 0xff;
    }

    public static final int r(final int color) {
        return (color >> 16) & 0xff;
    }

    public static final double b_normalized(final int color) {
        return b(color) / 255.0;
    }

    public static final double g_normalized(final int color) {
        return g(color) / 255.0;
    }

    public static final double r_normalized(final int color) {
        return r(color) / 255.0;
    }

    public static final double a_normalized(final int color) {
        return a(color) / 255.0;
    }

}
