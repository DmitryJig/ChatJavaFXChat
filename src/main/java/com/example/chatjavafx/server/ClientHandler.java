package com.example.chatjavafx.server;

import com.example.chatjavafx.Command;
import com.example.chatjavafx.logger.ChatLogger;
import com.example.chatjavafx.logger.LoggerImpl;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private AuthService authService;
    private ChatLogger logger;
    private Logger log4jLogger;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService, ExecutorService executorService, Logger log4jLogger) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.log4jLogger = log4jLogger;

            executorService.execute(() -> {
                try {
                    autenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException e) {
            log4jLogger.warn("Ошибка создания подключения клиента {}", e.getMessage());
            throw new RuntimeException("Ошибка создания подключения клиента", e);
        }
    }


    private void readMessage() {
        try {
            while (true) {

                String msg = in.readUTF();
                log4jLogger.info("Получено сообщение: {}", msg);
//                System.out.println("Получено сообщение: " + msg);
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    final String[] params = command.parse(msg);

                    if (command == Command.END) {
                        break;
                    }
                    if (command == Command.PRIVATE_MESSAGE) {

                        server.sendMessageToClient(this, params[0], params[1]);
//                        logger.write(nick + " приватно для " + params[1] + " " + msg);
                        continue;
                    }
                    if (command == Command.CHANGE_NICK) {
                        String newNick = params[0];
                        if (authService.changeNick(nick, newNick)) {
                            server.unsubscribe(this);
                            this.nick = newNick;
                            server.subscribe(this);
                        }
                        continue;
                    }
                }
                log4jLogger.info("Получено сообщение от {} {}", nick, msg);
//                System.out.println("Получено сообщение от " + nick + " " + msg);
//                logger.write(nick + " " + msg);
                server.broadcast(nick + " " + msg);
            }
        } catch (IOException e) {
            log4jLogger.warn(() -> e.getMessage());
//            e.printStackTrace();
        }


    }

    private void autenticate() {
        try {
            while (true) {

                final String str = in.readUTF(); // /auth login1 pass1
                if (Command.isCommand(str)) {
                    Command command = Command.getCommand(str);
                    String[] params = command.parse(str);
                    if (command == Command.AUTH) {
                        log4jLogger.info("is Auth");
//                        System.out.println("is Auth");
                        final String login = params[0];
                        final String password = params[1];

                        String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick); // /authok nick1
                            logger = new LoggerImpl(login);
                            this.nick = nick;
                            server.broadcast("Пользователь " + nick + " вошел в чат");
                            server.subscribe(this);
                            sendMessage(logger.readLast100Lines());
                            break;
                        } else {
                            sendMessage(Command.ERROR, "Неверные логин и пароль");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    public void sendMessage(String message) {
        try {
            log4jLogger.info("Отправляю сообщение: {}", message);
//            System.out.println("Отправляю сообщение: " + message);
            out.writeUTF(message);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void closeConnection() {
        logger.close();
//        sendMessage(Command.END);
        authService.close();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public ChatLogger getLogger() {
        return logger;
    }
}
