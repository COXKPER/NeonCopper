package com.neon.copper;

public class CustomNoise {
    private final double baseFrequency;
    private final double baseAmplitude;
    private final int octaves;
    private final double persistence;
    private final double xOffset;
    private final double zOffset;
    private final PerlinNoise perlin;

    public CustomNoise(long seed, double baseFrequency, double baseAmplitude, int octaves, double persistence) {
        this.baseFrequency = baseFrequency;
        this.baseAmplitude = baseAmplitude;
        this.octaves = octaves;
        this.persistence = persistence;
        this.perlin = new PerlinNoise(seed);

        java.util.Random random = new java.util.Random(seed);
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

            total += perlin.noise(nx, nz) * amp;

            freq *= 2;
            amp *= persistence;
        }

        return total;  // Usually between -1 and 1, normalize if needed
    }
}
