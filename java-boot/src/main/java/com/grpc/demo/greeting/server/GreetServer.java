package com.grpc.demo.greeting.server;

import com.grpc.demo.greeting.tls.TLSService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class GreetServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.tencent.kona.ssl.debug", "all");

        System.out.println("start GRPC");

        TLSService tlsService = new TLSService(false, true, null);
        Server server = NettyServerBuilder.forPort(50051)
            .addService(new GreetServiceImpl())
            .sslContext(tlsService.jdkSslContext)
            .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
        System.out.println("end GRPC");
    }
}
