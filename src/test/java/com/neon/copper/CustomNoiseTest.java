package com.neon.copper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomNoiseTest {

    @Test
    void sameSeedProducesDeterministicTerrain() {
        CustomNoise noiseA = new CustomNoise(12345L, 0.001953125, 10, 6, 0.5);
        CustomNoise noiseB = new CustomNoise(12345L, 0.001953125, 10, 6, 0.5);

        var a = noiseA.sampleTerrain(120, -48);
        var b = noiseB.sampleTerrain(120, -48);

        assertEquals(a.surfaceHeight(), b.surfaceHeight());
        assertEquals(a.biome(), b.biome());
        assertEquals(a.erosionFactor(), b.erosionFactor());
    }

    @Test
    void biomeSamplingCanProduceMultipleBiomeTypes() {
        CustomNoise noise = new CustomNoise(998877L, 0.001953125, 10, 6, 0.5);

        boolean foundPlains = false;
        boolean foundDesert = false;
        for (int x = -2000; x <= 2000; x += 64) {
            for (int z = -2000; z <= 2000; z += 64) {
                var sample = noise.sampleTerrain(x, z);
                foundPlains |= sample.biome() == CustomNoise.Biome.PLAINS;
                foundDesert |= sample.biome() == CustomNoise.Biome.DESERT;
                if (foundPlains && foundDesert) {
                    return;
                }
            }
        }

        fail("Expected both PLAINS and DESERT biomes to appear in sampled region");
    }

    @Test
    void erosionFactorIsNormalized() {
        CustomNoise noise = new CustomNoise(77L, 0.001953125, 10, 6, 0.5);
        var sample = noise.sampleTerrain(512, -512);
        assertTrue(sample.erosionFactor() >= 0.0 && sample.erosionFactor() <= 1.0);
    }

    @Test
    void treeSizeSamplingReturnsValidEnum() {
        CustomNoise noise = new CustomNoise(999L, 0.001953125, 10, 6, 0.5);
        var size = noise.sampleTreeSize(100, 100);
        assertNotNull(size);
    }

    @Test
    void caveGenerationIsBoundedForExtremeCoordinates() {
        CustomNoise noise = new CustomNoise(42L, 0.001953125, 10, 6, 0.5);

        assertDoesNotThrow(() -> noise.isCave(Integer.MAX_VALUE, 10, Integer.MIN_VALUE));
    }
}
