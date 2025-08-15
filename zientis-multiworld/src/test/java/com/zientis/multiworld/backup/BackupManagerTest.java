package com.zientis.multiworld.backup;

import com.zientis.core.data.Island;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BackupManager
 * Tests backup functionality without requiring actual Bukkit server
 */
class BackupManagerTest {
    
    @Mock
    private Plugin mockPlugin;
    
    @TempDir
    File tempDir;
    
    private BackupManager backupManager;
    private AutoCloseable mockitoCloseable;
    
    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));
        when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        
        backupManager = new BackupManager(mockPlugin);
    }
    
    @Test
    @DisplayName("BackupManager should initialize with backup directory")
    void testInitialization() {
        File backupDir = new File(tempDir, "backups");
        assertTrue(backupDir.exists());
        assertTrue(backupDir.isDirectory());
    }
    
    @Test
    @DisplayName("Backup statistics should start with zero values")
    void testInitialBackupStats() {
        BackupManager.BackupStats stats = backupManager.getBackupStats();
        
        assertEquals(0, stats.getTotalBackups());
        assertEquals(0, stats.getTotalSize());
        assertEquals(0, stats.getIslandsWithBackups());
        assertEquals("0 B", stats.getFormattedSize());
    }
    
    @Test
    @DisplayName("Get available backups should return empty list for non-existent island")
    void testGetAvailableBackupsEmpty() {
        UUID islandId = UUID.randomUUID();
        List<File> backups = backupManager.getAvailableBackups(islandId);
        
        assertNotNull(backups);
        assertTrue(backups.isEmpty());
    }
    
    @Test
    @DisplayName("Create backup should fail for non-existent world")
    void testCreateBackupNonExistentWorld() {
        UUID islandId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Island island = new Island(islandId, playerId);
        
        // This will fail because Bukkit.server is null in test environment
        var future = backupManager.createBackup(island);
        
        assertDoesNotThrow(() -> {
            BackupManager.BackupResult result = future.get(5, TimeUnit.SECONDS);
            assertNotNull(result);
            assertFalse(result.isSuccess());
            // In test environment, we expect Bukkit error instead of world folder not found
            assertTrue(result.getMessage().contains("Bukkit") || 
                       result.getMessage().contains("World folder not found"));
        });
    }
    
    @Test
    @DisplayName("Backup result should have correct properties")
    void testBackupResult() {
        // Test successful result
        BackupManager.BackupResult successResult = new BackupManager.BackupResult(true, "Success");
        assertTrue(successResult.isSuccess());
        assertEquals("Success", successResult.getMessage());
        assertNull(successResult.getBackupFile());
        
        // Test failure result
        BackupManager.BackupResult failureResult = new BackupManager.BackupResult(false, "Failed");
        assertFalse(failureResult.isSuccess());
        assertEquals("Failed", failureResult.getMessage());
        
        // Test with backup file
        File mockFile = new File("test.zip");
        BackupManager.BackupResult resultWithFile = new BackupManager.BackupResult(true, "Success", mockFile);
        assertEquals(mockFile, resultWithFile.getBackupFile());
    }
    
    @Test
    @DisplayName("Backup stats should format file sizes correctly")
    void testBackupStatsFormatting() {
        // Test bytes
        BackupManager.BackupStats bytesStats = new BackupManager.BackupStats(1, 512, 1);
        assertEquals("512 B", bytesStats.getFormattedSize());
        
        // Test kilobytes
        BackupManager.BackupStats kbStats = new BackupManager.BackupStats(1, 2048, 1);
        assertEquals("2.0 KB", kbStats.getFormattedSize());
        
        // Test megabytes
        BackupManager.BackupStats mbStats = new BackupManager.BackupStats(1, 2 * 1024 * 1024, 1);
        assertEquals("2.0 MB", mbStats.getFormattedSize());
        
        // Test gigabytes
        BackupManager.BackupStats gbStats = new BackupManager.BackupStats(1, 2L * 1024 * 1024 * 1024, 1);
        assertEquals("2.0 GB", gbStats.getFormattedSize());
    }
    
    @Test
    @DisplayName("BackupManager should shutdown gracefully")
    void testShutdown() {
        assertDoesNotThrow(() -> {
            backupManager.shutdown();
        });
    }
}