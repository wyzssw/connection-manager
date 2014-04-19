package com.wap.sohu.recom.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sohu.smc.core.Service;
import com.sohu.smc.core.config.Environment;
import com.sohu.smc.core.server.builer.SpringProxyFactory;

/**
 * 类RecomService.java的实现描述：TODO 类实现描述 
 * @author hongfengwang 2012-7-20 下午03:17:25
 */
public class RecomServer extends Service<RecomConfiguration>  {
    public static void main(String[] args) throws Exception {
//        InputStream input = RecomServer.class.getClassLoader().getResourceAsStream("recomservice.yml");
        new RecomServer().run(args);
        
    }   

  
    @Override
    protected void initialize(RecomConfiguration configuration, Environment environment) throws Exception {
//        final Template template = configuration.buildTemplate();
//        environment.addHealthCheck(new TemplateHealthCheck(template));
        String location = "spring/applicationContext.xml";
        ApplicationContext recomContext =  new ClassPathXmlApplicationContext(
                                                                          new String[] { location });
        this.packages.add("com.wap.sohu.recom.action");
        this.setProxyFactory(new SpringProxyFactory(recomContext));
        
    }

}
