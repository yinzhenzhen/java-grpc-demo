package com.grpc.demo.greeting.client;

import com.grpc.demo.greeting.tls.TLSService;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    public static void main(String[] args) throws Exception {

//        System.setProperty("com.tencent.kona.ssl.debug", "all");

        run();
    }

    private static void run() throws Exception {

        TLSService tlsService = new TLSService(true, true, null);
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 50051) // gm
//        ManagedChannel channel = NettyChannelBuilder.forAddress("orderer0.example.com", 50051) // ecc
                .sslContext(tlsService.jdkSslContext)
                .negotiationType(NegotiationType.TLS)
                .build();
        System.out.println(">===================================>doUnaryCall");
        // 一元调用
        doUnaryCall(channel);
        System.out.println("<===================================<doUnaryCall");

        // 服务端流式 RPC
//        System.out.println(">===================================>doServerStreamingCall");
//        doServerStreamingCall(channel);
//        System.out.println("<===================================<doServerStreamingCall");
//
//        // 客户端流式 RPC
//        System.out.println(">===================================>doClientStreamingCall");
//        doClientStreamingCall(channel);
//        System.out.println("<===================================<doClientStreamingCall");
//
//        // 双向流式 RPC
//        System.out.println(">===================================>doBiDiStreamingCall");
//        doBiDiStreamingCall(channel);
//        System.out.println("<===================================<doBiDiStreamingCall");
//
//        System.out.println(">===================================>doUnaryCallWithDeadline");
//        doUnaryCallWithDeadline(channel);
//        System.out.println("<===================================<doUnaryCallWithDeadline");

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private static void doUnaryCall(ManagedChannel channel) throws Exception {
        // 创建服务的客户端 - 阻塞同步
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        // Unary
        Greeting greeting = Greeting.newBuilder()
            .setFirstName("zhen")
            .setLastName("yin")
            .build();

        // 设置请求
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        try {
            GreetResponse greetResponse = greetClient.greet(greetRequest);
            System.out.println("result: " + greetResponse.getResult());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    private static void doServerStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Server Streaming
        // we prepare the request
        GreetManyTimesRequest greetManyTimesRequest =
                GreetManyTimesRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName("Stephane"))
                        .build();

        // we stream the responses (in a blocking manner)
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });

    }

    private static void doClientStreamingCall(ManagedChannel channel) {
        // create an asynchronous client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                // onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // we get an error from the server
            }

            @Override
            public void onCompleted() {
                // the server is done sending us data
                // onCompleted will be called right after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        // streaming message #1
        System.out.println("sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Stephane")
                        .build())
                .build());

        // streaming message #2
        System.out.println("sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("John")
                        .build())
                .build());

        // streaming message #3
        System.out.println("sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Marc")
                        .build())
                .build());

        // we tell the server that the client is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Stephane", "John", "Marc", "Patricia").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name))
                            .build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call (3000 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS).greetWithDeadline(GreetWithDeadlineRequest.newBuilder().setGreeting(
                    Greeting.newBuilder().setFirstName("Stephane")
            ).build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }

        // second call (100 ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 100 ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest.newBuilder().setGreeting(
                    Greeting.newBuilder().setFirstName("Stephane")
            ).build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }

}
