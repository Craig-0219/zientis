package com.zientis.core;

import com.zientis.core.config.ConfigService;
import com.zientis.core.database.DatabaseManager;
import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceManager;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Zientis核心功能測試
 */
class ZientisCoreTest {
    
    @Mock
    private Plugin mockPlugin;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockPlugin.getDataFolder()).thenReturn(new java.io.File("test-data"));
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
    }
    
    @Test
    void testDependencyContainer() {
        DependencyContainer container = DependencyContainer.getInstance();
        
        // 測試單例註冊
        String testService = "Test Service";
        container.registerSingleton(String.class, testService);
        
        // 測試獲取實例
        String retrieved = container.getInstance(String.class);
        assertEquals(testService, retrieved);
        
        // 測試命名實例
        container.registerNamed("testString", testService);
        String namedRetrieved = container.getNamedInstance("testString", String.class);
        assertEquals(testService, namedRetrieved);
        
        // 清理
        container.clear();
    }
    
    @Test
    void testServiceRegistryInitialization() {
        // 測試服務註冊器是否能正確初始化
        assertDoesNotThrow(() -> {
            com.zientis.core.service.ServiceRegistry.initialize(mockPlugin);
            com.zientis.core.service.ServiceRegistry registry = 
                com.zientis.core.service.ServiceRegistry.getInstance();
            assertNotNull(registry);
        });
    }
    
    @Test
    void testConfigServiceCreation() {
        // 測試配置服務是否能正確創建
        assertDoesNotThrow(() -> {
            ConfigService configService = new ConfigService(mockPlugin);
            assertNotNull(configService);
            assertEquals("ConfigService", configService.getName());
            assertEquals("1.0.0", configService.getVersion());
        });
    }
    
    @Test
    void testDatabaseConfigDefaults() {
        // 測試資料庫配置預設值
        com.zientis.core.database.DatabaseConfig config = new com.zientis.core.database.DatabaseConfig();
        
        assertEquals("localhost", config.getHost());
        assertEquals(3306, config.getPort());
        assertEquals("zientis", config.getDatabase());
        assertEquals("zientis", config.getUsername());
        assertEquals("", config.getPassword());
        assertEquals(5, config.getMinimumIdle());
        assertEquals(20, config.getMaximumPoolSize());
    }
}