package org.xht.xdb.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EmbeddedDatabaseType.class)
@AutoConfigureBefore(name = {
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
})
@Import({XdbRegistrarConfiguration.class, XdbServletFilterConfiguration.class})
@Slf4j
public class XdbAutoConfiguration {

}
