package com.wap.sohu.recom.urlmapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类UriMapping.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-23 
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UriMapping {
    
    /**
     * 模仿spring requestMapping只有一个标识uri的value
     * @return
     */
    String[] value() default {};

}
