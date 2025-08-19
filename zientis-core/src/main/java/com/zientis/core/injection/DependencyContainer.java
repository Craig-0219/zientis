package com.zientis.core.injection;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 依賴注入容器
 * 提供簡單的依賴注入功能
 */
public class DependencyContainer {
    
    private static DependencyContainer instance;
    private final ConcurrentHashMap<Class<?>, Object> singletons;
    private final ConcurrentHashMap<String, Object> namedInstances;
    private final Logger logger;
    
    private DependencyContainer() {
        this.singletons = new ConcurrentHashMap<>();
        this.namedInstances = new ConcurrentHashMap<>();
        this.logger = Logger.getLogger(DependencyContainer.class.getName());
    }
    
    /**
     * 獲取容器實例
     */
    public static DependencyContainer getInstance() {
        if (instance == null) {
            synchronized (DependencyContainer.class) {
                if (instance == null) {
                    instance = new DependencyContainer();
                }
            }
        }
        return instance;
    }
    
    /**
     * 註冊單例
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletons.put(clazz, instance);
        logger.info("單例已註冊: " + clazz.getSimpleName());
    }
    
    /**
     * 註冊命名實例
     */
    public void registerNamed(String name, Object instance) {
        namedInstances.put(name, instance);
        logger.info("命名實例已註冊: " + name);
    }
    
    /**
     * 獲取實例
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) singletons.get(clazz);
    }
    
    /**
     * 獲取命名實例
     */
    @SuppressWarnings("unchecked")
    public <T> T getNamedInstance(String name, Class<T> clazz) {
        Object instance = namedInstances.get(name);
        if (instance != null && clazz.isInstance(instance)) {
            return (T) instance;
        }
        return null;
    }
    
    /**
     * 執行依賴注入
     */
    public void inject(Object target) {
        Class<?> clazz = target.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Injectable.class)) {
                injectField(target, field);
            }
        }
    }
    
    /**
     * 注入單一欄位
     */
    private void injectField(Object target, Field field) {
        Injectable annotation = field.getAnnotation(Injectable.class);
        boolean accessible = field.isAccessible();
        
        try {
            field.setAccessible(true);
            
            Object instance = null;
            
            // 嘗試按名稱注入
            if (!annotation.value().isEmpty()) {
                instance = namedInstances.get(annotation.value());
            }
            
            // 嘗試按類型注入
            if (instance == null) {
                instance = singletons.get(field.getType());
            }
            
            if (instance != null) {
                field.set(target, instance);
                logger.fine("依賴注入成功: " + field.getName() + " in " + target.getClass().getSimpleName());
            } else if (annotation.required()) {
                logger.warning("必需依賴注入失敗: " + field.getName() + " in " + target.getClass().getSimpleName());
            }
            
        } catch (Exception e) {
            logger.severe("依賴注入失敗: " + field.getName() + " - " + e.getMessage());
        } finally {
            field.setAccessible(accessible);
        }
    }
    
    /**
     * 檢查是否有註冊的實例
     */
    public boolean hasInstance(Class<?> clazz) {
        return singletons.containsKey(clazz);
    }
    
    /**
     * 檢查是否有命名實例
     */
    public boolean hasNamedInstance(String name) {
        return namedInstances.containsKey(name);
    }
    
    /**
     * 移除實例
     */
    public void removeInstance(Class<?> clazz) {
        singletons.remove(clazz);
        logger.info("實例已移除: " + clazz.getSimpleName());
    }
    
    /**
     * 移除命名實例
     */
    public void removeNamedInstance(String name) {
        namedInstances.remove(name);
        logger.info("命名實例已移除: " + name);
    }
    
    /**
     * 清空容器
     */
    public void clear() {
        singletons.clear();
        namedInstances.clear();
        logger.info("依賴注入容器已清空");
    }
    
    /**
     * 獲取容器狀態
     */
    public String getStatus() {
        return String.format("依賴注入容器狀態 - 單例數量: %d, 命名實例數量: %d", 
            singletons.size(), namedInstances.size());
    }
}