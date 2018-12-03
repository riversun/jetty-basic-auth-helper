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

### Example

```java
package myserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.riversun.jetty.basicauth.BasicAuth;
import org.riversun.jetty.basicauth.BasicAuthSecurityHandler;

public class StartWebServer {

    public void start() {

        final int PORT = 8080;

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

        servletContextHandler.addServlet(ExampleServlet.class, "/api");

        final HandlerList handlerList = new HandlerList();

        final ResourceHandler resourceHandler = new ResourceHandler();

        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");
        resourceHandler.setDirectoriesListed(false);

        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setCacheControl("no-store,no-cache,must-revalidate");

        handlerList.addHandler(resourceHandler);
        handlerList.addHandler(servletContextHandler);

        final Server jettyServer = new Server();
        jettyServer.setHandler(handlerList);

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);

        final HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfig);
        final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnFactory);
        httpConnector.setPort(PORT);
        jettyServer.setConnectors(new Connector[] { httpConnector });

        // Enabling basic authentication
        BasicAuthSecurityHandler bash = new BasicAuthSecurityHandler();
        bash.setBasicAuth(new BasicAuth.Builder().setRealm("private site")
                .addUserPath("user1", "pass1", "/*")
                .addUserPath("user2", "pass2", "/index.html,/api")
                .addUserPath("user3", "pass3", "/api")
                .build());

        servletContextHandler.setSecurityHandler(bash);

        try {
            jettyServer.start();
            System.out.println("Server started.");
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    public static class ExampleServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            final String CONTENT_TYPE = "text/plain; charset=UTF-8";
            resp.setContentType(CONTENT_TYPE);

            final PrintWriter out = resp.getWriter();
            out.println("OK");
            out.close();
        }

    }

    public static void main(String[] args) {
        new StartWebServer().start();
    }

}
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
