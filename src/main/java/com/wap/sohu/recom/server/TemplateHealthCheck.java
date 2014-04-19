//package com.wap.sohu.recom.server;
//
//
//import com.yammer.metrics.core.HealthCheck;
//
//public class TemplateHealthCheck extends HealthCheck {
//    private final Template template;
//
//    public TemplateHealthCheck(Template template) {
//        super("template");
//        this.template = template;
//    }
//
//    @Override
//    protected Result check() throws Exception {
//        template.render(Optional.of("woo"));
//        template.render(Optional.<String>absent());
//        return Result.healthy();
//    }
//}
