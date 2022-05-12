package com.example.chatjavafx.server;

import java.io.Closeable;
import java.io.IOException;

public interface AuthService extends Closeable {

    String getNickByLoginAndPassword(String login, String password);

    boolean changeNick(String oldNick, String newNick);

    void run() throws Exception;

    void close();
}
