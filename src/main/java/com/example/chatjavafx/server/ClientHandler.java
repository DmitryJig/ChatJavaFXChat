package com.example.chatjavafx.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    autenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения клиента", e);
        }
    }


    private void readMessage() {
        while (true) {
            try {

                String msg = in.readUTF();
                if (nick != null && !msg.startsWith("/")){
                    msg = this.nick + ": " + msg;
                }
                if (msg.startsWith("/w")){
                    String[] arr = msg.split("\\s+");
                    String sendNick = arr[1];
                    String messageBody = Arrays.stream(arr).skip(2).collect(Collectors.joining());
                    server.sendMessageForOneClient(messageBody, sendNick);
                    continue;
                }
                if ("/end".equals(msg)) {
                    break;
                }
                System.out.println("Получено сообщение " + msg);
                server.broadcast(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void autenticate() {
        while (true) {
            try {
                String msg = in.readUTF(); // /auth login1 pass1
                if (msg.startsWith("/auth")) {
                    String[] s = msg.split("\\s+");// s[0] - command, s[1] - login, s[2] - pass
                    final String login = s[1];
                    final String password = s[2];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick); // /authok nick1
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        server.subscribe(this);
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void closeConnection() {
        sendMessage("/end");

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Отправляю сообщение: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
