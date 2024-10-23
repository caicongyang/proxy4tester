# Proxy4Tester - Dynamic Proxy System

[中文版](README.md) | English

Proxy4Tester is a dynamic proxy system based on Spring Cloud Gateway and Netty, allowing users to dynamically manage HTTP and TCP routing rules through database configuration, supporting request forwarding and direct response functionality.

## Features

- Dynamic route configuration: Manage HTTP and TCP routing rules through database
- HTTP proxy: HTTP request forwarding and direct response based on Spring Cloud Gateway
- TCP proxy: TCP connection forwarding and direct response based on Netty
- Request/Response modification: Support modifying HTTP request and response content
- RESTful API: Provide API interfaces for managing HTTP and TCP routes

## System Requirements

- Java 8+
- MySQL 5.7+
- Maven 3.6+

## Quick Start

### 1. Clone the project

git clone https://github.com/your-username/proxy4tester.git
cd proxy4tester

### 2. Configure the database

Modify the database configuration in src/main/resources/application.yml:

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/tester?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password

### 3. Build and Run

mvn clean package
java -jar target/proxy4tester-1.0-SNAPSHOT.jar

The application will start on http://localhost:8080.

## Usage Guide

### Managing HTTP Routes

Use the following API endpoints to manage HTTP routes:

- Get all routes: GET /api/http-routes
- Add new route: POST /api/http-routes
- Update route: PUT /api/http-routes/{id}
- Delete route: DELETE /api/http-routes/{id}
- Refresh routes: POST /api/http-routes/refresh
- Update route by routeId: PUT /api/http-routes/update/{routeId}

#### Adding a new HTTP route example

Send a POST request to /api/http-routes with the following body:

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://example.com",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

#### Configuring HTTP Direct Response

To configure a route that directly returns a response, set directResponse to true and provide responseBody, responseContentType, and responseStatusCode:

{
  "routeId": "direct_response_route",
  "path": "/direct/**",
  "uri": "no://op",
  "directResponse": true,
  "responseBody": "This is a direct response",
  "responseContentType": "text/plain",
  "responseStatusCode": 200
}

#### Modifying Requests and Responses

The system will by default add the following modifications for all non-direct response routes:

- Request modification: Add "X-Proxy-By" header, modify request body
- Response modification: Modify response body

You can customize these modifications in the DynamicRouteConfig class.

### Managing TCP Routes

Use the /api/tcp-routes endpoint to manage TCP routes.

#### Adding a new TCP route example

Send a POST request to /api/tcp-routes with the following body:

{
  "routeId": "tcp_example_route",
  "localPort": 8888,
  "remoteHost": "example.com",
  "remotePort": 80,
  "directResponse": false,
  "enabled": true
}

#### Configuring TCP Direct Response

To configure a TCP route that directly returns a response, set directResponse to true and provide a mockResponse:

{
  "routeId": "tcp_direct_response_route",
  "localPort": 8889,
  "directResponse": true,
  "mockResponse": "This is a direct TCP response",
  "enabled": true
}

### Using TCP Proxy

1. After configuring a TCP route, Proxy4Tester will listen for connections on the specified localPort.
2. Clients can connect to this local port, and Proxy4Tester will forward requests or return direct responses based on the route configuration.
3. For forwarded connections, Proxy4Tester acts as a middleman, forwarding data between the client and the remote server.

For example, if you've configured a TCP route to forward connections from local port 8888 to example.com:80, you can test it using telnet:

telnet localhost 8888

Then enter an HTTP request:

GET / HTTP/1.1
Host: example.com

Proxy4Tester will forward this request to example.com and return the response to you.

For direct response routes, you will immediately receive the configured mockResponse upon connecting to the specified port.

## Advanced Configuration

### Customizing TCP Proxy Behavior

You can customize the behavior of the TCP proxy by modifying the TcpProxyHandler class, for example, adding logging, data transformation, etc.

### Adjusting TCP Route Refresh Mechanism

The TcpProxyServer class provides a restart method, allowing the TCP proxy server to be reloaded when routes are updated. You can trigger a refresh by calling the /api/tcp-routes/refresh endpoint.

## Database Schema

The system uses the route_definitions table to store HTTP route definitions and the tcp_route_definition table to store TCP route definitions. The table structures are defined in the src/main/resources/doc/schema.sql file.

