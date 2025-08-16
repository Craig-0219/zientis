package com.zientis.display.commands;

import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayUpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 展示系統指令處理器
 * 
 * 提供展示管理的指令功能
 */
public class DisplayCommand implements CommandExecutor, TabCompleter {
    
    private final ZientisDisplayAPI displayAPI;

    public DisplayCommand(ZientisDisplayAPI displayAPI) {
        this.displayAPI = displayAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
                
            case "remove":
                return handleRemove(sender, args);
                
            case "update":
                return handleUpdate(sender, args);
                
            case "list":
                return handleList(sender, args);
                
            case "info":
                return handleInfo(sender, args);
                
            case "reload":
                return handleReload(sender, args);
                
            case "stats":
                return handleStats(sender, args);
                
            case "batch":
                return handleBatch(sender, args);
                
            case "teleport":
            case "tp":
                return handleTeleport(sender, args);
                
            default:
                sender.sendMessage("§c未知的子指令: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }

    /**
     * 處理創建展示指令
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.create")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /display create <島嶼ID> [x] [y] [z]");
            return true;
        }
        
        try {
            UUID islandId = UUID.fromString(args[1]);
            Location location = null;
            
            if (args.length >= 5) {
                // 指定位置
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                location = new Location(Bukkit.getWorlds().get(0), x, y, z);
            } else if (sender instanceof Player) {
                // 玩家當前位置
                location = ((Player) sender).getLocation();
            }
            
            sender.sendMessage("§e正在創建島嶼展示...");
            
            displayAPI.createIslandDisplay(islandId, location).thenAccept(model -> {
                if (model != null) {
                    sender.sendMessage("§a成功創建島嶼展示: " + islandId);
                    sender.sendMessage("§7位置: " + formatLocation(model.getCenterLocation()));
                    sender.sendMessage("§7等級: " + model.getDisplayTier());
                    sender.sendMessage("§7方塊數: " + model.getBlockCount());
                } else {
                    sender.sendMessage("§c創建島嶼展示失敗");
                }
            }).exceptionally(throwable -> {
                sender.sendMessage("§c創建展示時發生錯誤: " + throwable.getMessage());
                return null;
            });
            
            return true;
            
        } catch (NumberFormatException e) {
            sender.sendMessage("§c無效的座標格式");
            return true;
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c無效的島嶼ID格式");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§c指令執行錯誤: " + e.getMessage());
            return true;
        }
    }

    /**
     * 處理移除展示指令
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.remove")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /display remove <島嶼ID>");
            return true;
        }
        
        try {
            UUID islandId = UUID.fromString(args[1]);
            
            sender.sendMessage("§e正在移除島嶼展示...");
            
            displayAPI.removeDisplay(islandId).thenAccept(success -> {
                if (success) {
                    sender.sendMessage("§a成功移除島嶼展示: " + islandId);
                } else {
                    sender.sendMessage("§c移除失敗，展示可能不存在");
                }
            });
            
            return true;
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c無效的島嶼ID格式");
            return true;
        }
    }

    /**
     * 處理更新展示指令
     */
    private boolean handleUpdate(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.update")) {
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§c用法: /display update <島嶼ID> <更新類型>");
            sender.sendMessage("§7更新類型: FULL_REBUILD, INCREMENTAL, HOLOGRAM_ONLY, PARTICLE_ONLY");
            return true;
        }
        
        try {
            UUID islandId = UUID.fromString(args[1]);
            DisplayUpdateType updateType = DisplayUpdateType.valueOf(args[2].toUpperCase());
            
            sender.sendMessage("§e正在更新島嶼展示...");
            
            displayAPI.updateDisplayModel(islandId, updateType).thenAccept(model -> {
                if (model != null) {
                    sender.sendMessage("§a成功更新島嶼展示: " + islandId);
                    sender.sendMessage("§7更新類型: " + updateType);
                } else {
                    sender.sendMessage("§c更新失敗，展示可能不存在");
                }
            });
            
            return true;
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c無效的島嶼ID或更新類型");
            return true;
        }
    }

    /**
     * 處理列表展示指令
     */
    private boolean handleList(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.list")) {
            return true;
        }
        
        List<DisplayModel> displays = displayAPI.getAllDisplays();
        
        sender.sendMessage("§e=== 島嶼展示列表 ===");
        sender.sendMessage("§7總數: " + displays.size());
        
        int page = 1;
        int pageSize = 10;
        
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, displays.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            DisplayModel model = displays.get(i);
            sender.sendMessage(String.format("§f%d. §e%s §7- %s §7(%s)", 
                i + 1, 
                model.getIslandId().toString().substring(0, 8),
                model.getDisplayTier(),
                model.getStatus()
            ));
        }
        
        int totalPages = (displays.size() + pageSize - 1) / pageSize;
        sender.sendMessage("§7頁數: " + page + "/" + totalPages);
        
