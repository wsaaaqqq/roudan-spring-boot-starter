package org.xht.xdb.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class XdbServletFilterConfiguration {

    @Bean
    @ConditionalOnClass(name = "javax.servlet.Filter")
    @ConditionalOnProperty(name = "xdb.filter.enabled", havingValue = "true", matchIfMissing = true)
    public Object javaxXdbFilter() throws Exception {
        log.info("{} - {}", "XdbFilter(javax)", "register");
        return buildFilterRegistration(new JavaxXdbFilter(), "javax.servlet.Filter");
    }

    @Bean
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    @ConditionalOnProperty(name = "xdb.filter.enabled", havingValue = "true", matchIfMissing = true)
    public Object jakartaXdbFilter() throws Exception {
        log.info("{} - {}", "XdbFilter(jakarta)", "register");
        return buildFilterRegistration(new JakartaXdbFilter(), "jakarta.servlet.Filter");
    }

    private Object buildFilterRegistration(Object filter, String filterClassName) throws Exception {
        Class<?> frbClass = Class.forName(
                "org.springframework.boot.web.servlet.FilterRegistrationBean");
        Object registration = frbClass.getDeclaredConstructor().newInstance();

        Class<?> filterClass = Class.forName(filterClassName);
        frbClass.getMethod("setFilter", filterClass).invoke(registration, filter);
        frbClass.getMethod("setName", String.class).invoke(registration, "XdbFilter");
        frbClass.getMethod("addUrlPatterns", String[].class)
                .invoke(registration, (Object) new String[]{"/*"});
        frbClass.getMethod("setOrder", int.class).invoke(registration, 1);

        return registration;
    }
}
