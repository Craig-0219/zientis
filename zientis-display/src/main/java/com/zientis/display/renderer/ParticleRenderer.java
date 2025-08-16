package com.zientis.display.renderer;

import com.zientis.display.data.ParticleEffectData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 粒子效果渲染器
 * 
 * 負責創建、管理和渲染粒子效果
 * 支援多種粒子效果模式和動畫
 */
public class ParticleRenderer {
    
    private static final Logger logger = Logger.getLogger(ParticleRenderer.class.getName());
    
    // 活躍的粒子效果任務
    private final Map<String, ParticleTask> activeTasks;

    public ParticleRenderer() {
        this.activeTasks = new ConcurrentHashMap<>();
    }

    /**
     * 開始粒子效果
     * 
     * @param particleData 粒子效果數據
     * @return 效果ID，如果啟動失敗則返回null
     */
    public String startParticleEffect(ParticleEffectData particleData) {
        if (!particleData.shouldDisplay()) {
            return null;
        }
        
        try {
            String effectId = UUID.randomUUID().toString();
            ParticleTask task = new ParticleTask(effectId, particleData);
            
            // 開始定時任務
            BukkitTask bukkitTask = task.runTaskTimer(
                Bukkit.getPluginManager().getPlugin("ZientisDisplay"), 
                0, 
                particleData.getIntervalMillis() / 50 // 轉換為tick
            );
            
            task.setBukkitTask(bukkitTask);
            activeTasks.put(effectId, task);
            
            logger.info("啟動粒子效果: " + effectId + " (類型: " + particleData.getEffectType() + ")");
            return effectId;
            
        } catch (Exception e) {
            logger.severe("啟動粒子效果時發生錯誤: " + e.getMessage());
            return null;
        }
    }

    /**
     * 停止粒子效果
     * 
     * @param effectId 效果ID
     * @return 是否成功停止
     */
    public boolean stopParticleEffect(String effectId) {
        if (effectId == null) {
            return false;
        }
        
        ParticleTask task = activeTasks.remove(effectId);
        if (task != null) {
            task.cancel();
            logger.info("停止粒子效果: " + effectId);
            return true;
        }
        
        return false;
    }

    /**
     * 更新粒子效果
     * 
     * @param effectId 效果ID
     * @param newData 新的粒子數據
     * @return 是否成功更新
     */
    public boolean updateParticleEffect(String effectId, ParticleEffectData newData) {
        if (effectId == null) {
            return false;
        }
        
        // 停止舊效果
        stopParticleEffect(effectId);
        
        // 啟動新效果
        String newEffectId = startParticleEffect(newData);
        
        // 更新映射
        if (newEffectId != null) {
            // 這裡可能需要額外的邏輯來保持ID一致性
            return true;
        }
        
        return false;
    }

    /**
     * 獲取活躍效果數量
     */
    public int getActiveEffectCount() {
        return activeTasks.size();
    }

