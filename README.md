# Neon Copper

A Minecraft server project built with Minestom.

## Build

```bash
mvn clean package
```

## Highlights

- Project version: `1.1.0`
- Persistent world seed (`world/world.seed`) to keep terrain stable between restarts.
- World generation now includes:
  - plains + desert biomes,
  - erosion shaping,
  - ocean/sea level filling,
  - caves,
  - four tree sizes (small, medium, large, super large).
- Autosave runs every 120 seconds, including when no players are online.
