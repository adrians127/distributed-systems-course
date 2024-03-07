package org.example.server;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientTCPHandler implements Runnable {
    private final Socket clientSocket;
    private final int id;
    private String nickname;
    private final Server server;
    private BufferedReader input;
    private BufferedWriter output;


    public ClientTCPHandler(Socket clientSocket, int id, Server server) {
        this.clientSocket = clientSocket;
        this.id = id;
        this.server = server;
    }

    @Override
    public void run() {
        initializeSocket();
        try {
            firstMessage();
            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                if (clientMessage.equals("exit")) {
                    System.out.println("Client " + id + " disconnected");
                    break;
                }
                System.out.println("Got message with id:" + id + " from '" + nickname + "': " + clientMessage);
                server.sendToAllClients(nickname + ": " + clientMessage, id);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                clientSocket.close();
                server.removeClient(this);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void initializeSocket() {
        try {
            input = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            output = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void firstMessage() {
        try {
            String clientMessage = input.readLine();
            if (clientMessage != null) {
                System.out.println("New user connected: " + clientMessage);
            }
            nickname = clientMessage;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendShutdownMessage() {
        try {
            output.write("Server is shutting down");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            if (!clientSocket.isClosed()) {
                output.write(message);
                output.newLine();
                output.flush();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getId() {
        return id;
    }
}