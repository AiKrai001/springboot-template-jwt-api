# SpringBoot JWT API 快速开发模板

## 项目简介

这是一个基于 SpringBoot 3.x 和 Kotlin 的后端快速开发模板，集成了 JWT 认证、Swagger 文档、Jimmer ORM 等常用功能，帮助开发者快速构建 RESTful API 服务。

## 技术栈

- **核心框架**: SpringBoot 3.4.4
- **编程语言**: Kotlin 1.9.25
- **认证授权**: Sa-Token + JWT
- **ORM 框架**: Jimmer 0.9.73
- **API 文档**: SpringDoc + Knife4j
- **数据库**: PostgreSQL
- **缓存**: Redis
- **工具类**: Hutool
- **构建工具**: Gradle (Kotlin DSL)
- **Java 版本**: 21

## 主要功能

### 1. 认证与授权
- 基于 Sa-Token 的 JWT 认证
- 角色权限控制 (`@SaCheckRole`)
- 自动续签 Token
- 登录/登出接口

### 2. 全局处理
- 统一响应格式 (`RespBean`)
- 全局异常处理 (`GlobalException`)
- 请求日志记录 (`RequestLogFilter`)
- 跨域支持 (`CorsFilter`)
- 请求限流 (`FlowLimitingFilter`)

### 3. 开发工具
- Swagger/Knife4j API 文档
- 代码格式化 (Spotless)
- 分页查询支持
- 雪花 ID 生成器
- 日志配置 (Logback)

### 4. 数据库
- Jimmer ORM 支持
- 数据库结构校验
- SQL 日志打印

## 项目结构

```
src/
├── main/
│   ├── kotlin/com/app/
│   │   ├── config/            # 配置类
│   │   │   ├── exception/     # 异常处理
│   │   │   ├── satoken/       # Sa-Token 配置
│   │   │   └── swagger/       # Swagger 配置
│   │   ├── controller/        # 控制器
│   │   ├── data/              # 数据模型和DTO
│   │   ├── filter/            # 过滤器
│   │   ├── repository/        # 数据仓库
│   │   ├── service/           # 服务层
│   │   └── utils/             # 工具类
│   └── resources/             # 资源文件
│       ├── application.yml    # 主配置文件
│       ├── application-*.yml  # 环境配置
│       └── logback.xml        # 日志配置
└── test/                      # 测试代码
```

## 快速开始

### 1. 环境准备

- JDK 21+
- PostgreSQL 数据库
- Redis

### 2. 配置修改

修改 `application.yml` 中的数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-db-host:5432/your-db
    username: your-username
    password: your-password
```

### 3. 启动项目

```bash
./gradlew bootRun
```

### 4. 访问 API 文档

- Knife4j UI: `http://localhost:18080/doc.html`
- Swagger UI: `http://localhost:18080/swagger-ui.html`