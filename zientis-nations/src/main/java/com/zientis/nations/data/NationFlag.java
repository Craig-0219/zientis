package com.zientis.nations.data;

import java.util.Arrays;
import java.util.List;

/**
 * 國家旗幟數據類
 * 
 * 定義國家的視覺標識和旗幟設計
 */
public class NationFlag {
    
    private final String primaryColor;
    private final String secondaryColor;
    private final String accentColor;
    private final FlagPattern pattern;
    private final String symbol;
    private final String motto;
    
    // 預定義的顏色選項
    public static final List<String> AVAILABLE_COLORS = Arrays.asList(
        "§c", "§6", "§e", "§a", "§b", "§9", "§d", "§5", "§f", "§7", "§8", "§4", "§2", "§3", "§1", "§0"
    );
    
    // 預定義的旗幟圖案
    public enum FlagPattern {
        SOLID("純色", "單一顏色的旗幟"),
        HORIZONTAL_STRIPES("橫條紋", "水平條紋圖案"),
        VERTICAL_STRIPES("豎條紋", "垂直條紋圖案"),
        DIAGONAL("對角線", "對角線分割圖案"),
        CROSS("十字", "十字形圖案"),
        SALTIRE("聖安德魯十字", "X形十字圖案"),
        GRADIENT("漸變", "顏色漸變效果"),
        BORDER("邊框", "帶邊框的設計"),
        CENTER_EMBLEM("中央徽章", "中央帶徽章的設計"),
        CORNER_SYMBOL("角落符號", "角落帶符號的設計");
        
        private final String displayName;
        private final String description;
        
        FlagPattern(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public NationFlag(String primaryColor, String secondaryColor, String accentColor, 
                     FlagPattern pattern, String symbol, String motto) {
        this.primaryColor = validateColor(primaryColor);
        this.secondaryColor = validateColor(secondaryColor);
        this.accentColor = validateColor(accentColor);
        this.pattern = pattern != null ? pattern : FlagPattern.SOLID;
        this.symbol = symbol != null ? symbol : "⚡";
        this.motto = motto != null ? motto : "";
    }
    
    /**
     * 創建默認旗幟
     */
    public static NationFlag createDefault() {
        return new NationFlag("§f", "§7", "§6", FlagPattern.SOLID, "⚡", "團結就是力量");
    }
    
    /**
     * 創建隨機旗幟
     */
    public static NationFlag createRandom() {
        String[] colors = AVAILABLE_COLORS.toArray(new String[0]);
        String primary = colors[(int)(Math.random() * colors.length)];
        String secondary = colors[(int)(Math.random() * colors.length)];
        String accent = colors[(int)(Math.random() * colors.length)];
        
        FlagPattern[] patterns = FlagPattern.values();
        FlagPattern pattern = patterns[(int)(Math.random() * patterns.length)];
        
        String[] symbols = {"⚡", "⭐", "🔥", "💎", "🗡️", "🛡️", "👑", "🏰", "🦅", "🐉"};
        String symbol = symbols[(int)(Math.random() * symbols.length)];
        
        return new NationFlag(primary, secondary, accent, pattern, symbol, "");
    }
    
    /**
     * 驗證顏色代碼
     */
    private String validateColor(String color) {
        if (color == null || !AVAILABLE_COLORS.contains(color)) {
            return "§f"; // 默認白色
        }
        return color;
    }
    
    /**
     * 生成旗幟的ASCII藝術展示
     */
    public String generateAsciiFlag() {
        StringBuilder flag = new StringBuilder();
        
        switch (pattern) {
            case SOLID:
                flag.append(generateSolidFlag());
                break;
            case HORIZONTAL_STRIPES:
                flag.append(generateHorizontalStripes());
                break;
            case VERTICAL_STRIPES:
                flag.append(generateVerticalStripes());
                break;
            case CROSS:
                flag.append(generateCrossFlag());
                break;
            case CENTER_EMBLEM:
                flag.append(generateCenterEmblem());
                break;
            default:
                flag.append(generateSolidFlag());
                break;
        }
        
        return flag.toString();
    }
    
    private String generateSolidFlag() {
        return primaryColor + "████████████\n" +
               primaryColor + "██" + secondaryColor + symbol + primaryColor + "███████\n" +
               primaryColor + "████████████\n" +
               primaryColor + "████████████§r";
    }
    
    private String generateHorizontalStripes() {
        return primaryColor + "████████████\n" +
               secondaryColor + "████████████\n" +
               accentColor + "████████████\n" +
               secondaryColor + "████████████§r";
    }
    
    private String generateVerticalStripes() {
        return primaryColor + "████" + secondaryColor + "████" + accentColor + "████\n" +
               primaryColor + "████" + secondaryColor + "████" + accentColor + "████\n" +
               primaryColor + "████" + secondaryColor + "████" + accentColor + "████\n" +
               primaryColor + "████" + secondaryColor + "████" + accentColor + "████§r";
    }
    
    private String generateCrossFlag() {
        return primaryColor + "█████" + secondaryColor + "██" + primaryColor + "█████\n" +
               secondaryColor + "████████████\n" +
               secondaryColor + "████████████\n" +
               primaryColor + "█████" + secondaryColor + "██" + primaryColor + "█████§r";
    }
    
    private String generateCenterEmblem() {
        return primaryColor + "████████████\n" +
               primaryColor + "█████" + accentColor + symbol + symbol + primaryColor + "█████\n" +
               primaryColor + "█████" + accentColor + symbol + symbol + primaryColor + "█████\n" +
               primaryColor + "████████████§r";
    }
    
    /**
     * 生成旗幟的聊天格式展示
     */
    public String getChatDisplay() {
        return primaryColor + "▋" + secondaryColor + "▋" + accentColor + "▋§r " + symbol + " ";
    }
    
    /**
     * 生成旗幟的簡化展示
     */
    public String getCompactDisplay() {
        return primaryColor + "█" + secondaryColor + "█" + accentColor + "█§r";
    }
    
    /**
     * 生成旗幟的詳細描述
     */
    public String getDetailedDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("§7旗幟設計: §f").append(pattern.getDisplayName()).append("\n");
        desc.append("§7主色: ").append(primaryColor).append("███§7, ");
        desc.append("副色: ").append(secondaryColor).append("███§7, ");
        desc.append("強調色: ").append(accentColor).append("███§7\n");
        desc.append("§7符號: §f").append(symbol).append("\n");
        if (!motto.isEmpty()) {
            desc.append("§7座右銘: §f").append(motto);
        }
        return desc.toString();
    }
    
