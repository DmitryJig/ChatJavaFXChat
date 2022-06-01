package com.example.chatjavafx.server;

import com.example.chatjavafx.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final Map<String, ClientHandler> clients;
    private final AuthService authService;

    public ChatServer() {
        authService = new DbAuthService();
        this.clients = new HashMap<>();
    }

    public void run() {
        ExecutorService serverExecutorService = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                System.out.println("Ожидаем подключения клиента");
                final Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this, authService, serverExecutorService);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authService.close();
            serverExecutorService.shutdownNow();
        }
    }

    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    public void broadcast(String message) {

        clients.values().forEach(client ->
        {
            client.sendMessage(message);
            client.getLogger().write(message);
        });
    }

    public synchronized void broadcastClientList() {
        StringBuilder nicks = new StringBuilder();
        for (ClientHandler value : clients.values()) {
            nicks.append(value.getNick()).append(" ");
        }
        broadcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broadcast(Command command, String stringNicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, stringNicks);
        }
    }

    public void sendMessageToClient(ClientHandler sender, String to, String message) {

        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("от " + sender.getNick() + ": " + message);
            receiver.getLogger().write("от " + sender.getNick() + ": " + message);
            sender.sendMessage("участнику " + to + ": " + message);
            sender.getLogger().write("участнику " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
        }
    }
}
