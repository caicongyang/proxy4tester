# Proxy4Tester - 动态代理系统

Proxy4Tester 是一个基于 Spring Cloud Gateway 的动态代理系统，允许用户通过数据库配置来动态管理路由规则，支持请求转发和直接响应功能。

## 功能特点

- 动态路由配置：通过数据库管理路由规则
- 请求转发：将请求转发到指定的目标服务
- 直接响应：配置特定路由直接返回自定义响应
- 请求/响应修改：支持修改请求和响应内容
- RESTful API：提供管理路由的API接口

## 系统要求

- Java 8+
- MySQL 5.7+
- Maven 3.6+

## 快速开始

### 1. 克隆项目

git clone https://github.com/your-username/proxy4tester.git
cd proxy4tester

### 2. 配置数据库

修改 src/main/resources/application.yml 文件中的数据库配置：

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/tester?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password

### 3. 构建和运行

mvn clean package
java -jar target/proxy4tester-1.0-SNAPSHOT.jar

应用将在 http://localhost:8080 上启动。

## 使用指南

### 管理路由

使用以下 API 端点来管理路由：

- 获取所有路由：GET /routes
- 添加新路由：POST /routes
- 更新路由：PUT /routes/{id}
- 删除路由：DELETE /routes/{id}
- 刷新路由：POST /routes/refresh
- 通过 routeId 更新路由：PUT /routes/update/{routeId}

### 添加新路由示例

发送 POST 请求到 /routes，请求体如下：

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://example.com",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

### 配置直接响应

要配置一个直接返回响应的路由，设置 directResponse 为 true，并提供 responseBody、responseContentType 和 responseStatusCode：

{
  "routeId": "direct_response_route",
  "path": "/direct/**",
  "uri": "no://op",
  "directResponse": true,
  "responseBody": "This is a direct response",
  "responseContentType": "text/plain",
  "responseStatusCode": 200
}

### 修改请求和响应

系统默认会为所有非直接响应的路由添加以下修改：

- 请求修改：添加 "X-Proxy-By" 头，修改请求体
- 响应修改：修改响应体

你可以在 DynamicRouteConfig 类中自定义这些修改。

## 高级配置

### 自定义全局过滤器

编辑 CustomGlobalFilter 类来添加适用于所有路由的自定义逻辑。

### 调整路由刷新机制

RouteRefresher 类提供了刷新路由的功能。你可以通过调用 /routes/refresh 端点来触发刷新。

## 数据库架构

系统使用 route_definitions 表来存储路由定义。表结构定义在 src/main/resources/doc/schema.sql 文件中。

## 故障排除

- 如果遇到数据库连接问题，请检查 application.yml 中的数据库配置。
- 确保 MySQL 服务正在运行，并且指定的数据库存在。
- 检查日志文件以获取更详细的错误信息。
- 如果路由不生效，尝试调用 /routes/refresh 端点刷新路由。

## 贡献

欢迎提交 Pull Requests 来改进这个项目。对于重大变更，请先开 issue 讨论您想要改变的内容。

## 许可证

本项目采用 MIT 许可证。详情请见 LICENSE 文件。

## 使用案例

让我们通过一个具体的例子来说明如何使用 Proxy4Tester。

### 1. 配置示例路由

首先，我们将配置一个名为 "example_route" 的路由，它将把所有 "/example" 开头的请求转发到 "http://httpbin.org"。

发送以下 POST 请求到 `http://localhost:8080/routes`：

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://httpbin.org",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

### 2. 验证路由配置

发送 GET 请求到 `http://localhost:8080/routes` 来确认路由已经被正确添加。

### 3. 使用配置的路由

现在，你可以通过 Proxy4Tester 来访问 httpbin.org 的服务。例如：

- 要访问 http://httpbin.org/get，发送请求到：
  GET http://localhost:8080/example/get

- 要发送 POST 请求到 http://httpbin.org/post，发送请求到：
  POST http://localhost:8080/example/post

### 4. 观察结果

当你发送这些请求时，Proxy4Tester 会：
1. 将请求转发到 httpbin.org
2. 添加 "X-Proxy-By: proxy4tester" 头到请求中
3. 修改请求体（如果有的话）
4. 修改响应体

你可以观察到响应中包含了这些修改。

### 5. 刷新路由

如果你修改了路由配置，记得调用刷新端点来使更改生效：
POST http://localhost:8080/routes/refresh

这个例子展示了如何配置一个简单的转发路由，并通过 Proxy4Tester 来使用它。你可以基于此来配置更复杂的路由和直接响应。
