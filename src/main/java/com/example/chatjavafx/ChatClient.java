package com.example.chatjavafx;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread timerThread; // сделал поток для отслеживания времени ввода логина и пароля

    private Controller controller;

    public ChatClient(Controller controller) {
        this.controller = controller;
        timerThread = new Thread(() -> {
            try {
                Thread.sleep(120000);
                System.out.println("Время для аутентификации вышло");
                Platform.exit();
                sendMessage(Command.END);
            } catch (InterruptedException e) {
                System.out.println("Авторизация прошла в срок");
            }
        });
        timerThread.start();
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        final Thread readThread = new Thread(() -> {
            try {
                waitAuth();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void closeConnection() {
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
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            final String msg = in.readUTF();
            System.out.println("Receive message: " + msg);
            if (Command.isCommand(msg)) {
                final Command command = Command.getCommand(msg);
                final String[] params = command.parse(msg);
                if (command == Command.END) {
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    controller.updateClientList(params);
                    continue;
                }
            }
            controller.addMessage(msg);
        }
    }


    private void waitAuth() throws IOException {
        while (true) {
            final String msgAuth = in.readUTF(); // /authok nick
            if (Command.isCommand(msgAuth)) {
                final Command command = Command.getCommand(msgAuth);
                final String[] params = command.parse(msgAuth);
                if (command == Command.AUTHOK) {
                    timerThread.interrupt();        // Прерываем таймер при успешной аутентификации
                    final String nick = params[0];
                    controller.setAuth(true);
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                }
            }
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
