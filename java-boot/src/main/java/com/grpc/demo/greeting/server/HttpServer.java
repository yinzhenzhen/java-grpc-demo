package com.grpc.demo.greeting.server;

import com.grpc.demo.greeting.tls.TLSService;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.tencent.kona.ssl.debug", "all");

        TLSService tlsService = new TLSService(false, true, null);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setSslContext(tlsService.jdkSslContext.context());

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.addCustomizer(new SecureRequestCustomizer());

        Server server = new Server();
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(config));
        httpsConnector.setPort(50051);
        server.addConnector(httpsConnector);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(HelloServlet.class, "/");
        server.setHandler(new HandlerList(context, new DefaultHandler()));

        server.start();
    }

    public static class HelloServlet extends HttpServlet {

        private static final long serialVersionUID = -4748362333014218314L;

        @Override
        public void doGet(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Hello!");
        }

        @Override
        public void doPost(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            doGet(request, response);
        }
    }
}
