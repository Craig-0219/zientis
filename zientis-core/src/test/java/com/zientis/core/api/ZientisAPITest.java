package com.zientis.core.api;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ZientisAPI class
 */
class ZientisAPITest {
    
    @Mock
    private Plugin mockPlugin;
    
    private AutoCloseable mockitoCloseable;
    
    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        // Reset singleton instance before each test
        resetZientisAPI();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        resetZientisAPI();
    }
    
    @Test
    @DisplayName("API should not be initialized before calling initialize()")
    void testInitialState() {
        assertFalse(ZientisAPI.isInitialized());
        assertThrows(IllegalStateException.class, ZientisAPI::getInstance);
    }
    
    @Test
    @DisplayName("API should be initialized correctly")
    void testInitialization() {
        ZientisAPI.initialize(mockPlugin);
        
        assertTrue(ZientisAPI.isInitialized());
        ZientisAPI api = ZientisAPI.getInstance();
        assertNotNull(api);
        assertEquals(mockPlugin, api.getPlugin());
    }
    
    @Test
    @DisplayName("API should not allow double initialization")
    void testDoubleInitialization() {
        ZientisAPI.initialize(mockPlugin);
        ZientisAPI firstInstance = ZientisAPI.getInstance();
        
        // Second initialization should be ignored
        ZientisAPI.initialize(mockPlugin);
        ZientisAPI secondInstance = ZientisAPI.getInstance();
        
        assertSame(firstInstance, secondInstance);
    }
    
    @Test
    @DisplayName("getInstance should throw exception when not initialized")
    void testGetInstanceWithoutInitialization() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class, 
            ZientisAPI::getInstance
        );
        
        assertTrue(exception.getMessage().contains("not initialized"));
    }
    
    /**
     * Reset the singleton instance using reflection for testing
     */
    private void resetZientisAPI() {
        try {
            Field instanceField = ZientisAPI.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset ZientisAPI instance", e);
        }
    }
}