package com.grpc.demo.greeting.client;

import com.grpc.demo.greeting.tls.TLSService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        System.setProperty("com.tencent.kona.ssl.debug", "all");

        TLSService tlsService = new TLSService(true, true, null);

        SSLConnectionSocketFactory socketFactory
            = new SSLConnectionSocketFactory(tlsService.jdkSslContext.context());
        CloseableHttpClient client = HttpClients.custom()
            .setSSLSocketFactory(socketFactory).build();

        // Access Servlet /hello over HTTPS scheme.
        HttpGet getMethod = new HttpGet("https://localhost:50051");
        CloseableHttpResponse response = client.execute(getMethod);
        client.close();

        System.out.println(response.toString());
        response.close();
    }
}
