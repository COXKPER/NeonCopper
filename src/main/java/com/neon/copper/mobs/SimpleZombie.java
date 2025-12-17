package com.neon.copper.mobs;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.coordinate.Pos;
import java.util.Comparator;

public class SimpleZombie extends EntityCreature {
    public SimpleZombie() {
        super(EntityType.ZOMBIE);
    }

    private long lastPathUpdate = 0;

    @Override
    public void update(long time) {
        super.update(time);

        // Update path every 20 ticks (approx 1 second)
        if (time - lastPathUpdate < 1000) { // time is in millis in Minestom (usually) or tick based?
            // Minestom update time is usually system time or tick time.
            // Let's assume we check every 20 ticks (1000ms).
            // Actually, Minestom entity update(long time) passes the current server time in millis.
            return;
        }

        // Check if we waited long enough (1000ms = 1s)
        if (System.currentTimeMillis() - lastPathUpdate < 1000) return;
        lastPathUpdate = System.currentTimeMillis();

        // Find nearest player within 20 blocks
        Player nearest = getInstance().getPlayers().stream()
                .filter(p -> p.getInstance() == getInstance())
                .min(Comparator.comparingDouble(p -> p.getPosition().distanceSquared(getPosition())))
                .orElse(null);

        if (nearest != null && nearest.getPosition().distanceSquared(getPosition()) < 400) { // 20^2 = 400
            getNavigator().setPathTo(nearest.getPosition());
        }
    }
}
