package com.zientis.display.data;

import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleEffectData {

    public enum ParticleEffectType {
        STATIC,
        ANIMATED,
        INTERACTIVE,
        BASIC_ORBIT,
        SPIRAL_UP,
        PULSE,
        MAGIC_CIRCLE,
        SMOKE_CLOUD,
        SPARKLE,
        PREMIUM_AURA
    }

    private final ParticleEffectType effectType;
    private final Particle particleType;
    private final Location centerLocation;
    private final int particleCount;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final long intervalMillis;
    private final boolean shouldDisplay;

    public ParticleEffectData(ParticleEffectType effectType, Particle particleType, Location centerLocation, int particleCount, double offsetX, double offsetY, double offsetZ, double speed, long intervalMillis, boolean shouldDisplay) {
        this.effectType = effectType;
        this.particleType = particleType;
        this.centerLocation = centerLocation;
        this.particleCount = particleCount;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.intervalMillis = intervalMillis;
        this.shouldDisplay = shouldDisplay;
    }

    public static ParticleEffectData createForTier(Location location, IslandDisplayTier tier) {
        // Placeholder implementation with default values
        return new ParticleEffectData(
                ParticleEffectType.BASIC_ORBIT, // Changed to a valid case
                Particle.HAPPY_VILLAGER,
                location,
                10,
                0.5, 0.5, 0.5,
                0.1,
                1000,
                true
        );
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public ParticleEffectType getEffectType() {
        return effectType;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public Particle getParticleType() {
        return particleType;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public double getSpeed() {
        return speed;
    }
}
