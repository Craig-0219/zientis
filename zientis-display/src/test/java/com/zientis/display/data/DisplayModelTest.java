package com.zientis.display.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DisplayModel 測試類
 */
class DisplayModelTest {

    @Mock
    private World mockWorld;
    
    @Mock
    private BlockData mockBlockData;
    
    private DisplayModel displayModel;
    private UUID islandId;
    private UUID ownerId;
    private Location centerLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        islandId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        centerLocation = new Location(mockWorld, 100, 64, 200);
        
        displayModel = new DisplayModel(islandId, ownerId, centerLocation, IslandDisplayTier.ENHANCED);
        
        when(mockBlockData.getMaterial()).thenReturn(Material.STONE);
    }

    @Test
    void testDisplayModelCreation() {
        assertNotNull(displayModel);
        assertEquals(islandId, displayModel.getIslandId());
        assertEquals(ownerId, displayModel.getOwnerId());
        assertEquals(IslandDisplayTier.ENHANCED, displayModel.getDisplayTier());
        assertEquals(DisplayStatus.CREATING, displayModel.getStatus());
        assertEquals(0, displayModel.getBlockCount());
        assertNotNull(displayModel.getCreatedAt());
        assertNotNull(displayModel.getLastUpdated());
    }

    @Test
    void testAddMiniatureBlock() {
        BlockPosition position = new BlockPosition(1, 2, 3);
        
        displayModel.addMiniatureBlock(position, mockBlockData);
        
        assertEquals(1, displayModel.getBlockCount());
        assertTrue(displayModel.hasBlockAt(position));
        assertEquals(mockBlockData, displayModel.getBlockAt(position));
    }

    @Test
    void testRemoveMiniatureBlock() {
        BlockPosition position = new BlockPosition(1, 2, 3);
        displayModel.addMiniatureBlock(position, mockBlockData);
        
        displayModel.removeMiniatureBlock(position);
        
        assertEquals(0, displayModel.getBlockCount());
        assertFalse(displayModel.hasBlockAt(position));
        assertNull(displayModel.getBlockAt(position));
    }

    @Test
    void testClearMiniatureBlocks() {
        displayModel.addMiniatureBlock(new BlockPosition(1, 2, 3), mockBlockData);
        displayModel.addMiniatureBlock(new BlockPosition(4, 5, 6), mockBlockData);
        
        assertEquals(2, displayModel.getBlockCount());
        
        displayModel.clearMiniatureBlocks();
        
        assertEquals(0, displayModel.getBlockCount());
    }

    @Test
    void testGetAllBlockPositions() {
        BlockPosition pos1 = new BlockPosition(1, 2, 3);
        BlockPosition pos2 = new BlockPosition(4, 5, 6);
        
        displayModel.addMiniatureBlock(pos1, mockBlockData);
        displayModel.addMiniatureBlock(pos2, mockBlockData);
        
        var positions = displayModel.getAllBlockPositions();
        assertEquals(2, positions.size());
        assertTrue(positions.contains(pos1));
        assertTrue(positions.contains(pos2));
    }

    @Test
    void testUpdateHologram() {
        HologramData hologramData = new HologramData(centerLocation);
        hologramData.addLine("測試全息圖");
        
        displayModel.updateHologram(hologramData);
        
        assertEquals(hologramData, displayModel.getHologramData());
    }

    @Test
    void testIncrementViewAndClickCount() {
        assertEquals(0, displayModel.getViewCount());
        assertEquals(0, displayModel.getClickCount());
        
        displayModel.incrementViewCount();
        displayModel.incrementClickCount();
        
        assertEquals(1, displayModel.getViewCount());
        assertEquals(1, displayModel.getClickCount());
    }

    @Test
    void testStatusUpdate() {
        displayModel.setStatus(DisplayStatus.ACTIVE);
        assertEquals(DisplayStatus.ACTIVE, displayModel.getStatus());
    }

    @Test
    void testBoundingBoxCalculation() {
        // 添加一些方塊
        displayModel.addMiniatureBlock(new BlockPosition(-2, 0, -3), mockBlockData);
        displayModel.addMiniatureBlock(new BlockPosition(4, 5, 2), mockBlockData);
        displayModel.addMiniatureBlock(new BlockPosition(1, 2, 1), mockBlockData);
        
        BoundingBox boundingBox = displayModel.getBoundingBox();
        
        assertNotNull(boundingBox);
        // 驗證邊界框計算正確
        Location min = boundingBox.getMin();
        Location max = boundingBox.getMax();
        
        assertEquals(98, min.getX(), 0.001); // 100 + (-2)
        assertEquals(64, min.getY(), 0.001);  // 64 + 0
        assertEquals(197, min.getZ(), 0.001); // 200 + (-3)
        
        assertEquals(104, max.getX(), 0.001); // 100 + 4
        assertEquals(69, max.getY(), 0.001);  // 64 + 5
        assertEquals(202, max.getZ(), 0.001); // 200 + 2
    }

    @Test
    void testWithinRange() {
        Location nearLocation = new Location(mockWorld, 105, 64, 205);
        Location farLocation = new Location(mockWorld, 200, 64, 300);
        
        assertTrue(displayModel.isWithinRange(nearLocation, 10));
        assertFalse(displayModel.isWithinRange(farLocation, 10));
    }

    @Test
    void testGetMostCommonBlock() {
        BlockData stoneData = mock(BlockData.class);
        BlockData woodData = mock(BlockData.class);
        
        when(stoneData.getMaterial()).thenReturn(Material.STONE);
        when(woodData.getMaterial()).thenReturn(Material.OAK_PLANKS);
        
        // 添加更多石頭方塊
        displayModel.addMiniatureBlock(new BlockPosition(0, 0, 0), stoneData);
        displayModel.addMiniatureBlock(new BlockPosition(1, 0, 0), stoneData);
        displayModel.addMiniatureBlock(new BlockPosition(2, 0, 0), stoneData);
        displayModel.addMiniatureBlock(new BlockPosition(3, 0, 0), woodData);
        
        assertEquals(Material.STONE, displayModel.getMostCommonBlock());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID sameIslandId = displayModel.getIslandId();
        DisplayModel sameModel = new DisplayModel(sameIslandId, UUID.randomUUID(), centerLocation, IslandDisplayTier.BASIC);
        
        assertEquals(displayModel, sameModel);
        assertEquals(displayModel.hashCode(), sameModel.hashCode());
        
        DisplayModel differentModel = new DisplayModel(UUID.randomUUID(), ownerId, centerLocation, IslandDisplayTier.BASIC);
        assertNotEquals(displayModel, differentModel);
    }

    @Test
    void testToString() {
        String result = displayModel.toString();
        
        System.out.println("Actual toString result: " + result);
        
        assertNotNull(result);
        assertTrue(result.contains(islandId.toString()));
        assertTrue(result.contains(ownerId.toString()));
        assertTrue(result.contains("增強") || result.contains("ENHANCED"));
        assertTrue(result.contains("創建中") || result.contains("CREATING"));
    }
}