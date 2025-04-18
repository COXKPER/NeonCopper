package com.neon.copper;

import java.util.Random;

public class CustomNoise {
    private final double baseFrequency;
    private final double baseAmplitude;
    private final int octaves;
    private final double persistence;
    private final double xOffset;
    private final double zOffset;

    public CustomNoise(long seed, double baseFrequency, double baseAmplitude, int octaves, double persistence) {
        this.baseFrequency = baseFrequency;
        this.baseAmplitude = baseAmplitude;
        this.octaves = octaves;
        this.persistence = persistence;

        Random random = new Random(seed);
        this.xOffset = random.nextDouble() * 10000;
        this.zOffset = random.nextDouble() * 10000;
    }

    public double evaluateNoise(double x, double z) {
        double total = 0;
        double freq = baseFrequency;
        double amp = baseAmplitude;

        for (int i = 0; i < octaves; i++) {
            double nx = x * freq + xOffset;
            double nz = z * freq + zOffset;

            total += (Math.sin(nx) + Math.cos(nz)) * amp;

            freq *= 2;
            amp *= persistence;
        }

        return total;
    }
}
