package com.zientis.multiworld.backup;

import com.zientis.core.data.Island;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages backup operations for island worlds
 * Provides automatic backup scheduling and manual backup functionality
 */
public class BackupManager {
    
    private final Plugin plugin;
    private final Logger logger;
    private final File backupDirectory;
    private final ScheduledExecutorService scheduler;
    
    // Configuration
    private static final int AUTO_BACKUP_INTERVAL_HOURS = 6;
    private static final int MAX_BACKUPS_PER_ISLAND = 5;
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public BackupManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.backupDirectory = new File(plugin.getDataFolder(), "backups");
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Create backup directory if it doesn't exist
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }
        
        // Start automatic backup scheduler
        startAutomaticBackupScheduler();
        
        logger.info("BackupManager initialized - Backup directory: " + backupDirectory.getAbsolutePath());
    }
    
    /**
     * Create a backup of an island world
     * @param island The island to backup
     * @return Future containing the backup result
     */
    public CompletableFuture<BackupResult> createBackup(Island island) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get world folder
                File worldFolder = new File(Bukkit.getWorldContainer(), island.getWorldName());
                if (!worldFolder.exists()) {
                    logger.warning("World folder does not exist for island " + island.getIslandId());
                    return new BackupResult(false, "World folder not found");
                }
                
                // Create island backup directory
                File islandBackupDir = new File(backupDirectory, island.getIslandId().toString());
                if (!islandBackupDir.exists()) {
                    islandBackupDir.mkdirs();
                }
                
                // Generate backup filename
                String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
                File backupFile = new File(islandBackupDir, "backup_" + timestamp + ".zip");
                
                // Create backup
                boolean success = createZipBackup(worldFolder, backupFile);
                
                if (success) {
                    // Clean up old backups
                    cleanupOldBackups(islandBackupDir);
                    
                    logger.info("Successfully created backup for island " + island.getIslandId() + 
                               " at " + backupFile.getName());
                    return new BackupResult(true, "Backup created successfully", backupFile);
                } else {
                    logger.severe("Failed to create backup for island " + island.getIslandId());
                    return new BackupResult(false, "Backup creation failed");
                }
                
            } catch (Exception e) {
                logger.severe("Error creating backup for island " + island.getIslandId() + ": " + e.getMessage());
                e.printStackTrace();
                return new BackupResult(false, "Backup error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Restore an island from backup
     * @param island The island to restore
     * @param backupFile The backup file to restore from
     * @return Future containing the restore result
     */
    public CompletableFuture<BackupResult> restoreBackup(Island island, File backupFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Ensure world is unloaded before restoration
                World world = Bukkit.getWorld(island.getWorldName());
                if (world != null) {
                    boolean unloaded = Bukkit.unloadWorld(world, true);
                    if (!unloaded) {
                        return new BackupResult(false, "Cannot unload world for restoration");
                    }
                }
                
                // Get world folder
                File worldFolder = new File(Bukkit.getWorldContainer(), island.getWorldName());
                
                // Delete existing world folder
                if (worldFolder.exists()) {
                    deleteWorldFolder(worldFolder);
                }
                
                // Restore from backup
                boolean success = extractZipBackup(backupFile, worldFolder);
                
                if (success) {
                    logger.info("Successfully restored island " + island.getIslandId() + 
                               " from backup " + backupFile.getName());
                    return new BackupResult(true, "Backup restored successfully");
                } else {
                    logger.severe("Failed to restore island " + island.getIslandId() + 
                                 " from backup " + backupFile.getName());
                    return new BackupResult(false, "Backup restoration failed");
                }
                
            } catch (Exception e) {
                logger.severe("Error restoring backup for island " + island.getIslandId() + ": " + e.getMessage());
                e.printStackTrace();
                return new BackupResult(false, "Restore error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get list of available backups for an island
     * @param islandId The island UUID
     * @return List of backup files
     */
    public List<File> getAvailableBackups(UUID islandId) {
        File islandBackupDir = new File(backupDirectory, islandId.toString());
        if (!islandBackupDir.exists()) {
            return Collections.emptyList();
        }
        
        File[] backupFiles = islandBackupDir.listFiles((dir, name) -> 
            name.startsWith("backup_") && name.endsWith(".zip"));
        
        if (backupFiles == null) {
            return Collections.emptyList();
        }
        
        List<File> backups = Arrays.asList(backupFiles);
        // Sort by last modified time (newest first)
        backups.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        return backups;
    }
    
    /**
     * Get backup statistics
     * @return Backup statistics
     */
    public BackupStats getBackupStats() {
        int totalBackups = 0;
        long totalSize = 0;
        int islandsWithBackups = 0;
        
        File[] islandDirs = backupDirectory.listFiles(File::isDirectory);
        if (islandDirs != null) {
            islandsWithBackups = islandDirs.length;
            
            for (File islandDir : islandDirs) {
                File[] backupFiles = islandDir.listFiles((dir, name) -> 
                    name.startsWith("backup_") && name.endsWith(".zip"));
                
                if (backupFiles != null) {
                    totalBackups += backupFiles.length;
                    for (File backup : backupFiles) {
                        totalSize += backup.length();
                    }
                }
            }
        }
        
        return new BackupStats(totalBackups, totalSize, islandsWithBackups);
    }
    
    private boolean createZipBackup(File worldFolder, File backupFile) {
        try {
            // Ensure parent directory exists
            backupFile.getParentFile().mkdirs();
            
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupFile.toPath()))) {
                Path worldPath = worldFolder.toPath();
                
                Files.walkFileTree(worldPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Skip certain files that shouldn't be backed up
                        String fileName = file.getFileName().toString();
                        if (fileName.equals("session.lock") || fileName.equals("uid.dat")) {
                            return FileVisitResult.CONTINUE;
                        }
                        
                        Path relativePath = worldPath.relativize(file);
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace('\\', '/'));
                        zipEntry.setTime(attrs.lastModifiedTime().toMillis());
                        
                        zos.putNextEntry(zipEntry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                        
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            
            return true;
        } catch (IOException e) {
            logger.severe("Error creating zip backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean extractZipBackup(File backupFile, File worldFolder) {
        try {
            // Create world folder
            worldFolder.mkdirs();
            
            try (var fileSystem = FileSystems.newFileSystem(backupFile.toPath(), (ClassLoader) null)) {
                Path backupRoot = fileSystem.getPath("/");
                Path worldPath = worldFolder.toPath();
                
                Files.walkFileTree(backupRoot, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = backupRoot.relativize(file);
                        Path targetPath = worldPath.resolve(relativePath.toString());
                        
                        // Create parent directories
                        Files.createDirectories(targetPath.getParent());
                        
                        // Copy file
                        Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            
            return true;
        } catch (IOException e) {
            logger.severe("Error extracting zip backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void cleanupOldBackups(File islandBackupDir) {
        File[] backupFiles = islandBackupDir.listFiles((dir, name) -> 
            name.startsWith("backup_") && name.endsWith(".zip"));
        
        if (backupFiles == null || backupFiles.length <= MAX_BACKUPS_PER_ISLAND) {
            return;
        }
        
        // Sort by last modified time (oldest first)
        Arrays.sort(backupFiles, Comparator.comparing(File::lastModified));
        
        // Delete oldest backups
        int toDelete = backupFiles.length - MAX_BACKUPS_PER_ISLAND;
        for (int i = 0; i < toDelete; i++) {
            if (backupFiles[i].delete()) {
                logger.info("Deleted old backup: " + backupFiles[i].getName());
            }
        }
    }
    
    private void deleteWorldFolder(File folder) throws IOException {
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private void startAutomaticBackupScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Starting automatic backup cycle...");
            // This would need integration with WorldManager to get active islands
            // For now, this is a placeholder for the automatic backup logic
        }, AUTO_BACKUP_INTERVAL_HOURS, AUTO_BACKUP_INTERVAL_HOURS, TimeUnit.HOURS);
    }
    
    public void shutdown() {
        logger.info("Shutting down BackupManager...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("BackupManager scheduler did not terminate gracefully");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Result of a backup operation
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final File backupFile;
        
        public BackupResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public BackupResult(boolean success, String message, File backupFile) {
            this.success = success;
            this.message = message;
            this.backupFile = backupFile;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public File getBackupFile() { return backupFile; }
    }
    
    /**
     * Backup statistics
     */
    public static class BackupStats {
        private final int totalBackups;
        private final long totalSize;
        private final int islandsWithBackups;
        
        public BackupStats(int totalBackups, long totalSize, int islandsWithBackups) {
            this.totalBackups = totalBackups;
            this.totalSize = totalSize;
            this.islandsWithBackups = islandsWithBackups;
        }
        
        public int getTotalBackups() { return totalBackups; }
        public long getTotalSize() { return totalSize; }
        public int getIslandsWithBackups() { return islandsWithBackups; }
        
        public String getFormattedSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
            if (totalSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
            return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}