package com.zientis.discord.dto;

/**
 * 語音頻道配置資料傳輸物件
 * 用於儲存語音頻道狀態更新的配置資訊
 */
public class VoiceChannelConfig {
    
    private String channelId;
    private String format;
    private boolean enabled;
    private String channelType;
    
    public VoiceChannelConfig() {
    }
    
    public VoiceChannelConfig(String channelId, String format, boolean enabled) {
        this.channelId = channelId;
        this.format = format;
        this.enabled = enabled;
    }
    
    /**
     * 獲取 Discord 頻道 ID
     * @return 頻道 ID
     */
    public String getChannelId() {
        return channelId;
    }
    
    /**
     * 設定 Discord 頻道 ID
     * @param channelId 頻道 ID
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    /**
     * 獲取頻道名稱格式
     * @return 格式字串
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * 設定頻道名稱格式
     * @param format 格式字串，支援變數替換
     */
    public void setFormat(String format) {
        this.format = format;
    }
    
    /**
     * 檢查頻道是否啟用
     * @return 是否啟用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 設定頻道啟用狀態
     * @param enabled 是否啟用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 獲取頻道類型
     * @return 頻道類型
     */
    public String getChannelType() {
        return channelType;
    }
    
    /**
     * 設定頻道類型
     * @param channelType 頻道類型 (如 player-count, server-status 等)
     */
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }
    
    @Override
    public String toString() {
        return "VoiceChannelConfig{" +
                "channelId='" + channelId + '\'' +
                ", format='" + format + '\'' +
                ", enabled=" + enabled +
                ", channelType='" + channelType + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        VoiceChannelConfig that = (VoiceChannelConfig) o;
        
        if (enabled != that.enabled) return false;
        if (!channelId.equals(that.channelId)) return false;
        if (!format.equals(that.format)) return false;
        return channelType != null ? channelType.equals(that.channelType) : that.channelType == null;
    }
    
    @Override
    public int hashCode() {
        int result = channelId.hashCode();
        result = 31 * result + format.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (channelType != null ? channelType.hashCode() : 0);
        return result;
    }
}