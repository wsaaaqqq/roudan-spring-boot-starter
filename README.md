# roudan-spring-boot-starter

[![Maven Central](https://img.shields.io/badge/maven--central-0.0.4-blue)](https://central.sonatype.com/artifact/io.github.wsaaaqqq/roudan-springboot-starter)
[![Java](https://img.shields.io/badge/java-8%2B-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)

**roudan-spring-boot-starter** 是 [roudan (肉蛋)](https://github.com/wsaaaqqq/roudan-core) 的 Spring Boot 自动装配 Starter。

引入即用——自动读取 Spring Boot 的 `spring.datasource` 配置，完成 roudan 初始化、数据源与事务管理器注册，并接入 Spring 事务，无需任何手写配置代码。

---

## 特性

- **零配置接入** — 复用 `spring.datasource`，自动初始化 roudan，开箱即用
- **多数据源** — 支持单数据源与多数据源，第一个自动设为主/默认数据源
- **自动注册 Bean** — 为每个数据源注册 `DataSource` 与 `DataSourceTransactionManager`
- **Spring 事务集成** — 默认开启 `useSpringTransaction`，roudan 与 Spring 事务共用连接
- **javax / jakarta 双栈** — 同时兼容 Spring Boot 2（javax）与 Spring Boot 3/4（jakarta）
- **请求级数据源复位** — 内置 Servlet Filter，每次请求自动切回默认数据源
- **ORM 风格可选** — 通过 `RDConfig.setOrmType()` 切换 JPA / MyBatis-Flex / Jimmer 注解

---

## 快速入门

### 1. 添加依赖

除了本 Starter，还需引入 **数据库驱动** 和 **`spring-boot-starter-jdbc`**（Starter 依赖 spring-jdbc 完成数据源与事务管理器注册，该依赖在 Starter 中为 `provided`，需由使用方提供）。

以 Spring Boot 2 + MySQL 为例：

```xml
<!-- roudan starter -->
<dependency>
    <groupId>io.github.wsaaaqqq</groupId>
    <artifactId>roudan-springboot-starter</artifactId>
    <version>0.0.4</version>
</dependency>

<!-- Spring JDBC（提供数据源自动装配与事务管理器）-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<!-- 数据库驱动（按实际数据库选择）-->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

> - `roudan-core` 已由本 Starter 传递引入，无需单独添加。
> - 若项目已引入 `spring-boot-starter-data-jpa` 等包含 spring-jdbc 的 Starter，则无需再额外添加 `spring-boot-starter-jdbc`。
> - 若 classpath 中缺少 spring-jdbc，自动装配将不会生效。

### 2. 配置数据源

沿用标准的 Spring Boot 数据源配置即可：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test
    username: sa
    password:
    driver-class-name: org.h2.Driver
```

### 3. 直接使用

启动后 roudan 已初始化完成，任意 Bean 中直接调用：

```java
@Service
public class UserService {

    public void demo() {
        BaseDao<User> dao = RD.dao().baseDao(User.class);
        dao.save(new User().setId("1").setName("张三"));
        List<User> list = dao.listAll();
    }
}
```

> API 用法与 roudan-core 完全一致，详见 [roudan-core README](https://github.com/wsaaaqqq/roudan-core)。

---

## 多数据源

在 `spring.datasource` 下以命名子节点声明多个数据源，**第一个** 自动成为主/默认数据源：

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://localhost:3306/db1
      username: root
      password: 123456
    oracle:
      url: jdbc:oracle:thin:@localhost:1521:orcl
      username: scott
      password: tiger
```

使用时按名称切换：

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);
dao.listAll();                       // 默认数据源 master
dao.datasource("oracle").listAll();  // 临时切到 oracle
```

每个数据源都会注册对应的 `dataSource-<name>` 与 `transactionManager-<name>` Bean，主数据源标记为 `@Primary`。

---

## 配置项

roudan 的全局行为通过 `RDConfig` 配置，在应用启动后、首次使用前设置即可（例如放在配置类构造器或 `@PostConstruct` 中）：

| 配置 | 说明 | 默认值 |
|------|------|--------|
| `RDConfig.setOrmType()` | ORM 注解风格：`JPA` / `MYBATIS_FLEX` / `JIMMER` | `JPA` |
| `RDConfig.setSqlDir()` | SQL 文件根目录 | `""` |
| `RDConfig.setAutoCommit()` | 是否自动提交 | `true` |
| `RDConfig.setAutoClose()` | 是否自动关闭连接 | `true` |
| `RDConfig.setUseSpringTransaction()` | 是否接入 Spring 事务 | `true` |
| `RDConfig.setShowSql()` | 是否打印 SQL | `true` |
| `RDConfig.setShowSqlArgs()` | 是否打印参数 | `true` |
| `RDConfig.setShowSqlCaller()` | 是否打印调用位置 | `true` |

示例：

```java
@Configuration
public class RoudanConfig {
    public RoudanConfig() {
        RDConfig.setOrmType(OrmType.JPA);
        RDConfig.setUseSpringTransaction(true);
        RDConfig.setShowSql(true);
    }
}
```

> Starter 已自动接入 Spring 事务，通常无需额外配置即可开箱使用。

---

## Spring 事务

Starter 默认已接入 Spring 事务，roudan 会复用 Spring 管理的连接，直接使用 `@Transactional` 即可：

```java
@Transactional
public void doBusiness() {
    BaseDao<User> dao = RD.dao().baseDao(User.class);
    dao.save(new User().setId("1").setName("test"));

    RD.namedModify()
        .sql("update T_ORDER set status = :s")
        .args("s", "PAID")
        .execute();
}
```

---

## 兼容性

| 环境 | 支持情况 |
|------|----------|
| Java | 8+ |
| Spring Boot | 2.x（javax）、3.x / 4.x（jakarta）|
| 数据源 | 任意 `DataSource`（HikariCP、Druid 等）|

版本号与 [roudan-core](https://github.com/wsaaaqqq/roudan-core) 保持同步发布。

---

## 开源协议

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
