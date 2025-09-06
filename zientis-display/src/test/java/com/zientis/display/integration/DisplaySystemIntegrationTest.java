package com.zientis.display.integration;

import com.zientis.display.api.ZientisDisplayAPI;
import com.zientis.display.api.ZientisDisplayAPIImpl;
import com.zientis.display.data.DisplayModel;
import com.zientis.display.data.DisplayStatus;
import com.zientis.display.data.DisplayUpdateType;
import com.zientis.display.data.IslandDisplayTier;
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
import static org.mockito.Mockito.*;

/**
 * 展示系統整合測試
 * 
 * 測試整個展示系統的端到端工作流程
 */
@Disabled("Integration tests require full Bukkit server environment")
class DisplaySystemIntegrationTest {

    @Mock
    private World mockMainWorld;
    
    @Mock
    private World mockIslandWorld;
    
    @Mock
    private Location mockDisplayLocation;
    
    private ZientisDisplayAPIImpl displayAPI;
    private UUID testIslandId1;
    private UUID testIslandId2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        displayAPI = new ZientisDisplayAPIImpl();
        testIslandId1 = UUID.randomUUID();
        testIslandId2 = UUID.randomUUID();
        
        // 設置模擬環境
        when(mockMainWorld.getName()).thenReturn("world");
        when(mockIslandWorld.getName()).thenReturn("island_" + testIslandId1);
        
        when(mockDisplayLocation.getWorld()).thenReturn(mockMainWorld);
        when(mockDisplayLocation.getX()).thenReturn(0.0);
        when(mockDisplayLocation.getY()).thenReturn(64.0);
        when(mockDisplayLocation.getZ()).thenReturn(0.0);
        when(mockDisplayLocation.clone()).thenReturn(mockDisplayLocation);
        