    /**
     * 停止所有粒子效果
     */
    public void stopAllEffects() {
        for (ParticleTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
        logger.info("已停止所有粒子效果");
    }

    /**
     * 粒子效果任務類
     */
    private static class ParticleTask extends BukkitRunnable {
        
        private final String effectId;
        private final ParticleEffectData particleData;
        private BukkitTask bukkitTask;
        private int animationStep = 0;
        
        public ParticleTask(String effectId, ParticleEffectData particleData) {
            this.effectId = effectId;
            this.particleData = particleData;
        }
        
        public void setBukkitTask(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }
        
        @Override
        public void run() {
            try {
                renderParticleFrame();
                animationStep++;
            } catch (Exception e) {
                logger.severe("粒子效果渲染錯誤: " + e.getMessage());
                cancel();
            }
        }
        
        @Override
        public void cancel() {
            if (bukkitTask != null) {
                bukkitTask.cancel();
            }
            super.cancel();
        }
        
        /**
         * 渲染一幀粒子效果
         */
        private void renderParticleFrame() {
            Location center = particleData.getCenterLocation();
            ParticleEffectData.ParticleEffectType effectType = particleData.getEffectType();
            
            switch (effectType) {
                case BASIC_ORBIT:
                    renderBasicOrbit(center);
                    break;
                    
                case SPIRAL_UP:
                    renderSpiralUp(center);
                    break;
                    
                case PULSE:
                    renderPulse(center);
                    break;
                    
                case MAGIC_CIRCLE:
                    renderMagicCircle(center);
                    break;
                    
                case SMOKE_CLOUD:
                    renderSmokeCloud(center);
                    break;
                    
                case SPARKLE:
                    renderSparkle(center);
                    break;
                    
                case PREMIUM_AURA:
                    renderPremiumAura(center);
                    break;
                    
                default:
                    // 無效果
                    break;
            }
        }
        
        /**
         * 基礎環繞效果
         */
        private void renderBasicOrbit(Location center) {
            double angle = (animationStep * 0.1) % (2 * Math.PI);
            double radius = 2.0;
            
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            Location particleLocation = new Location(center.getWorld(), x, center.getY() + 0.5, z);
            
            center.getWorld().spawnParticle(
                particleData.getParticleType(),
                particleLocation,
                particleData.getParticleCount(),
                particleData.getOffsetX(),
                particleData.getOffsetY(),
                particleData.getOffsetZ(),
                particleData.getSpeed()
            );
        }
        
        /**
         * 螺旋上升效果
         */
        private void renderSpiralUp(Location center) {
            double angle = (animationStep * 0.2) % (2 * Math.PI);
            double radius = 1.5;
            double height = (animationStep * 0.02) % 3.0; // 3格高度循環
            
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            Location particleLocation = new Location(center.getWorld(), x, center.getY() + height, z);
            
            center.getWorld().spawnParticle(
                particleData.getParticleType(),
                particleLocation,
                particleData.getParticleCount(),
                0.1, 0.1, 0.1,
                particleData.getSpeed()
            );
        }
        
        /**
         * 脈衝效果
         */
        private void renderPulse(Location center) {
            double radius = 1.0 + Math.sin(animationStep * 0.1) * 0.5; // 0.5-1.5半徑脈衝
            int particleCount = (int) (particleData.getParticleCount() * (1.5 - radius + 0.5));
            
            center.getWorld().spawnParticle(
                particleData.getParticleType(),
                center,
                particleCount,
                radius, 0.5, radius,
                particleData.getSpeed()
            );
        }
        
        /**
         * 魔法環效果
         */
        private void renderMagicCircle(Location center) {
            // 創建多個同心圓
            for (int ring = 1; ring <= 3; ring++) {
                double radius = ring * 1.0;
                int points = ring * 8; // 外環更多點
                
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + (animationStep * 0.05 * ring);
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    
                    Location particleLocation = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                    
                    center.getWorld().spawnParticle(
                        particleData.getParticleType(),
                        particleLocation,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
        
        /**
         * 煙霧雲效果
         */
        private void renderSmokeCloud(Location center) {
            // 隨機位置產生煙霧
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 4;
                double offsetY = Math.random() * 2;
                double offsetZ = (Math.random() - 0.5) * 4;
                
                Location smokeLocation = center.clone().add(offsetX, offsetY, offsetZ);
                
                center.getWorld().spawnParticle(
                    particleData.getParticleType(),
                    smokeLocation,
                    3,
                    0.2, 0.2, 0.2,
                    0.01
                );
            }
        }
        
        /**
         * 閃爍效果
         */
        private void renderSparkle(Location center) {
            // 隨機位置產生閃爍粒子
            for (int i = 0; i < 8; i++) {
                double offsetX = (Math.random() - 0.5) * 3;
                double offsetY = Math.random() * 3;
                double offsetZ = (Math.random() - 0.5) * 3;
                
                Location sparkleLocation = center.clone().add(offsetX, offsetY, offsetZ);
                
                center.getWorld().spawnParticle(
                    particleData.getParticleType(),
                    sparkleLocation,
                    1,
                    0, 0, 0,
                    0.1
                );
            }
        }
        
        /**
         * 頂級光環效果
         */
        private void renderPremiumAura(Location center) {
            // 組合多種效果
            renderMagicCircle(center);
            
            // 額外的向上飄散粒子
            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 2;
                double offsetZ = (Math.random() - 0.5) * 2;
                
                Location floatLocation = center.clone().add(offsetX, 0, offsetZ);
                
                center.getWorld().spawnParticle(
                    particleData.getParticleType(),
                    floatLocation,
                    1,
                    0, 0.5, 0,
                    0.02
                );
            }
        }
    }
}