package com.zientis.display.data;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.time.LocalDateTime;

/**
 * 粒子效果數據類
 * 
 * 定義粒子效果的類型、參數和配置
 */
public class ParticleEffectData {
    
    private final Particle particleType;
    private final Location centerLocation;
    private final ParticleEffectType effectType;
    private final int particleCount;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final Object extraData;
    private final long intervalMillis;
    private final boolean enabled;
    private LocalDateTime lastUpdated;

    public ParticleEffectData(Particle particleType, Location centerLocation, ParticleEffectType effectType) {
        this.particleType = particleType;
        this.centerLocation = centerLocation.clone();
        this.effectType = effectType;
        this.particleCount = effectType.getDefaultParticleCount();
        this.offsetX = effectType.getDefaultOffsetX();
        this.offsetY = effectType.getDefaultOffsetY();
        this.offsetZ = effectType.getDefaultOffsetZ();
        this.speed = effectType.getDefaultSpeed();
        this.extraData = null;
        this.intervalMillis = effectType.getDefaultInterval();
        this.enabled = true;
        this.lastUpdated = LocalDateTime.now();
    }

    public ParticleEffectData(Particle particleType, Location centerLocation, ParticleEffectType effectType,
                             int particleCount, double offsetX, double offsetY, double offsetZ, 
                             double speed, Object extraData, long intervalMillis, boolean enabled) {
        this.particleType = particleType;
        this.centerLocation = centerLocation.clone();
        this.effectType = effectType;
        this.particleCount = particleCount;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.extraData = extraData;
        this.intervalMillis = intervalMillis;
        this.enabled = enabled;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 粒子效果類型枚舉
     */
    public enum ParticleEffectType {
        /**
         * 無效果
         */
        NONE(0, 0, 0, 0, 0, 10000),

        /**
         * 基礎環繞效果
         */
        BASIC_ORBIT(10, 1.0, 0.5, 1.0, 0.02, 2000),

        /**
         * 螺旋上升效果
         */
        SPIRAL_UP(15, 1.5, 2.0, 1.5, 0.05, 1500),

        /**
         * 脈衝效果
         */
        PULSE(20, 2.0, 1.0, 2.0, 0.1, 3000),

        /**
         * 魔法環效果
         */
        MAGIC_CIRCLE(25, 3.0, 0.1, 3.0, 0.03, 1000),

        /**
         * 煙霧效果
         */
        SMOKE_CLOUD(30, 2.0, 1.0, 2.0, 0.02, 2500),

        /**
         * 火花效果
         */
        SPARKLE(40, 1.0, 1.0, 1.0, 0.1, 800),

        /**
         * 頂級光環效果
         */
        PREMIUM_AURA(50, 3.0, 2.0, 3.0, 0.05, 500);

        private final int defaultParticleCount;
        private final double defaultOffsetX;
        private final double defaultOffsetY;
        private final double defaultOffsetZ;
        private final double defaultSpeed;
        private final long defaultInterval;

        ParticleEffectType(int particleCount, double offsetX, double offsetY, double offsetZ, 
                          double speed, long interval) {
            this.defaultParticleCount = particleCount;
            this.defaultOffsetX = offsetX;
            this.defaultOffsetY = offsetY;
            this.defaultOffsetZ = offsetZ;
            this.defaultSpeed = speed;
            this.defaultInterval = interval;
        }

        public int getDefaultParticleCount() { return defaultParticleCount; }
        public double getDefaultOffsetX() { return defaultOffsetX; }
        public double getDefaultOffsetY() { return defaultOffsetY; }
        public double getDefaultOffsetZ() { return defaultOffsetZ; }
        public double getDefaultSpeed() { return defaultSpeed; }
        public long getDefaultInterval() { return defaultInterval; }

        /**
         * 根據展示等級獲取推薦的粒子效果
         */
        public static ParticleEffectType getRecommendedForTier(IslandDisplayTier tier) {
            switch (tier) {
                case BASIC: return NONE;
                case ENHANCED: return BASIC_ORBIT;
                case ADVANCED: return SPIRAL_UP;
                case PREMIUM: return PREMIUM_AURA;
                default: return NONE;
            }
        }
    }

    /**
     * 創建禁用的粒子效果
     */
    public static ParticleEffectData createDisabled(Location location) {
        return new ParticleEffectData(Particle.FLAME, location, ParticleEffectType.NONE, 
                                    0, 0, 0, 0, 0, null, 10000, false);
    }

    /**
     * 根據展示等級創建推薦的粒子效果
     */
    public static ParticleEffectData createForTier(Location location, IslandDisplayTier tier) {
        ParticleEffectType effectType = ParticleEffectType.getRecommendedForTier(tier);
        Particle particle = getRecommendedParticle(tier);
        return new ParticleEffectData(particle, location, effectType);
    }

    /**
     * 根據展示等級獲取推薦的粒子類型
     */
    private static Particle getRecommendedParticle(IslandDisplayTier tier) {
        switch (tier) {
            case BASIC: return Particle.FLAME;
            case ENHANCED: return Particle.HAPPY_VILLAGER;
            case ADVANCED: return Particle.ENCHANT;
            case PREMIUM: return Particle.END_ROD;
            default: return Particle.FLAME;
        }
    }

    /**
     * 標記為已更新
     */
    public void markAsUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 檢查是否應該顯示粒子
     */
    public boolean shouldDisplay() {
        return enabled && effectType != ParticleEffectType.NONE && particleCount > 0;
    }

    // === Getters ===
    
    public Particle getParticleType() { return particleType; }
    public Location getCenterLocation() { return centerLocation.clone(); }
    public ParticleEffectType getEffectType() { return effectType; }
    public int getParticleCount() { return particleCount; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public double getSpeed() { return speed; }
    public Object getExtraData() { return extraData; }
    public long getIntervalMillis() { return intervalMillis; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    @Override
    public String toString() {
        return String.format("ParticleEffect{type=%s, effect=%s, count=%d, interval=%dms, enabled=%b}", 
            particleType, effectType, particleCount, intervalMillis, enabled);
    }
}