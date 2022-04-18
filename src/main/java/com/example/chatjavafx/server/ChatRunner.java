package com.example.chatjavafx.server;


// лаунчер
public class ChatRunner {
    public static void main(String[] args) {
        final ChatServer server = new ChatServer();
        server.run();
    }
}
