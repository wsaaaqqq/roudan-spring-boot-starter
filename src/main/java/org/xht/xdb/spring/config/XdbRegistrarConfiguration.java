package org.xht.xdb.spring.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class XdbRegistrarConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceProperties")
    @AutoConfigureBefore(name = {"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
    @Import(XdbSpringRegistrar.class)
    static class V2Config {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.boot.jdbc.autoconfigure.DataSourceProperties")
    @AutoConfigureBefore(name = {"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"})
    @Import(XdbSpringRegistrarV4.class)
    static class V4Config {
    }
}
