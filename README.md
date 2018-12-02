# Overview
A library for BASIC authentication for jetty9

It is licensed under [MIT](https://opensource.org/licenses/MIT).

# Usage

## Enable BASIC authentication when you use ServletContextHandler

```java
        BasicAuthSecurityHandler bash = new BasicAuthSecurityHandler();
        bash.setBasicAuth(new BasicAuth.Builder().setRealm("private site")
                .addUserPath("user1", "pass1", "/private1/*")
                .addUserPath("user2", "pass2", "/private2/*")
                .build());
        servletContextHandler.setSecurityHandler(bash);
```

## Enable BASIC authentication when you use ResourceHandler

Use BasicAuthResourceHandler instead of ResourceHandler

```java
            final BasicAuthResourceHandler resourceHandler = new BasicAuthResourceHandler();

            resourceHandler.setBasicAuth(new BasicAuth.Builder().setRealm("private site")
                .addUserPath("user1", "pass1", "/private1/*")
                .addUserPath("user2", "pass2", "/private2/*")
                .build());
```
## Download/Install

**Maven**

```xml
<dependency>
	<groupId>org.riversun</groupId>
	<artifactId>jetty-basic-auth-helper</artifactId>
	<version>0.5.0</version>
</dependency>
```
