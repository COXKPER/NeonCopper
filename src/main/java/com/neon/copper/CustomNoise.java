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
        this.xOffset = random.nextDouble() * 100;
        this.zOffset = random.nextDouble() * 10000;
    }

    public double evaluateNoise(double x, double z) {
        int mode = getTerrainMode(x, z); // 0 = flat, 1 = mountain, 2 = quirky

        double total = 0;
        double freq = baseFrequency;
        double amp = baseAmplitude;

        for (int i = 0; i < octaves; i++) {
            double nx = (x + xOffset) * freq;
            double nz = (z + zOffset) * freq;

            double noise;

            switch (mode) {
                case 0: // Flat terrain, smooth curves
                    noise = Math.sin(nx * 0.8) * Math.cos(nz * 0.8);
                    noise *= 0.3; // keep it flatter
                    break;

                case 1: // Mountain terrain, layered waves and elevation boosts
                    noise = Math.pow(Math.sin(nx) * Math.cos(nz), 2);
                    noise *= (1 + 0.5 * Math.sin(nz * 0.1)); // minor ridges
                    break;

                case 2: // Quirky terrain, wild patterns
                    noise = Math.sin(nx * 0.7) * Math.cos(nz * 1.3);
                    noise += Math.sin(nz * 0.5 + Math.sin(nx * 0.2));
                    noise *= Math.cos(nx * 0.1 + nz * 0.1);
                    break;

                default:
                    noise = 0;
            }

            total += noise * amp;
            freq *= 2.0 + 0.2 * mode; // different scale per mode
            amp *= persistence;
        }

        return total / octaves;
    }

    private int getTerrainMode(double x, double z) {
        // Grid system: each 100x100 block area gets a different mode
        int gridX = (int) Math.floor(x / 100);
        int gridZ = (int) Math.floor(z / 100);

        int hash = (gridX * 734287) ^ (gridZ * 912443);
        hash ^= (int)(xOffset * 31 + zOffset * 57);
        return Math.abs(hash % 3); // 0: flat, 1: mountain, 2: quirky
    }
}