    /**
     * 檢查旗幟是否有效
     */
    public boolean isValid() {
        return primaryColor != null && 
               secondaryColor != null && 
               accentColor != null && 
               pattern != null && 
               symbol != null;
    }
    
    /**
     * 計算旗幟的美觀度評分 (0-100)
     */
    public int getAestheticScore() {
        int score = 50; // 基礎分
        
        // 顏色搭配評分
        if (!primaryColor.equals(secondaryColor)) score += 10;
        if (!primaryColor.equals(accentColor)) score += 10;
        if (!secondaryColor.equals(accentColor)) score += 10;
        
        // 圖案複雜度評分
        switch (pattern) {
            case SOLID: score += 5; break;
            case HORIZONTAL_STRIPES:
            case VERTICAL_STRIPES: score += 10; break;
            case CROSS:
            case SALTIRE: score += 15; break;
            case CENTER_EMBLEM:
            case CORNER_SYMBOL: score += 20; break;
            default: score += 8; break;
        }
        
        // 符號獨特性評分
        if (symbol.length() == 1) score += 5;
        
        // 座右銘評分
        if (!motto.isEmpty() && motto.length() <= 20) score += 10;
        
        return Math.min(100, Math.max(0, score));
    }
    
    // === Getter 方法 ===
    
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getAccentColor() { return accentColor; }
    public FlagPattern getPattern() { return pattern; }
    public String getSymbol() { return symbol; }
    public String getMotto() { return motto; }
    
    @Override
    public String toString() {
        return String.format("NationFlag{pattern=%s, colors=[%s,%s,%s], symbol=%s}", 
            pattern.name(), primaryColor, secondaryColor, accentColor, symbol);
    }
}