package com.zientis.economy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * JSON建構工具類
 * 用於建構標準化的JSON回應
 */
public class JsonBuilder {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectNode rootNode;
    
    private JsonBuilder() {
        this.rootNode = objectMapper.createObjectNode();
    }
    
    /**
     * 建立新的JsonBuilder實例
     */
    public static JsonBuilder create() {
        return new JsonBuilder();
    }
    
    /**
     * 添加字串值
     */
    public JsonBuilder put(String key, String value) {
        rootNode.put(key, value);
        return this;
    }
    
    /**
     * 添加布林值
     */
    public JsonBuilder put(String key, boolean value) {
        rootNode.put(key, value);
        return this;
    }
    
    /**
     * 添加數字值
     */
    public JsonBuilder put(String key, Number value) {
        if (value instanceof Integer) {
            rootNode.put(key, value.intValue());
        } else if (value instanceof Long) {
            rootNode.put(key, value.longValue());
        } else if (value instanceof Double) {
            rootNode.put(key, value.doubleValue());
        } else if (value instanceof Float) {
            rootNode.put(key, value.floatValue());
        } else {
            rootNode.put(key, value.toString());
        }
        return this;
    }
    
    /**
     * 添加物件值
     */
    public JsonBuilder put(String key, Object value) {
        if (value == null) {
            rootNode.putNull(key);
        } else if (value instanceof Map) {
            try {
                rootNode.set(key, objectMapper.valueToTree(value));
            } catch (Exception e) {
                rootNode.put(key, value.toString());
            }
        } else {
            rootNode.put(key, value.toString());
        }
        return this;
    }
    
    /**
     * 建構JSON字串
     */
    public String build() {
        try {
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return "{\"error\":\"Failed to build JSON: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 建構美化的JSON字串
     */
    public String buildPretty() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (Exception e) {
            return "{\"error\":\"Failed to build JSON: " + e.getMessage() + "\"}";
        }
    }
}