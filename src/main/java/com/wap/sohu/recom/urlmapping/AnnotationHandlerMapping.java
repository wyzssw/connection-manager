package com.wap.sohu.recom.urlmapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * 简单的url映射注解handler，可以不在server层写很多映射代码
 * 模仿于DefaultAnnotationHandlerMapping
 * @author hongfengwang 2012-7-23
 */
public class AnnotationHandlerMapping extends ApplicationObjectSupport{
    
    private PathMatcher pathMatcher = new AntPathMatcher();
    
    private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();
    
    @SuppressWarnings("rawtypes")
    private final Map<Class, UriMapping> cachedMappings = new HashMap<Class, UriMapping>();
    
    
    
    
    @Override
    public void initApplicationContext() throws ApplicationContextException {
        super.initApplicationContext();
        detectHandlers();
    }
    /**
     * Register all handlers found in the current ApplicationContext.
     * <p>The actual URL determination for a handler is up to the concrete
     * {@link #determineUrlsForHandler(String)} implementation. A bean for
     * which no such URLs could be determined is simply not considered a handler.
     * @throws org.springframework.beans.BeansException if the handler couldn't be registered
     * @see #determineUrlsForHandler(String)
     */
    public void detectHandlers() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for URL mappings in application context: " + getApplicationContext());
        }
        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
        // Take any bean name that we can determine URLs for.
        for (String beanName : beanNames) {
            String[] urls = determineUrlsForHandler(beanName);
            if (!ObjectUtils.isEmpty(urls)) {
                // URL paths found: Let's consider it a handler.
                registerHandler(urls, beanName);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rejected bean name '" + beanName + "': no URL paths identified");
                }
            }
        }
    }


    /**
     * Checks for presence of the {@link org.springframework.web.bind.annotation.UriMapping}
     * annotation on the handler class and on any of its methods.
     */
    public String[] determineUrlsForHandler(String beanName) {
        ApplicationContext context = getApplicationContext();
        Class<?> handlerType = context.getType(beanName);
        UriMapping mapping = context.findAnnotationOnBean(beanName, UriMapping.class);
        if (mapping != null) {
            // @UriMapping found at type level
            this.cachedMappings.put(handlerType, mapping);
            Set<String> urls = new LinkedHashSet<String>();
            String[] typeLevelPatterns = mapping.value();
            if (typeLevelPatterns.length > 0) {
                // @UriMapping specifies paths at type level
                String[] methodLevelPatterns = determineUrlsForHandlerMethods(handlerType);
                for (String typeLevelPattern : typeLevelPatterns) {
                    if (!typeLevelPattern.startsWith("/")) {
                        typeLevelPattern = "/" + typeLevelPattern;
                    }
                    for (String methodLevelPattern : methodLevelPatterns) {
                        String combinedPattern = pathMatcher.combine(typeLevelPattern, methodLevelPattern);
                        addUrlsForPath(urls, combinedPattern);
                    }
                    addUrlsForPath(urls, typeLevelPattern);
                }
                return StringUtils.toStringArray(urls);
            }
            else {
                // actual paths specified by @UriMapping at method level
                return determineUrlsForHandlerMethods(handlerType);
            }
        }
        else if (AnnotationUtils.findAnnotation(handlerType, Controller.class) != null) {
            // @UriMapping to be introspected at method level
            return determineUrlsForHandlerMethods(handlerType);
        }
        else {
            return null;
        }
    }

    /**
     * Derive URL mappings from the handler's method-level mappings.
     * @param handlerType the handler type to introspect
     * @return the array of mapped URLs
     */
    public String[] determineUrlsForHandlerMethods(Class<?> handlerType) {
        final Set<String> urls = new LinkedHashSet<String>();
        Class<?>[] handlerTypes =
                Proxy.isProxyClass(handlerType) ? handlerType.getInterfaces() : new Class<?>[]{handlerType};
        for (Class<?> currentHandlerType : handlerTypes) {
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    UriMapping mapping = AnnotationUtils.findAnnotation(method, UriMapping.class);
                    if (mapping != null) {
                        String[] mappedPaths = mapping.value();
                        for (String mappedPath : mappedPaths) {
                            addUrlsForPath(urls, mappedPath);
                        }
                    }
                }
            });
        }
        return StringUtils.toStringArray(urls);
    }

    /**
     * Add URLs and/or URL patterns for the given path.
     * @param urls the Set of URLs for the current bean
     * @param path the currently introspected path
     */
    public void addUrlsForPath(Set<String> urls, String path) {
        urls.add(path);
       
    }

    protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {    
        Object resolvedHandler = handler;

        // Eagerly resolve handler if referencing singleton via name.
        if (handler instanceof String) {
            String handlerName = (String) handler;
            if (getApplicationContext().isSingleton(handlerName)) {
                resolvedHandler = getApplicationContext().getBean(handlerName);
            }
        }
        Object mappedHandler = this.handlerMap.get(urlPath);
        if (mappedHandler != null) {
            if (mappedHandler != resolvedHandler) {
                throw new IllegalStateException(
                        "Cannot map handler [" + handler + "] to URL path [" + urlPath +
                        "]: There is already handler [" + resolvedHandler + "] mapped.");
            }
        }
        else {
           
                this.handlerMap.put(urlPath, resolvedHandler);
                if (logger.isInfoEnabled()) {
                    logger.info("Mapped URL path [" + urlPath + "] onto handler [" + resolvedHandler + "]");
                }
        }
    }
  
    protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
        for (String urlPath : urlPaths) {
            registerHandler(urlPath, beanName);
        }
    }
    
    public final Map<String, Object> getHandlerMap() {
        return Collections.unmodifiableMap(this.handlerMap);
    }


}