## Troubleshooting

- Check database connection and configuration
- View log files for detailed error information
- If TCP routes are not taking effect, try calling the /api/tcp-routes/refresh endpoint to refresh routes

## Contributing

Pull requests are welcome to improve this project. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Use Cases

### HTTP Proxy Case

Let's illustrate how to use the HTTP proxy functionality of Proxy4Tester through a specific example.

#### 1. Configure HTTP Proxy Route

First, we'll configure a route that will forward all requests starting with "/example" to "http://httpbin.org".

Send the following POST request to `http://localhost:8080/api/http-routes`:

{
  "routeId": "example_route",
  "path": "/example/**",
  "uri": "http://httpbin.org",
  "filters": "",
  "order": 1,
  "enabled": true,
  "directResponse": false
}

#### 2. Verify Route Configuration

Send a GET request to `http://localhost:8080/api/http-routes` to confirm that the route has been correctly added.

#### 3. Use the Configured Route

Now, you can access httpbin.org services through Proxy4Tester. For example:

- To access http://httpbin.org/get, send a request to:
  GET http://localhost:8080/example/get

- To send a POST request to http://httpbin.org/post, send a request to:
  POST http://localhost:8080/example/post

#### 4. Observe Results

When you send these requests, Proxy4Tester will:
1. Forward the request to httpbin.org
2. Add the "X-Proxy-By: proxy4tester" header to the request
3. Modify the request body (if any)
4. Modify the response body

You can observe these modifications in the response.

#### 5. Refresh Routes

If you modify the route configuration, remember to call the refresh endpoint to make the changes take effect:
POST http://localhost:8080/api/http-routes/refresh

This example demonstrates how to configure a simple forwarding route and use it through Proxy4Tester. You can build upon this to configure more complex routes and direct responses.

### TCP Proxy Case

Let's illustrate how to use the TCP proxy functionality of Proxy4Tester through a specific example.

#### 1. Configure TCP Proxy Route

First, we'll configure a TCP proxy route that will forward all connections to local port 8888 to port 80 of example.com.

Send the following POST request to http://localhost:8080/api/tcp-routes:

{
  "routeId": "example_tcp_route",
  "localPort": 8888,
  "remoteHost": "example.com",
  "remotePort": 80,
  "directResponse": false,
  "enabled": true
}

#### 2. Verify Route Configuration

Send a GET request to http://localhost:8080/api/tcp-routes to confirm that the route has been correctly added.

#### 3. Use the Configured TCP Proxy

Now, you can access example.com's service through Proxy4Tester. For example, using telnet:

telnet localhost 8888

Then enter an HTTP request:

GET / HTTP/1.1
Host: example.com

(Press Enter twice)

#### 4. Observe Results

Proxy4Tester will:
1. Receive your connection to local port 8888
2. Forward the connection to port 80 of example.com
3. Return the HTTP response from example.com to you

You should see the HTTP response returned by example.com.

#### 5. Configure Direct Response

Now, let's configure a TCP route with direct response.

Send the following POST request to http://localhost:8080/api/tcp-routes:

{
  "routeId": "direct_response_tcp_route",
  "localPort": 8889,
  "directResponse": true,
  "mockResponse": "Hello from Proxy4Tester!",
  "enabled": true
}

#### 6. Test Direct Response

Use telnet to connect to port 8889:

telnet localhost 8889

You should immediately receive the response:

Hello from Proxy4Tester!

#### 7. Refresh TCP Routes

If you modify the TCP route configuration, you can call the refresh endpoint to make the changes take effect:

POST http://localhost:8080/api/tcp-routes/refresh

This example demonstrates how to configure and use TCP proxy routes, including forwarding connections and direct responses. You can build upon this to configure more complex TCP proxy scenarios.

## Acknowledgements

The development of this project has been supported by the following technologies and tools, to which we express our sincere gratitude:

- **Cursor**: Thanks to Cursor for providing intelligent programming tools that greatly improved our development efficiency.

- **OpenAI**: Thanks to OpenAI for their groundbreaking work in the field of artificial intelligence, providing us with powerful AI support.

These tools and technologies have made significant contributions to AI-driven software development, enabling projects like Proxy4Tester to be developed faster and more efficiently. We deeply appreciate their contributions to the AI field.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
