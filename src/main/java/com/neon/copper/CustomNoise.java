// Save as CustomNoise.java
package com.neon.copper;

import java.util.Random;

public class CustomNoise {
    private final double baseFrequency;
    private final double baseAmplitude;
    private final int octaves;
    private final double persistence;
    private final double xOffset;
    private final double zOffset;

    // Layered noise
    private final PerlinNoise baseNoise;
    private final PerlinNoise mountainNoise;
    private final PerlinNoise valleyNoise;
    private final PerlinNoise caveNoise;

    public CustomNoise(long seed, double baseFrequency, double baseAmplitude, int octaves, double persistence) {
        this.baseFrequency = baseFrequency;
        this.baseAmplitude = baseAmplitude;
        this.octaves = octaves;
        this.persistence = persistence;

        Random random = new Random(seed);
        this.xOffset = random.nextDouble() * 10000;
        this.zOffset = random.nextDouble() * 10000;

        // Different noise layers for world features
        this.baseNoise = new PerlinNoise(random.nextLong());
        this.mountainNoise = new PerlinNoise(random.nextLong());
        this.valleyNoise = new PerlinNoise(random.nextLong());
        this.caveNoise = new PerlinNoise(random.nextLong());
    }

    /** Main terrain noise: combines base + mountain + valley */
public double evaluateNoise(double x, double z) {
    double nx = x + xOffset;
    double nz = z + zOffset;

    // Base terrain - halus dan luas
    double base = octaveNoise(baseNoise, nx * baseFrequency, nz * baseFrequency, octaves, persistence);

    // Gunung lembut
    double mountain = octaveNoise(mountainNoise, nx * baseFrequency * 0.25, nz * baseFrequency * 0.25, 4, 0.5);
    mountain = Math.max(0, mountain) * 0.6; // kurangi pengaruh gunung

    // Valley kecil saja
    double valley = octaveNoise(valleyNoise, nx * baseFrequency * 0.5, nz * baseFrequency * 0.5, 2, 0.6);
    valley = -Math.max(0, valley) * 0.3; // kurangi efek lembah

    // Gabungkan dan kecilkan amplitude total
    double height = (base + mountain + valley) * (baseAmplitude * 0.6);
    return height;
}


    /** Optional: check if coordinate should be a cave */
public boolean isCave(double x, double y, double z) { 
    return (y < 35) && octaveNoise3D(caveNoise, x * 0.05, y * 0.05, z * 0.05, 3, 0.5) > 0.55;
}


    // -------- Internal noise methods --------
    private double octaveNoise(PerlinNoise noise, double x, double z, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise.noise(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxValue;
    }

    private double octaveNoise3D(PerlinNoise noise, double x, double y, double z, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise.noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return (total / maxValue + 1) / 2; // normalize 0..1
    }
}
