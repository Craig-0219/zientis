package com.zientis.display.api;

import com.zientis.core.api.ZientisAPI;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayStatus;
import com.zientis.display.data.DisplayUpdateType;
import com.zientis.display.data.IslandDisplayTier;
import com.zientis.multiworld.api.ZientisMultiWorldAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZientisDisplayAPIImpl 測試類
 */
class ZientisDisplayAPIImplTest {

    @Mock
    private ZientisAPI mockCoreAPI;
    
    @Mock
    private ZientisMultiWorldAPI mockMultiWorldAPI;
    
    @Mock
    private World mockMainWorld;
    
    @Mock
    private World mockIslandWorld;
    
    @Mock
    private Location mockLocation;
    
    private ZientisDisplayAPIImpl displayAPI;
    private UUID testIslandId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        displayAPI = new ZientisDisplayAPIImpl();
        testIslandId = UUID.randomUUID();
        
        // 設置模擬行為
        when(mockMainWorld.getName()).thenReturn("world");
        when(mockIslandWorld.getName()).thenReturn("island_" + testIslandId);
        when(mockMultiWorldAPI.getOrLoadWorld(any(UUID.class))).thenReturn(mockIslandWorld);
        
        when(mockLocation.getWorld()).thenReturn(mockMainWorld);
        when(mockLocation.getX()).thenReturn(0.0);
        when(mockLocation.getY()).thenReturn(64.0);
        when(mockLocation.getZ()).thenReturn(0.0);
        when(mockLocation.clone()).thenReturn(mockLocation);
        
