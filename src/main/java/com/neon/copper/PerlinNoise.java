// Save as PerlinNoise.java
package com.neon.copper;

public class PerlinNoise {
    private final int[] permutation;

    public PerlinNoise(long seed) {
        permutation = new int[512];
        int[] p = new int[256];
        java.util.Random random = new java.util.Random(seed);

        for (int i = 0; i < 256; i++) p[i] = i;

        // Shuffle
        for (int i = 255; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[index];
            p[index] = temp;
        }

        for (int i = 0; i < 512; i++) permutation[i] = p[i & 255];
    }

    public double noise(double x, double y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        double u = fade(x);
        double v = fade(y);

        int A = permutation[X] + Y;
        int B = permutation[X + 1] + Y;

        return lerp(v, lerp(u, grad(permutation[A], x, y),
                                grad(permutation[B], x - 1, y)),
                       lerp(u, grad(permutation[A + 1], x, y - 1),
                                grad(permutation[B + 1], x - 1, y - 1)));
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = (h < 8) ? x : y;
        double v = (h < 4) ? y : (h == 12 || h == 14) ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
