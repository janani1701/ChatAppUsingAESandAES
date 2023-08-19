package com.app.chat;

import com.google.gson.Gson;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.util.Methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {

    public static void main(String[] args) {
        Undertow.builder()
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .addHttpListener(8080, "0.0.0.0")
                .setIoThreads(20)
                .setWorkerThreads(40)
                .setHandler(
                        new AccessLogHandler(
                                getRoutingHandler(),
                                new LogReceiver(ChatServer.class), "combined", ChatServer.class.getClassLoader()
                        )
                )
                .build();
    }

    public static RoutingHandler getRoutingHandler() {
        RoutingHandler routingHandler = new RoutingHandler();

        routingHandler.add(
                Methods.POST,
                "/api/register",
                exchange -> {
                    String queryBody = getQueryBody(exchange.getInputStream());
                    RegistrationRequest registrationRequest = new Gson().fromJson(queryBody, RegistrationRequest.class);
                    DBUtils.RegistrationResponse registrationResponse = register(registrationRequest);
                    if (registrationResponse.userExists) {
                        exchange.setStatusCode(400);
                        exchange.getResponseSender().send("Username already exists");
                        return;
                    }
                    exchange.setStatusCode(200);
                }
        );

        routingHandler.add(
                Methods.POST,
                "/api/login",
                exchange -> {
                    String queryBody = getQueryBody(exchange.getInputStream());
                    LoginRequest loginRequest = new Gson().fromJson(queryBody, LoginRequest.class);
                    boolean loginSuccess = login(loginRequest);
                    if (!loginSuccess) {
                        exchange.setStatusCode(400);
                        exchange.getResponseSender().send("Username/Password incorrect");
                        return;
                    }
                    exchange.setStatusCode(200);
                }
        );

        routingHandler.add(
                Methods.POST,
                "/api/message",
                exchange -> {
                    String queryBody = getQueryBody(exchange.getInputStream());
                    MessageRequest messageRequest = new Gson().fromJson(queryBody, MessageRequest.class);
                    sendMessage(messageRequest);
                    exchange.setStatusCode(200);
                }
        );

        return null;
    }

    public static List<MessageRequest> pollMessages(String senderName, String receiverName) {
        return MessageQueue.pollMessages(receiverName, senderName);
    }

    public static void sendMessage(MessageRequest messageRequest) {
        MessageQueue.offerMessage(messageRequest);
    }

    public static boolean login(LoginRequest loginRequest) {
        return DBUtils.loginUser(loginRequest);
    }

    public static DBUtils.RegistrationResponse register(RegistrationRequest registrationRequest) {
        return DBUtils.createUserIfNotExists(registrationRequest);
    }

    protected static String getQueryBody(InputStream is)  {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String response;
        try{
            response = br.lines().collect(Collectors.joining(System.lineSeparator()));
        }finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return response;
    }

    // Get RSA keys. Uses key size of 2048.
    private static Map<String, Object> getRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Object> keys = new HashMap<>();
        keys.put("private", privateKey);
        keys.put("public", publicKey);
        return keys;
    }

    public static String getPrivateKeyForUser(String userName) {
        return DBUtils.getPrivateKey(userName);
    }

    public static String getPublicKeyForUser(String userName) {
        return DBUtils.getPublicKey(userName);
    }

    public static void updatePassword(String userName, String encryptedPassword) {
        DBUtils.updatePassword(userName, encryptedPassword);
    }

    public static List<String> getUsers() {
        return DBUtils.getUsers();
    }
}
