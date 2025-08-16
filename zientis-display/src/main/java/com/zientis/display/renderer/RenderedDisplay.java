package com.zientis.display.renderer;

import com.zientis.display.data.DisplayModel;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 已渲染的展示實例
 * 
 * 記錄展示模型在世界中的渲染狀態和相關資源
 */
public class RenderedDisplay {
    
    private DisplayModel displayModel;
    private List<Location> renderedBlocks;
    private String hologramId;
    private String particleEffectId;
    private LocalDateTime renderTime;
    private LocalDateTime lastUpdate;

    public RenderedDisplay(DisplayModel displayModel) {
        this.displayModel = displayModel;
        this.renderTime = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    /**
     * 更新展示模型
     */
    public void updateModel(DisplayModel newModel) {
        this.displayModel = newModel;
        this.lastUpdate = LocalDateTime.now();
    }

    /**
     * 標記為已更新
     */
    public void markAsUpdated() {
        this.lastUpdate = LocalDateTime.now();
    }

    // === Getters and Setters ===
    
    public DisplayModel getDisplayModel() { return displayModel; }
    public List<Location> getRenderedBlocks() { return renderedBlocks; }
    public String getHologramId() { return hologramId; }
    public String getParticleEffectId() { return particleEffectId; }
    public LocalDateTime getRenderTime() { return renderTime; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    
    public void setRenderedBlocks(List<Location> renderedBlocks) { 
        this.renderedBlocks = renderedBlocks; 
        markAsUpdated();
    }
    
    public void setHologramId(String hologramId) { 
        this.hologramId = hologramId; 
        markAsUpdated();
    }
    
    public void setParticleEffectId(String particleEffectId) { 
        this.particleEffectId = particleEffectId; 
        markAsUpdated();
    }
}