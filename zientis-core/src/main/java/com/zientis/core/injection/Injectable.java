package com.zientis.core.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 依賴注入標註
 * 標記需要自動注入的欄位
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Injectable {
    
    /**
     * 指定注入的服務名稱，預設使用類型名稱
     */
    String value() default "";
    
    /**
     * 是否為必需依賴，預設為true
     */
    boolean required() default true;
}