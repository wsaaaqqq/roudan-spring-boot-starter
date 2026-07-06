package org.xht.xdb.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class XdbSpringRegistrarV4 implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;
    private BeanDefinitionRegistry beanFactory;

    @Override
    public void registerBeanDefinitions(@Nullable AnnotationMetadata metadata,
                                        @NonNull BeanDefinitionRegistry registry
    ) {
        xdbConfig();

        this.beanFactory = registry;
        Map<String, DataSourceProperties> dataSourcePropertiesMap = getDataSourcePropertiesMap();
        XdbConfig.setUseSpringTransaction(true);
        AtomicBoolean needSetPrimary = new AtomicBoolean(true);
        dataSourcePropertiesMap.forEach((dataSourceName, dataSourceProperties) -> {
            boolean primary = needSetPrimary.getAndSet(false);
            DataSource dataSource = createAndBindDataSource(dataSourceProperties);
            registerXdb(dataSource, dataSourceName, primary);
            registerDataSource(dataSource, dataSourceName, primary);
            registerTransactionManager(dataSource, dataSourceName, primary);
        });
    }

    private void xdbConfig() {
        String ormType = environment.getProperty("xdb.ormType", String.class, "JPA");
        if ("JIMMER".equalsIgnoreCase(ormType)) {
            XdbConfig.setOrmType(OrmType.JIMMER);
        } else if ("MYBATIS_FLEX".equalsIgnoreCase(ormType)) {
            XdbConfig.setOrmType(OrmType.MYBATIS_FLEX);
        } else {
            XdbConfig.setOrmType(OrmType.JPA);
        }
        String sqlDir = environment.getProperty("xdb.sqlDir", String.class, "");
        XdbConfig.setSqlDir(sqlDir);
        Boolean autoCommit = environment.getProperty("xdb.autoCommit", Boolean.class, true);
        XdbConfig.setAutoCommit(autoCommit);
        Boolean autoClose = environment.getProperty("xdb.autoClose", Boolean.class, true);
        XdbConfig.setAutoClose(autoClose);
        Boolean useSpringTransaction = environment.getProperty("xdb.useSpringTransaction", Boolean.class, true);
        XdbConfig.setUseSpringTransaction(useSpringTransaction);
    }

    public Map<String, DataSourceProperties> getDataSourcePropertiesMap() {
        Binder binder = Binder.get(environment);
        DataSourceProperties singleDataSourceProps = binder
                .bind("spring.datasource", Bindable.of(DataSourceProperties.class))
                .orElse(null);
        if (singleDataSourceProps != null) {
            Map<String, DataSourceProperties> map = new LinkedHashMap<>();
            map.put("default", singleDataSourceProps);
            return map;
        } else {
            return binder
                    .bind("spring.datasource", Bindable.mapOf(String.class, DataSourceProperties.class))
                    .orElse(new LinkedHashMap<>());
        }
    }

    private void registerXdb(DataSource dataSource, String dataSourceName, boolean primary) {
        Xdb
                .init()
                .addDataSource(dataSource, dataSourceName);
        if (primary) {
            Xdb
                    .init()
                    .addDataSourceDefault(dataSource);
            Xdb.selectDataSourceByName(dataSourceName);
        }
    }

    private DataSource createAndBindDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    private void registerDataSource(DataSource dataSource, String dataSourceName, boolean primary) {
        Class<?> dataSourceClass = dataSource.getClass();
        BeanDefinitionBuilder beanDefinitionBuilderDS = BeanDefinitionBuilder.rootBeanDefinition(dataSourceClass);
        beanDefinitionBuilderDS.addConstructorArgValue(dataSource);
        beanDefinitionBuilderDS.setPrimary(primary);
        String beanName = "dataSource-" + dataSourceName;
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilderDS.getBeanDefinition());
    }

    private void registerTransactionManager(DataSource dataSource, String dataSourceName, boolean primary) {
        BeanDefinitionBuilder beanDefinitionBuilderTM =
                BeanDefinitionBuilder.rootBeanDefinition(DataSourceTransactionManager.class);
        beanDefinitionBuilderTM.addConstructorArgValue(dataSource);
        beanDefinitionBuilderTM.setPrimary(primary);
        String beanName = "transactionManager-" + dataSourceName;
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilderTM.getBeanDefinition());
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
