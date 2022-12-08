package me.petrolingus.cit;

public class Img {

    public static int interpolateARGB(int[][] img, int w, int h, final double xNormalized, final double yNormalized) {
        double xF = xNormalized * (w - 1);
        double yF = yNormalized * (h - 1);
        int x = (int) xF;
        int y = (int) yF;
        int c00 = img[y][x]; // getValue(x, y)
        int c01 = img[(y + 1 < h ? y + 1 : y)][x]; // getValue(x, (y + 1 < getHeight() ? y + 1 : y));
        int c10 = img[y][(x + 1 < w ? x + 1 : x)]; //getValue((x + 1 < getWidth() ? x + 1 : x), y);
        int c11 = img[(y + 1 < h ? y + 1 : y)][(x + 1 < w ? x + 1 : x)]; // getValue((x + 1 < getWidth() ? x + 1 : x), (y + 1 < getHeight() ? y + 1 : y));
        return interpolateColors(c00, c01, c10, c11, xF - x, yF - y);
    }

    private static int interpolateColors(final int c00, final int c01, final int c10, final int c11, final double mx, final double my) {
        return argb_fast/*_bounded*/(
                blend(blend(a(c00), a(c10), mx), blend(a(c01), a(c11), mx), my),
                blend(blend(r(c00), r(c10), mx), blend(r(c01), r(c11), mx), my),
                blend(blend(g(c00), g(c10), mx), blend(g(c01), g(c11), mx), my),
                blend(blend(b(c00), b(c10), mx), blend(b(c01), b(c11), mx), my));
    }

    public static int argb_fast(final int a, final int r, final int g, final int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int blend(final int channel1, final int channel2, final double m) {
        return (int) ((channel2 * m) + (channel1 * (1.0 - m)));
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
