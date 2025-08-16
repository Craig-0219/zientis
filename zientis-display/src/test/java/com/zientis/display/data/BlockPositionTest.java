package com.zientis.display.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BlockPosition 測試類
 */
class BlockPositionTest {

    @Test
    void testBlockPositionCreation() {
        BlockPosition position = new BlockPosition(10, 20, 30);
        
        assertEquals(10, position.getX());
        assertEquals(20, position.getY());
        assertEquals(30, position.getZ());
    }

    @Test
    void testFromString() {
        String positionString = "5,10,15";
        BlockPosition position = BlockPosition.fromString(positionString);
        
        assertEquals(5, position.getX());
        assertEquals(10, position.getY());
        assertEquals(15, position.getZ());
    }

    @Test
    void testFromStringWithSpaces() {
        String positionString = " 5 , 10 , 15 ";
        BlockPosition position = BlockPosition.fromString(positionString);
        
        assertEquals(5, position.getX());
        assertEquals(10, position.getY());
        assertEquals(15, position.getZ());
    }

    @Test
    void testFromStringNegativeNumbers() {
        String positionString = "-5,10,-15";
        BlockPosition position = BlockPosition.fromString(positionString);
        
        assertEquals(-5, position.getX());
        assertEquals(10, position.getY());
        assertEquals(-15, position.getZ());
    }

    @Test
    void testFromStringInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            BlockPosition.fromString("1,2");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            BlockPosition.fromString("1,2,3,4");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            BlockPosition.fromString("a,b,c");
        });
    }

    @Test
    void testAdd() {
        BlockPosition original = new BlockPosition(10, 20, 30);
        BlockPosition result = original.add(5, -10, 15);
        
        assertEquals(15, result.getX());
        assertEquals(10, result.getY());
        assertEquals(45, result.getZ());
        
        // 確保原始對象未被修改
        assertEquals(10, original.getX());
        assertEquals(20, original.getY());
        assertEquals(30, original.getZ());
    }

    @Test
    void testSubtract() {
        BlockPosition original = new BlockPosition(10, 20, 30);
        BlockPosition result = original.subtract(5, 10, 15);
        
        assertEquals(5, result.getX());
        assertEquals(10, result.getY());
        assertEquals(15, result.getZ());
    }

    @Test
    void testDistance() {
        BlockPosition pos1 = new BlockPosition(0, 0, 0);
        BlockPosition pos2 = new BlockPosition(3, 4, 0);
        
        double distance = pos1.distance(pos2);
        assertEquals(5.0, distance, 0.001); // 3-4-5 直角三角形
    }

    @Test
    void testManhattanDistance() {
        BlockPosition pos1 = new BlockPosition(0, 0, 0);
        BlockPosition pos2 = new BlockPosition(3, 4, 5);
        
        int manhattanDistance = pos1.manhattanDistance(pos2);
        assertEquals(12, manhattanDistance); // |3| + |4| + |5| = 12
    }

    @Test
    void testManhattanDistanceNegative() {
        BlockPosition pos1 = new BlockPosition(5, 5, 5);
        BlockPosition pos2 = new BlockPosition(2, 8, 1);
        
        int manhattanDistance = pos1.manhattanDistance(pos2);
        assertEquals(10, manhattanDistance); // |5-2| + |5-8| + |5-1| = 3 + 3 + 4 = 10
    }

    @Test
    void testIsWithinRange() {
        BlockPosition center = new BlockPosition(10, 10, 10);
        BlockPosition nearby = new BlockPosition(12, 11, 9);
        BlockPosition far = new BlockPosition(15, 15, 15);
        
        assertTrue(center.isWithinRange(nearby, 3));
        assertFalse(center.isWithinRange(far, 3));
    }

    @Test
    void testIsWithinRangeBoundary() {
        BlockPosition center = new BlockPosition(0, 0, 0);
        BlockPosition boundary = new BlockPosition(2, 2, 2);
        
        assertTrue(center.isWithinRange(boundary, 2));
        assertFalse(center.isWithinRange(boundary, 1));
    }

    @Test
    void testToString() {
        BlockPosition position = new BlockPosition(10, -5, 20);
        assertEquals("10,-5,20", position.toString());
    }

    @Test
    void testEquals() {
        BlockPosition pos1 = new BlockPosition(10, 20, 30);
        BlockPosition pos2 = new BlockPosition(10, 20, 30);
        BlockPosition pos3 = new BlockPosition(10, 20, 31);
        
        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertNotEquals(pos1, null);
        assertNotEquals(pos1, "not a position");
    }

    @Test
    void testHashCode() {
        BlockPosition pos1 = new BlockPosition(10, 20, 30);
        BlockPosition pos2 = new BlockPosition(10, 20, 30);
        BlockPosition pos3 = new BlockPosition(10, 20, 31);
        
        assertEquals(pos1.hashCode(), pos2.hashCode());
        assertNotEquals(pos1.hashCode(), pos3.hashCode());
    }

    @Test
    void testSelfReflexivity() {
        BlockPosition position = new BlockPosition(5, 10, 15);
        assertEquals(position, position);
        assertEquals(0, position.distance(position), 0.001);
        assertEquals(0, position.manhattanDistance(position));
        assertTrue(position.isWithinRange(position, 0));
    }
}