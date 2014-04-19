package com.wap.sohu.recom.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseJdbcSupport {

    @Autowired
    private JdbcTemplate jdbcTemplatePics;

    @Autowired
    private JdbcTemplate jdbcTemplateMpaperCms2;

    @Autowired
    private JdbcTemplate jdbcTemplateMpaperNewSlave;
    
    @Autowired
    private JdbcTemplate jdbcTemplateMpaperCms2Slave;

    @Autowired
    private JdbcTemplate jdbcTemplateSmcsubscribeSlave;
    
    @Autowired
    private JdbcTemplate jdbcTemplateSmcSociality;

    protected JdbcTemplate getJdbcTemplatePics() {
        return jdbcTemplatePics;
    }

    protected JdbcTemplate getJdbcTemplateMpaperCms2() {
        return jdbcTemplateMpaperCms2;
    }

    protected JdbcTemplate getJdbcTemplateMpaperNewSlave() {
        return jdbcTemplateMpaperNewSlave;
    }

    protected JdbcTemplate getJdbcTemplateSmcsubscribeSlave() {
        return jdbcTemplateSmcsubscribeSlave;
    }
    
    protected JdbcTemplate getJdbcTemplateSmcSociality(){
        return jdbcTemplateSmcSociality;
    }
    
    protected JdbcTemplate getJdbcTemplateMpaperCms2Slave(){
        return jdbcTemplateMpaperCms2Slave;
    }

}
