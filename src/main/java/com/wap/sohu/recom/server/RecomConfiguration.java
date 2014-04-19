package com.wap.sohu.recom.server;

import com.sohu.smc.core.config.Configuration;


/**
 * 类RecomConfiguration.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-20 下午03:27:00
 */
public class RecomConfiguration extends Configuration {
    private String template;    
    private String defaultName = "Stranger";

    public String getTemplate() {
        return template;
    }

    public String getDefaultName() {
        return defaultName;
    }

//    public Template buildTemplate() {
//        return new Template(template, defaultName);
//    }
}