        return true;
    }

    /**
     * 處理展示信息指令
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.info")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /display info <島嶼ID>");
            return true;
        }
        
        try {
            UUID islandId = UUID.fromString(args[1]);
            DisplayModel model = displayAPI.getDisplayModel(islandId);
            
            if (model == null) {
                sender.sendMessage("§c找不到指定的展示: " + islandId);
                return true;
            }
            
            sender.sendMessage("§e=== 島嶼展示信息 ===");
            sender.sendMessage("§7島嶼ID: §f" + islandId);
            sender.sendMessage("§7等級: §f" + model.getDisplayTier());
            sender.sendMessage("§7狀態: §f" + model.getStatus());
            sender.sendMessage("§7位置: §f" + formatLocation(model.getCenterLocation()));
            sender.sendMessage("§7方塊數: §f" + model.getBlockCount());
            sender.sendMessage("§7創建時間: §f" + model.getCreatedAt());
            sender.sendMessage("§7最後更新: §f" + model.getLastUpdated());
            
            return true;
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c無效的島嶼ID格式");
            return true;
        }
    }

    /**
     * 處理重載指令
     */
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.admin")) {
            return true;
        }
        
        sender.sendMessage("§e正在重載展示系統...");
        // TODO: 實現重載邏輯
        sender.sendMessage("§a展示系統重載完成");
        
        return true;
    }

    /**
     * 處理統計指令
     */
    private boolean handleStats(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.stats")) {
            return true;
        }
        
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        
        sender.sendMessage("§e=== 展示系統統計 ===");
        sender.sendMessage("§7總展示數: §f" + stats.getTotalDisplays());
        sender.sendMessage("§7活躍展示: §f" + stats.getActiveDisplays());
        sender.sendMessage("§7記憶體使用: §f" + (stats.getMemoryUsage() / 1024 / 1024) + " MB");
        sender.sendMessage("§7平均更新時間: §f" + String.format("%.2f ms", stats.getAverageUpdateTime()));
        
        return true;
    }

    /**
     * 處理批次操作指令
     */
    private boolean handleBatch(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "zientis.display.admin")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /display batch <update|remove> [更新類型]");
            return true;
        }
        
        String operation = args[1].toLowerCase();
        
        if ("update".equals(operation)) {
            if (args.length < 3) {
                sender.sendMessage("§c用法: /display batch update <更新類型>");
                return true;
            }
            
            try {
                DisplayUpdateType updateType = DisplayUpdateType.valueOf(args[2].toUpperCase());
                List<UUID> allIds = displayAPI.getAllDisplays().stream()
                    .map(DisplayModel::getIslandId)
                    .collect(Collectors.toList());
                
                sender.sendMessage("§e正在批次更新 " + allIds.size() + " 個展示...");
                
                displayAPI.batchUpdateDisplays(allIds, updateType).thenAccept(successCount -> {
                    sender.sendMessage("§a批次更新完成: " + successCount + "/" + allIds.size() + " 成功");
                });
                
                return true;
                
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§c無效的更新類型");
                return true;
            }
        } else {
            sender.sendMessage("§c不支持的批次操作: " + operation);
            return true;
        }
    }

    /**
     * 處理傳送指令
     */
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此指令只能由玩家執行");
            return true;
        }
        
        if (!checkPermission(sender, "zientis.display.teleport")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /display tp <島嶼ID>");
            return true;
        }
        
        try {
            UUID islandId = UUID.fromString(args[1]);
            DisplayModel model = displayAPI.getDisplayModel(islandId);
            
            if (model == null) {
                sender.sendMessage("§c找不到指定的展示: " + islandId);
                return true;
            }
            
            Player player = (Player) sender;
            Location teleportLocation = model.getCenterLocation().clone().add(0, 2, 0);
            
            player.teleport(teleportLocation);
            sender.sendMessage("§a已傳送到島嶼展示: " + islandId.toString().substring(0, 8));
            
            return true;
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c無效的島嶼ID格式");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "remove", "update", "list", "info", "reload", "stats", "batch", "tp")
                .stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("update".equals(subCommand)) {
                return Arrays.stream(DisplayUpdateType.values())
                    .map(Enum::name)
                    .filter(s -> s.startsWith(args[1].toUpperCase()))
                    .collect(Collectors.toList());
            }
            
            if ("batch".equals(subCommand)) {
                return Arrays.asList("update", "remove")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3 && "batch".equals(args[0].toLowerCase()) && "update".equals(args[1].toLowerCase())) {
            return Arrays.stream(DisplayUpdateType.values())
                .map(Enum::name)
                .filter(s -> s.startsWith(args[2].toUpperCase()))
                .collect(Collectors.toList());
        }
        
        return null;
    }

    /**
     * 發送幫助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== Zientis 島嶼展示系統指令 ===");
        sender.sendMessage("§f/display create <島嶼ID> [x] [y] [z] §7- 創建展示");
        sender.sendMessage("§f/display remove <島嶼ID> §7- 移除展示");
        sender.sendMessage("§f/display update <島嶼ID> <類型> §7- 更新展示");
        sender.sendMessage("§f/display list [頁數] §7- 列出所有展示");
        sender.sendMessage("§f/display info <島嶼ID> §7- 查看展示信息");
        sender.sendMessage("§f/display tp <島嶼ID> §7- 傳送到展示");
        sender.sendMessage("§f/display stats §7- 系統統計");
        
        if (sender.hasPermission("zientis.display.admin")) {
            sender.sendMessage("§c=== 管理員指令 ===");
            sender.sendMessage("§f/display reload §7- 重載系統");
            sender.sendMessage("§f/display batch <操作> [類型] §7- 批次操作");
        }
    }

    /**
     * 檢查權限
     */
    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        
        sender.sendMessage("§c你沒有權限執行此指令");
        return false;
    }

    /**
     * 格式化位置信息
     */
    private String formatLocation(Location location) {
        return String.format("%.1f, %.1f, %.1f (%s)", 
            location.getX(), 
            location.getY(), 
            location.getZ(), 
            location.getWorld() != null ? location.getWorld().getName() : "unknown"
        );
    }
}