        // 設置 API 依賴
        displayAPI.setCoreAPI(mockCoreAPI);
        displayAPI.setMultiWorldAPI(mockMultiWorldAPI);
        displayAPI.setMainWorld(mockMainWorld);
    }

    @Test
    void testAPIInitialization() {
        // 測試 API 初始化
        assertNotNull(displayAPI);
        
        // 驗證統計信息可以正常獲取
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        assertNotNull(stats);
        assertEquals(0, stats.getTotalDisplays());
        assertEquals(0, stats.getActiveDisplays());
    }

    @Test
    void testGetSystemStats() {
        // 測試系統統計信息
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        
        assertNotNull(stats);
        assertTrue(stats.getTotalDisplays() >= 0);
        assertTrue(stats.getActiveDisplays() >= 0);
        assertTrue(stats.getMemoryUsage() >= 0);
        assertTrue(stats.getAverageUpdateTime() >= 0.0);
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testCreateIslandDisplay() throws Exception {
        // 測試創建島嶼展示
        CompletableFuture<DisplayModel> future = 
            displayAPI.createIslandDisplay(testIslandId, mockLocation);
        
        DisplayModel result = future.get();
        
        assertNotNull(result);
        assertEquals(testIslandId, result.getIslandId());
        assertTrue(result.getStatus() == DisplayStatus.ACTIVE || 
                   result.getStatus() == DisplayStatus.CREATING);
    }

    @Test
    void testGetDisplayModel() {
        // 測試獲取展示模型
        DisplayModel model = displayAPI.getDisplayModel(testIslandId);
        
        // 新API實例中應該沒有展示
        assertNull(model);
    }

    @Test
    void testGetAllDisplays() {
        // 測試獲取所有展示
        List<DisplayModel> displays = displayAPI.getAllDisplays();
        
        assertNotNull(displays);
        assertTrue(displays.isEmpty()); // 新API實例中應該沒有展示
    }

    @Test
    void testGetNearbyDisplays() {
        // 測試獲取附近展示
        List<DisplayModel> nearby = displayAPI.getNearbyDisplays(mockLocation, 50);
        
        assertNotNull(nearby);
        assertTrue(nearby.isEmpty()); // 新API實例中應該沒有展示
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testUpdateDisplayModel() throws Exception {
        // 測試更新展示模型
        UUID nonExistentId = UUID.randomUUID();
        
        CompletableFuture<DisplayModel> future = 
            displayAPI.updateDisplayModel(nonExistentId, DisplayUpdateType.FULL_REBUILD);
        
        DisplayModel result = future.get();
        
        // 不存在的展示應該返回null
        assertNull(result);
    }

    @Test
    @Disabled("Requires full Bukkit server environment")  
    void testRemoveDisplay() throws Exception {
        // 測試移除展示
        CompletableFuture<Boolean> future = displayAPI.removeDisplay(testIslandId);
        
        Boolean result = future.get();
        
        // 不存在的展示應該返回false
        assertFalse(result);
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testBatchUpdateDisplays() throws Exception {
        // 測試批量更新展示
        List<UUID> islandIds = List.of(testIslandId, UUID.randomUUID());
        
        CompletableFuture<Integer> future = 
            displayAPI.batchUpdateDisplays(islandIds, DisplayUpdateType.HOLOGRAM_ONLY);
        
        Integer successCount = future.get();
        
        assertNotNull(successCount);
        assertTrue(successCount >= 0);
        assertTrue(successCount <= islandIds.size());
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testReloadDisplay() throws Exception {
        // 測試重載展示
        CompletableFuture<Boolean> future = displayAPI.reloadDisplay(testIslandId);
        
        Boolean result = future.get();
        
        // 不存在的展示應該返回false
        assertFalse(result);
    }

    @Test
    void testSetAutoUpdateInterval() {
        // 測試設置自動更新間隔
        assertDoesNotThrow(() -> {
            displayAPI.setAutoUpdateInterval("test_region", 60);
        });
    }

    @Test
    void testDependencyInjection() {
        // 測試依賴注入
        ZientisDisplayAPIImpl newAPI = new ZientisDisplayAPIImpl();
        
        newAPI.setCoreAPI(mockCoreAPI);
        newAPI.setMultiWorldAPI(mockMultiWorldAPI);
        newAPI.setMainWorld(mockMainWorld);
        
        // 驗證依賴設置正確
        assertNotNull(newAPI.getSystemStats());
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testDiscordIntegration() throws Exception {
        // 測試Discord整合功能
        CompletableFuture<String> statsFuture = displayAPI.getDiscordDisplayStats();
        String stats = statsFuture.get();
        
        assertNotNull(stats);
        assertTrue(stats.contains("展示系統統計"));
    }

    @Test
    @Disabled("Requires full Bukkit server environment")
    void testDiscordDisplayCommand() throws Exception {
        // 測試Discord展示指令
        String[] args = {"status"};
        CompletableFuture<String> future = 
            displayAPI.handleDiscordDisplayCommand("display", args, "test_user_123");
        
        String result = future.get();
        
        assertNotNull(result);
        assertTrue(result.contains("✅") || result.contains("❌"));
    }

    @Test
    @Disabled("Requires full Bukkit server environment")  
    void testDiscordNotification() throws Exception {
        // 測試Discord通知
        CompletableFuture<Boolean> future = 
            displayAPI.sendDiscordDisplayNotification("display_created", testIslandId, "測試通知");
        
        Boolean result = future.get();
        
        assertNotNull(result);
        // 由於沒有實際的Discord整合，應該返回true（模擬成功）
        assertTrue(result);
    }

    @Test
    void testAsyncOperations() {
        // 測試異步操作
        CompletableFuture<DisplayModel> createFuture = 
            displayAPI.createIslandDisplay(testIslandId, mockLocation);
        
        assertNotNull(createFuture);
        assertFalse(createFuture.isCancelled());
        
        CompletableFuture<Boolean> removeFuture = displayAPI.removeDisplay(testIslandId);
        
        assertNotNull(removeFuture);
        assertFalse(removeFuture.isCancelled());
    }

    @Test
    void testStatisticsAccuracy() {
        // 測試統計信息準確性
        ZientisDisplayAPI.DisplaySystemStats initialStats = displayAPI.getSystemStats();
        
        assertEquals(0, initialStats.getTotalDisplays());
        assertEquals(0, initialStats.getActiveDisplays());
        assertEquals(0, initialStats.getMemoryUsage());
        assertEquals(0.0, initialStats.getAverageUpdateTime());
    }
}