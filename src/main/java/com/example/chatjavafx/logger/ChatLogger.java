package com.example.chatjavafx.logger;

import java.util.List;

public interface ChatLogger {

    void run();

    void write(String msg);

    String readLast100Lines();

    void close();
}