        displayAPI.setMainWorld(mockMainWorld);
    }

    @Test
    void testCompleteDisplayLifecycle() throws Exception {
        // 測試完整的展示生命周期：創建 -> 更新 -> 移除
        
        // 1. 創建展示
        CompletableFuture<DisplayModel> createFuture = 
            displayAPI.createIslandDisplay(testIslandId1, mockDisplayLocation);
        DisplayModel model = createFuture.get();
        
        assertNotNull(model);
        assertEquals(testIslandId1, model.getIslandId());
        assertTrue(model.getStatus() == DisplayStatus.ACTIVE || 
                   model.getStatus() == DisplayStatus.CREATING);
        
        // 2. 驗證展示存在於系統中
        DisplayModel retrievedModel = displayAPI.getDisplayModel(testIslandId1);
        assertNotNull(retrievedModel);
        assertEquals(testIslandId1, retrievedModel.getIslandId());
        
        // 3. 更新展示
        CompletableFuture<DisplayModel> updateFuture = 
            displayAPI.updateDisplayModel(testIslandId1, DisplayUpdateType.HOLOGRAM_ONLY);
        DisplayModel updatedModel = updateFuture.get();
        
        assertNotNull(updatedModel);
        assertEquals(testIslandId1, updatedModel.getIslandId());
        
        // 4. 移除展示
        CompletableFuture<Boolean> removeFuture = displayAPI.removeDisplay(testIslandId1);
        Boolean removeSuccess = removeFuture.get();
        
        assertTrue(removeSuccess);
        
        // 5. 驗證展示已被移除
        DisplayModel shouldBeNull = displayAPI.getDisplayModel(testIslandId1);
        assertNull(shouldBeNull);
    }

    @Test
    void testMultipleDisplaysManagement() throws Exception {
        // 測試多個展示的管理
        
        // 創建第一個展示
        Location location1 = mock(Location.class);
        when(location1.getWorld()).thenReturn(mockMainWorld);
        when(location1.getX()).thenReturn(50.0);
        when(location1.getY()).thenReturn(64.0);
        when(location1.getZ()).thenReturn(0.0);
        when(location1.clone()).thenReturn(location1);
        
        CompletableFuture<DisplayModel> create1Future = 
            displayAPI.createIslandDisplay(testIslandId1, location1);
        
        // 創建第二個展示
        Location location2 = mock(Location.class);
        when(location2.getWorld()).thenReturn(mockMainWorld);
        when(location2.getX()).thenReturn(-50.0);
        when(location2.getY()).thenReturn(64.0);
        when(location2.getZ()).thenReturn(0.0);
        when(location2.clone()).thenReturn(location2);
        
        CompletableFuture<DisplayModel> create2Future = 
            displayAPI.createIslandDisplay(testIslandId2, location2);
        
        // 等待兩個展示都創建完成
        DisplayModel model1 = create1Future.get();
        DisplayModel model2 = create2Future.get();
        
        assertNotNull(model1);
        assertNotNull(model2);
        assertNotEquals(model1.getIslandId(), model2.getIslandId());
        
        // 檢查系統中有兩個展示
        List<DisplayModel> allDisplays = displayAPI.getAllDisplays();
        assertEquals(2, allDisplays.size());
        
        // 檢查附近展示功能
        List<DisplayModel> nearbyDisplays = displayAPI.getNearbyDisplays(location1, 100);
        assertTrue(nearbyDisplays.size() >= 1); // 至少包含自己
        
        // 清理
        displayAPI.removeDisplay(testIslandId1).get();
        displayAPI.removeDisplay(testIslandId2).get();
    }

    @Test
    void testBatchOperations() throws Exception {
        // 測試批量操作
        
        // 創建多個展示
        List<UUID> islandIds = List.of(testIslandId1, testIslandId2);
        
        for (UUID id : islandIds) {
            Location loc = mock(Location.class);
            when(loc.getWorld()).thenReturn(mockMainWorld);
            when(loc.getX()).thenReturn(Math.random() * 100);
            when(loc.getY()).thenReturn(64.0);
            when(loc.getZ()).thenReturn(Math.random() * 100);
            when(loc.clone()).thenReturn(loc);
            
            displayAPI.createIslandDisplay(id, loc).get();
        }
        
        // 批量更新
        CompletableFuture<Integer> batchUpdateFuture = 
            displayAPI.batchUpdateDisplays(islandIds, DisplayUpdateType.PARTICLE_ONLY);
        Integer updateCount = batchUpdateFuture.get();
        
        assertEquals(2, updateCount.intValue());
        
        // 清理
        for (UUID id : islandIds) {
            displayAPI.removeDisplay(id).get();
        }
    }

    @Test 
    void testDisplayTierUpgrade() throws Exception {
        // 測試展示等級升級
        
        // 創建基礎等級展示
        CompletableFuture<DisplayModel> createFuture = 
            displayAPI.createIslandDisplay(testIslandId1, mockDisplayLocation);
        DisplayModel model = createFuture.get();
        
        assertNotNull(model);
        
        // 模擬等級升級
        CompletableFuture<DisplayModel> upgradeFuture = 
            displayAPI.updateDisplayModel(testIslandId1, DisplayUpdateType.TIER_UPGRADE);
        DisplayModel upgradedModel = upgradeFuture.get();
        
        assertNotNull(upgradedModel);
        assertEquals(testIslandId1, upgradedModel.getIslandId());
        
        // 清理
        displayAPI.removeDisplay(testIslandId1).get();
    }

    @Test
    void testSystemPerformanceMetrics() throws Exception {
        // 測試系統性能指標
        
        ZientisDisplayAPI.DisplaySystemStats initialStats = displayAPI.getSystemStats();
        assertEquals(0, initialStats.getTotalDisplays());
        
        // 創建一些展示
        int displayCount = 3;
        for (int i = 0; i < displayCount; i++) {
            UUID id = UUID.randomUUID();
            Location loc = mock(Location.class);
            when(loc.getWorld()).thenReturn(mockMainWorld);
            when(loc.getX()).thenReturn(i * 10.0);
            when(loc.getY()).thenReturn(64.0);
            when(loc.getZ()).thenReturn(0.0);
            when(loc.clone()).thenReturn(loc);
            
            displayAPI.createIslandDisplay(id, loc).get();
        }
        
        // 檢查更新後的統計
        ZientisDisplayAPI.DisplaySystemStats updatedStats = displayAPI.getSystemStats();
        assertEquals(displayCount, updatedStats.getTotalDisplays());
        assertTrue(updatedStats.getActiveDisplays() <= updatedStats.getTotalDisplays());
        
        // 清理所有展示
        List<DisplayModel> allDisplays = displayAPI.getAllDisplays();
        for (DisplayModel model : allDisplays) {
            displayAPI.removeDisplay(model.getIslandId()).get();
        }
        
        // 驗證清理後的統計
        ZientisDisplayAPI.DisplaySystemStats finalStats = displayAPI.getSystemStats();
        assertEquals(0, finalStats.getTotalDisplays());
    }

    @Test
    void testErrorHandlingAndRecovery() throws Exception {
        // 測試錯誤處理和恢復
        
        // 嘗試獲取不存在的展示
        DisplayModel nonExistentDisplay = displayAPI.getDisplayModel(UUID.randomUUID());
        assertNull(nonExistentDisplay);
        
        // 嘗試更新不存在的展示
        CompletableFuture<DisplayModel> updateFuture = 
            displayAPI.updateDisplayModel(UUID.randomUUID(), DisplayUpdateType.FULL_REBUILD);
        DisplayModel updateResult = updateFuture.get();
        assertNull(updateResult);
        
        // 嘗試移除不存在的展示
        CompletableFuture<Boolean> removeFuture = displayAPI.removeDisplay(UUID.randomUUID());
        Boolean removeResult = removeFuture.get();
        assertFalse(removeResult);
        
        // 系統應該保持穩定
        ZientisDisplayAPI.DisplaySystemStats stats = displayAPI.getSystemStats();
        assertNotNull(stats);
    }

    @Test
    void testConcurrentOperations() throws Exception {
        // 測試並發操作
        
        int concurrentCount = 5;
        CompletableFuture<DisplayModel>[] createFutures = new CompletableFuture[concurrentCount];
        UUID[] testIds = new UUID[concurrentCount];
        
        // 並發創建多個展示
        for (int i = 0; i < concurrentCount; i++) {
            testIds[i] = UUID.randomUUID();
            Location loc = mock(Location.class);
            when(loc.getWorld()).thenReturn(mockMainWorld);
            when(loc.getX()).thenReturn(i * 20.0);
            when(loc.getY()).thenReturn(64.0);
            when(loc.getZ()).thenReturn(0.0);
            when(loc.clone()).thenReturn(loc);
            
            createFutures[i] = displayAPI.createIslandDisplay(testIds[i], loc);
        }
        
        // 等待所有創建完成
        CompletableFuture.allOf(createFutures).get();
        
        // 驗證所有展示都被正確創建
        for (int i = 0; i < concurrentCount; i++) {
            DisplayModel model = createFutures[i].get();
            assertNotNull(model);
            assertEquals(testIds[i], model.getIslandId());
        }
        
        // 並發移除所有展示
        CompletableFuture<Boolean>[] removeFutures = new CompletableFuture[concurrentCount];
        for (int i = 0; i < concurrentCount; i++) {
            removeFutures[i] = displayAPI.removeDisplay(testIds[i]);
        }
        
        CompletableFuture.allOf(removeFutures).get();
        
        // 驗證所有展示都被移除
        for (CompletableFuture<Boolean> future : removeFutures) {
            assertTrue(future.get());
        }
        
        // 最終系統狀態應該是空的
        assertEquals(0, displayAPI.getAllDisplays().size());
    }
}