package com.zientis.nations.commands;

import com.zientis.nations.api.ZientisNationsAPI;
import com.zientis.nations.data.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 國家系統主指令處理器
 */
public class NationCommand implements CommandExecutor, TabCompleter {
    
    private final ZientisNationsAPI nationsAPI;
    
    public NationCommand(ZientisNationsAPI nationsAPI) {
        this.nationsAPI = nationsAPI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
            case "disband":
                return handleDelete(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "invite":
                return handleInvite(sender, args);
            case "accept":
                return handleAccept(sender, args);
            case "kick":
                return handleKick(sender, args);
            case "promote":
                return handlePromote(sender, args);
            case "deposit":
                return handleDeposit(sender, args);
            case "withdraw":
                return handleWithdraw(sender, args);
            case "list":
                return handleList(sender, args);
            case "war":
                return handleWar(sender, args);
            case "peace":
                return handlePeace(sender, args);
            case "ally":
                return handleAlly(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "未知的子指令: " + subCommand);
                return true;
        }
    }
    
    /**
     * 創建國家
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以創建國家");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation create <國家名稱> [描述]");
            return true;
        }
        
        Player player = (Player) sender;
        String nationName = args[1];
        String description = args.length > 2 ? 
            String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
        
        nationsAPI.createNation(player.getUniqueId(), nationName, description)
            .thenAccept(nation -> {
                player.sendMessage(ChatColor.GREEN + "成功創建國家: " + nationName);
                player.sendMessage(ChatColor.YELLOW + "你現在是這個國家的創建者！");
            })
            .exceptionally(throwable -> {
                player.sendMessage(ChatColor.RED + "創建國家失敗: " + throwable.getMessage());
                return null;
            });
        
        return true;
    }
    
    /**
     * 解散國家
     */
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以解散國家");
            return true;
        }
        
        Player player = (Player) sender;
        Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
        
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "你不屬於任何國家");
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.deleteNation(nation.getId(), player.getUniqueId())
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "成功解散國家: " + nation.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "解散國家失敗");
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(ChatColor.RED + "解散國家失敗: " + throwable.getMessage());
                return null;
            });
        
        return true;
    }
    
    /**
     * 查看國家信息
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        String targetNationName = null;
        
        if (args.length > 1) {
            targetNationName = args[1];
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
            if (nationOpt.isPresent()) {
                targetNationName = nationOpt.get().getName();
            }
        }
        
        if (targetNationName == null) {
            sender.sendMessage(ChatColor.RED + "請指定國家名稱或加入一個國家");
            return true;
        }
        
        Optional<Nation> nationOpt = nationsAPI.getNationByName(targetNationName);
        if (nationOpt.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "找不到國家: " + targetNationName);
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        // 獲取統計信息
        nationsAPI.getNationStats(nation.getId())
            .thenAccept(stats -> {
                if (stats != null) {
                    sendNationInfo(sender, nation, stats);
                } else {
                    sender.sendMessage(ChatColor.RED + "無法獲取國家統計信息");
                }
            });
        
        return true;
    }
    
    /**
     * 邀請玩家加入國家
     */
    private boolean handleInvite(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以邀請成員");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation invite <玩家名稱>");
            return true;
        }
        
        Player player = (Player) sender;
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "找不到玩家: " + targetName);
            return true;
        }
        
        Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "你不屬於任何國家");
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.inviteMember(nation.getId(), player.getUniqueId(), target.getUniqueId())
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "已邀請 " + targetName + " 加入國家");
                    target.sendMessage(ChatColor.YELLOW + "你被邀請加入國家: " + nation.getName());
                    target.sendMessage(ChatColor.YELLOW + "使用 /nation accept " + nation.getName() + " 來接受邀請");
                } else {
                    player.sendMessage(ChatColor.RED + "邀請失敗");
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(ChatColor.RED + "邀請失敗: " + throwable.getMessage());
                return null;
            });
        
        return true;
    }
    
    /**
     * 接受邀請
     */
    private boolean handleAccept(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以接受邀請");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation accept <國家名稱>");
            return true;
        }
        
        Player player = (Player) sender;
        String nationName = args[1];
        
        Optional<Nation> nationOpt = nationsAPI.getNationByName(nationName);
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "找不到國家: " + nationName);
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.acceptInvitation(player.getUniqueId(), nation.getId())
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "成功加入國家: " + nationName);
                    // 通知國家成員
                    broadcastToNation(nation, ChatColor.YELLOW + player.getName() + " 加入了國家！");
                } else {
                    player.sendMessage(ChatColor.RED + "加入國家失敗，可能沒有有效的邀請");
                }
            });
        
        return true;
    }
    
    /**
     * 踢出成員
     */
    private boolean handleKick(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以踢出成員");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation kick <玩家名稱>");
            return true;
        }
        
        Player player = (Player) sender;
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "找不到玩家: " + targetName);
            return true;
        }
        
        Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "你不屬於任何國家");
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.removeMember(nation.getId(), player.getUniqueId(), target.getUniqueId())
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "已踢出成員: " + targetName);
                    target.sendMessage(ChatColor.RED + "你被踢出了國家: " + nation.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "踢出成員失敗");
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(ChatColor.RED + "踢出成員失敗: " + throwable.getMessage());
                return null;
            });
        
        return true;
    }
    
    /**
     * 存款到國庫
     */
    private boolean handleDeposit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以存款");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation deposit <金額>");
            return true;
        }
        
        Player player = (Player) sender;
        double amount;
        
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "金額必須大於0");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無效的金額");
            return true;
        }
        
        Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "你不屬於任何國家");
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.depositToTreasury(nation.getId(), player.getUniqueId(), amount)
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + 
                        String.format("成功存入 %.2f 到國庫", amount));
                } else {
                    player.sendMessage(ChatColor.RED + "存款失敗，餘額不足");
                }
            });
        
        return true;
    }
    
    /**
     * 從國庫提款
     */
    private boolean handleWithdraw(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以提款");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /nation withdraw <金額>");
            return true;
        }
        
        Player player = (Player) sender;
        double amount;
        
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "金額必須大於0");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無效的金額");
            return true;
        }
        
        Optional<Nation> nationOpt = nationsAPI.getNationByMember(player.getUniqueId());
        if (nationOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "你不屬於任何國家");
            return true;
        }
        
        Nation nation = nationOpt.get();
        
        nationsAPI.withdrawFromTreasury(nation.getId(), player.getUniqueId(), amount)
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + 
                        String.format("成功從國庫提取 %.2f", amount));
                } else {
                    player.sendMessage(ChatColor.RED + "提款失敗，國庫餘額不足或權限不夠");
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(ChatColor.RED + "提款失敗: " + throwable.getMessage());
                return null;
            });
        
        return true;
    }
    
    /**
     * 列出所有國家
     */
    private boolean handleList(CommandSender sender, String[] args) {
        List<Nation> nations = nationsAPI.getAllNations();
        
        if (nations.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "目前沒有任何國家");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== 國家列表 ===");
        for (Nation nation : nations) {
            sender.sendMessage(String.format("%s%s %s- 成員數: %d, 等級: %d", 
                ChatColor.GREEN, nation.getName(),
                ChatColor.GRAY, nation.getMembers().size(), nation.getLevel()));
        }
        
        return true;
    }
    
    /**
     * 發送幫助信息
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Zientis 國家系統指令 ===");
        sender.sendMessage(ChatColor.YELLOW + "/nation create <名稱> [描述] - 創建國家");
        sender.sendMessage(ChatColor.YELLOW + "/nation delete - 解散國家");
        sender.sendMessage(ChatColor.YELLOW + "/nation info [國家名稱] - 查看國家信息");
        sender.sendMessage(ChatColor.YELLOW + "/nation invite <玩家> - 邀請玩家");
        sender.sendMessage(ChatColor.YELLOW + "/nation accept <國家名稱> - 接受邀請");
        sender.sendMessage(ChatColor.YELLOW + "/nation kick <玩家> - 踢出成員");
        sender.sendMessage(ChatColor.YELLOW + "/nation deposit <金額> - 存款到國庫");
        sender.sendMessage(ChatColor.YELLOW + "/nation withdraw <金額> - 從國庫提款");
        sender.sendMessage(ChatColor.YELLOW + "/nation list - 列出所有國家");
        sender.sendMessage(ChatColor.YELLOW + "/nation war <國家名稱> - 宣戰");
        sender.sendMessage(ChatColor.YELLOW + "/nation peace <國家名稱> - 和平");
        sender.sendMessage(ChatColor.YELLOW + "/nation ally <國家名稱> - 結盟");
    }
    
    /**
     * 發送國家信息
     */
    private void sendNationInfo(CommandSender sender, Nation nation, NationStats stats) {
        sender.sendMessage(ChatColor.GOLD + "=== " + nation.getName() + " 國家信息 ===");
        sender.sendMessage(ChatColor.YELLOW + "創建者: " + ChatColor.WHITE + 
            Bukkit.getOfflinePlayer(nation.getFounderId()).getName());
        sender.sendMessage(ChatColor.YELLOW + "描述: " + ChatColor.WHITE + nation.getDescription());
        sender.sendMessage(ChatColor.YELLOW + "等級: " + ChatColor.WHITE + nation.getLevel());
        sender.sendMessage(ChatColor.YELLOW + "成員數: " + ChatColor.WHITE + stats.getMemberCount());
        sender.sendMessage(ChatColor.YELLOW + "國庫: " + ChatColor.WHITE + String.format("%.2f", stats.getTreasury()));
        sender.sendMessage(ChatColor.YELLOW + "領土數: " + ChatColor.WHITE + stats.getTerritoryCount());
        sender.sendMessage(ChatColor.YELLOW + "同盟數: " + ChatColor.WHITE + stats.getAllianceCount());
        sender.sendMessage(ChatColor.YELLOW + "戰爭數: " + ChatColor.WHITE + stats.getWarCount());
        sender.sendMessage(ChatColor.YELLOW + "國力: " + ChatColor.WHITE + String.format("%.1f", stats.getPower()));
        
        // 顯示成員列表
        sender.sendMessage(ChatColor.GOLD + "=== 成員列表 ===");
        for (Map.Entry<UUID, NationRole> entry : nation.getMembers().entrySet()) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            String roleName = getRoleDisplayName(entry.getValue());
            sender.sendMessage(String.format("%s%s %s- %s", 
                ChatColor.GREEN, playerName, ChatColor.GRAY, roleName));
        }
    }
    
    /**
     * 獲取角色顯示名稱
     */
    private String getRoleDisplayName(NationRole role) {
        switch (role) {
            case FOUNDER: return "創建者";
            case LEADER: return "領袖";
            case OFFICER: return "軍官";
            case CITIZEN: return "公民";
            default: return role.name();
        }
    }
    
    /**
     * 向國家所有在線成員廣播消息
     */
    private void broadcastToNation(Nation nation, String message) {
        for (UUID memberId : nation.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
    
    // 宣戰處理
    private boolean handleWar(CommandSender sender, String[] args) {
        // 實現宣戰邏輯
        sender.sendMessage(ChatColor.RED + "戰爭系統尚未完全實現");
        return true;
    }
    
    // 和平處理  
    private boolean handlePeace(CommandSender sender, String[] args) {
        // 實現和平邏輯
        sender.sendMessage(ChatColor.GREEN + "和平系統尚未完全實現");
        return true;
    }
    
    // 結盟處理
    private boolean handleAlly(CommandSender sender, String[] args) {
        // 實現結盟邏輯
        sender.sendMessage(ChatColor.BLUE + "同盟系統尚未完全實現");
        return true;
    }
    
    // 升級處理
    private boolean handlePromote(CommandSender sender, String[] args) {
        // 實現升級邏輯
        sender.sendMessage(ChatColor.YELLOW + "角色管理系統尚未完全實現");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "info", "invite", "accept", "kick", 
                               "promote", "deposit", "withdraw", "list", "war", "peace", 
                               "ally", "help").stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "info":
                case "war":
                case "peace":
                case "ally":
                case "accept":
                    return nationsAPI.getAllNations().stream()
                        .map(Nation::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                        
                case "invite":
                case "kick":
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return Collections.emptyList();
    }
}