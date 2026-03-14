package com.neon.copper;

import java.util.Random;

/**
 * Terrain sampler with biome, erosion, ocean, and tree distribution support.
 */
public class CustomNoise {
    public enum Biome {
        PLAINS,
        DESERT
    }

    public enum TreeSize {
        SMALL,
        MEDIUM,
        LARGE,
        SUPER_LARGE
    }

    public record TerrainSample(int surfaceHeight, Biome biome, double erosionFactor) {}

    private final double baseFrequency;
    private final double baseAmplitude;
    private final int octaves;
    private final double persistence;
    private final double xOffset;
    private final double zOffset;

    private final PerlinNoise baseNoise;
    private final PerlinNoise mountainNoise;
    private final PerlinNoise valleyNoise;
    private final PerlinNoise caveNoise;
    private final PerlinNoise biomeNoise;
    private final PerlinNoise erosionNoise;
    private final PerlinNoise treeNoise;

    public CustomNoise(long seed, double baseFrequency, double baseAmplitude, int octaves, double persistence) {
        this.baseFrequency = baseFrequency;
        this.baseAmplitude = baseAmplitude;
        this.octaves = octaves;
        this.persistence = persistence;

        Random random = new Random(seed);
        this.xOffset = random.nextDouble() * 10000;
        this.zOffset = random.nextDouble() * 10000;

        this.baseNoise = new PerlinNoise(random.nextLong());
        this.mountainNoise = new PerlinNoise(random.nextLong());
        this.valleyNoise = new PerlinNoise(random.nextLong());
        this.caveNoise = new PerlinNoise(random.nextLong());
        this.biomeNoise = new PerlinNoise(random.nextLong());
        this.erosionNoise = new PerlinNoise(random.nextLong());
        this.treeNoise = new PerlinNoise(random.nextLong());
    }

    public double evaluateNoise(double x, double z) {
        double nx = x + xOffset;
        double nz = z + zOffset;

        double base = octaveNoise(baseNoise, nx * baseFrequency, nz * baseFrequency, octaves, persistence);
        double mountain = octaveNoise(mountainNoise, nx * baseFrequency * 0.22, nz * baseFrequency * 0.22, 4, 0.5);
        mountain = Math.max(0, mountain) * 0.7;

        double valley = octaveNoise(valleyNoise, nx * baseFrequency * 0.5, nz * baseFrequency * 0.5, 2, 0.6);
        valley = -Math.max(0, valley) * 0.35;

        return (base + mountain + valley) * (baseAmplitude * 0.65);
    }

    public TerrainSample sampleTerrain(double x, double z) {
        double normalizedBiome = (biomeNoise.noise((x + xOffset) * 0.0012, (z + zOffset) * 0.0012) + 1) / 2.0;
        Biome biome = normalizedBiome > 0.56 ? Biome.DESERT : Biome.PLAINS;

        double baseHeight = evaluateNoise(x, z);
        double erosionFactor = (erosionNoise.noise((x + xOffset) * 0.0007, (z + zOffset) * 0.0007) + 1) / 2.0;
        double erodedHeight = baseHeight - (erosionFactor * 3.5);
        double biomeHeightBoost = biome == Biome.DESERT ? 1.2 : 0.0;

        int surfaceHeight = (int) Math.round(erodedHeight + biomeHeightBoost) + 64;
        return new TerrainSample(surfaceHeight, biome, erosionFactor);
    }

    public boolean isCave(double x, double y, double z) {
        return (y < 35) && octaveNoise3D(caveNoise, x * 0.05, y * 0.05, z * 0.05, 3, 0.5) > 0.56;
    }

    public TreeSize sampleTreeSize(double x, double z) {
        double value = (treeNoise.noise((x + xOffset) * 0.015, (z + zOffset) * 0.015) + 1) / 2.0;
        if (value > 0.93) return TreeSize.SUPER_LARGE;
        if (value > 0.80) return TreeSize.LARGE;
        if (value > 0.60) return TreeSize.MEDIUM;
        return TreeSize.SMALL;
    }

    public boolean shouldGenerateTree(double x, double z, Biome biome) {
        if (biome == Biome.DESERT) {
            return false;
        }
        double density = (treeNoise.noise((x + xOffset) * 0.03, (z + zOffset) * 0.03) + 1) / 2.0;
        return density > 0.74;
    }

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
        return (total / maxValue + 1) / 2;
    }
}
