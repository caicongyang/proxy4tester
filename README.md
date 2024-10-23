# Proxy4Tester - 动态代理系统

中文 | [English](README_EN.md)

Proxy4Tester 是一个基于 Spring Cloud Gateway 和 Netty 的动态代理系统，允许用户通过数据库配置来动态管理 HTTP 和 TCP 路由规则，支持请求转发和直接响应功能。

## 功能特点

- 动态路由配置：通过数据库管理 HTTP 和 TCP 路由规则
- HTTP 代理：基于 Spring Cloud Gateway 的 HTTP 请求转发和直接响应
- TCP 代理：基于 Netty 的 TCP 连接转发和直接响应
- 请求/响应修改：支持修改 HTTP 请求和响应内容
- RESTful API：提供管理 HTTP 和 TCP 路由的 API 接口

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

### 管理 HTTP 路由

使用以下 API 端点来管理 HTTP 路由：

- 获取所有路由：GET /api/http-routes
- 添加新路由：POST /api/http-routes
- 更新路由：PUT /api/http-routes/{id}
- 删除路由：DELETE /api/http-routes/{id}
- 刷新路由：POST /api/http-routes/refresh
- 通过 routeId 更新路由：PUT /api/http-routes/update/{routeId}

#### 添加新 HTTP 路由示例

发送 POST 请求到 /api/http-routes，请求体如下：

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://example.com",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

#### 配置 HTTP 直接响应

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

#### 修改请求和响应

系统默认会为所有非直接响应的路由添加以下修改：

- 请求修改：添加 "X-Proxy-By" 头，修改请求体
- 响应修改：修改响应体

你可以在 DynamicRouteConfig 类中自定义这些修改。

### 管理 TCP 路由

使用 /api/tcp-routes 端点管理 TCP 路由。

#### 添加新 TCP 路由示例

发送 POST 请求到 /api/tcp-routes，请求体如下：

{
  "routeId": "tcp_example_route",
  "localPort": 8888,
  "remoteHost": "example.com",
  "remotePort": 80,
  "directResponse": false,
  "enabled": true
}

#### 配置 TCP 直接响应

要配置一个直接返回响应的 TCP 路由，设置 directResponse 为 true，并提供 mockResponse：

{
  "routeId": "tcp_direct_response_route",
  "localPort": 8889,
  "directResponse": true,
  "mockResponse": "This is a direct TCP response",
  "enabled": true
}

### 使用 TCP 代理

1. 配置 TCP 路由后，Proxy4Tester 将在指定的 localPort 上监听连接。
2. 客户端可以连接到这个本地端口，Proxy4Tester 将根据路由配置转发请求或返回直接响应。
3. 对于转发的连接，Proxy4Tester 将充当中间人，在客户端和远程服务器之间转发数据。

例如，如果您配置了一个 TCP 路由，将本地 8888 端口的连接转发到 example.com:80，您可以使用 telnet 进行测试：

telnet localhost 8888

然后输入 HTTP 请求：

GET / HTTP/1.1
Host: example.com

Proxy4Tester 将转发这个请求到 example.com，并将响应返回给您。

对于直接响应的路由，连接到指定端口后，您将立即收到配置的 mockResponse。

## 高级配置

### 自定义 TCP 代理行为

您可以通过修改 TcpProxyHandler 类来自定义 TCP 代理的行为，例如添加日志记录、数据转换等功能。

### 调整 TCP 路由刷新机制

TcpProxyServer 类提供了重启方法，允许在路由更新时重新加载 TCP 代理服务器。您可以通过调用 /api/tcp-routes/refresh 端点来触发刷新。

## 数据库架构

系统使用 route_definitions 表来存储 HTTP 路由定义，使用 tcp_route_definition 表来存储 TCP 路由定义。表结构定义在 src/main/resources/doc/schema.sql 文件中。

## 故障排除

- 检查数据库连接和配置
- 查看日志文件以获取详细错误信息
- 如果 TCP 路由不生效，尝试调用 /api/tcp-routes/refresh 端点刷新路由

## 贡献

欢迎提交 Pull Requests 来改进这个项目。对于重大变更，请先开 issue 讨论您想要改变的内容。

## 许可证

本项目采用 MIT 许可证。详情请见 LICENSE 文件。

## 使用案例

### HTTP 代理案例

让我们通过一个具体的例子来说明如何使用 Proxy4Tester 的 HTTP 代理功能。

#### 1. 配置 HTTP 代理路由

首先，我们将配置一个路由，它将把所有 "/example" 开头的请求转发到 "http://httpbin.org"。

发送以下 POST 请求到 `http://localhost:8080/api/http-routes`：

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://httpbin.org",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

#### 2. 验证路由配置

发送 GET 请求到 `http://localhost:8080/api/http-routes` 来确认路由已经被正确添加。

#### 3. 使用配置的路由

现在，你可以通过 Proxy4Tester 来访问 httpbin.org 的服务。例如：

- 要访问 http://httpbin.org/get，发送请求到：
  GET http://localhost:8080/example/get

- 要发送 POST 请求到 http://httpbin.org/post，发送请求到：
  POST http://localhost:8080/example/post

#### 4. 观察结果

当你发送这些请求时，Proxy4Tester 会：
1. 将请求转发到 httpbin.org
2. 添加 "X-Proxy-By: proxy4tester" 头到请求中
3. 修改请求体（如果有的话）
4. 修改响应体

你可以观察到响应中包含了这些修改。

#### 5. 刷新路由

如果你修改了路由配置，记得调用刷新端点来使更改生效：
POST http://localhost:8080/api/http-routes/refresh

这个例子展示了如何配置一个简单的转发路由，并通过 Proxy4Tester 来使用它。你可以基于此来配置更复杂的路由和直接响应。

### TCP 代理案例

让我们通过一个具体的例子来说明如何使用 Proxy4Tester 的 TCP 代理功能。

#### 1. 配置 TCP 代理路由

首先，我们将配置一个 TCP 代理路由，它将把所有到本地 8888 端口的连接转发到 example.com 的 80 端口。

发送以下 POST 请求到 `http://localhost:8080/api/tcp-routes`：

{
  "routeId": "example_tcp_route",
  "localPort": 8888,
  "remoteHost": "example.com",
  "remotePort": 80,
  "directResponse": false,
  "enabled": true
}

#### 2. 验证路由配置

发送 GET 请求到 `http://localhost:8080/api/tcp-routes` 来确认路由已经被正确添加。

#### 3. 使用配置的 TCP 代理

现在，你可以通过 Proxy4Tester 来访问 example.com 的服务。例如，使用 telnet：

telnet localhost 8888

然后输入 HTTP 请求：

GET / HTTP/1.1
Host: example.com

（按两次回车）

#### 4. 观察结果

Proxy4Tester 会：
1. 接收你发送到本地 8888 端口的连接
2. 将连接转发到 example.com 的 80 端口
3. 将 example.com 返回的 HTTP 响应返回给你

你应该能看到 example.com 返回的 HTTP 响应。

#### 5. 配置直接响应

现在，让我们配置一个直接响应的 TCP 路由。

发送以下 POST 请求到 `http://localhost:8080/api/tcp-routes`：

{
  "routeId": "direct_response_tcp_route",
  "localPort": 8889,
  "directResponse": true,
  "mockResponse": "Hello from Proxy4Tester!",
  "enabled": true
}

#### 6. 测试直接响应

使用 telnet 连接到 8889 端口：

telnet localhost 8889

你应该立即收到响应：

Hello from Proxy4Tester!

#### 7. 刷新 TCP 路由

如果你修改了 TCP 路由配置，可以调用刷新端点来使更改生效：

POST http://localhost:8080/api/tcp-routes/refresh

这个例子展示了如何配置和使用 TCP 代理路由，包括转发连接和直接响应。你可以基于此来配置更复杂的 TCP 代理场景。

## 致谢

本项目的开发过程中得到了以下技术和工具的支持，在此表示衷心的感谢：

- **Cursor**：感谢 Cursor 提供的智能编程工具，极大地提高了我们的开发效率。

- **OpenAI**：感谢 OpenAI 在人工智能领域的开创性工作，为我们提供了强大的 AI 支持。

这些工具和技术为 AI 驱动的软件开发做出了巨大贡献，使得像 Proxy4Tester 这样的项目能够更快、更高效地开发完成。我们深深感谢他们为 AI 领域所做的贡献。

## 许可证

本项目采用 MIT 许可证。详情请见 LICENSE 文件。
