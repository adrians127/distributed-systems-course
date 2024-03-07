package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final int port;
    private ServerSocket serverSocket; // TCP
    private DatagramSocket datagramSocket; // UDP
    private final ExecutorService tcpConnections;
    private final Map<Integer, ClientTCPHandler> clients = new HashMap<>();
    private final Queue<Integer> availableIds = new LinkedList<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int maxClients;
    private int numberOfConnections = 0;

    public Server(int port, int tcpConnections) {
        this.port = port;
        this.tcpConnections = Executors.newFixedThreadPool(tcpConnections);
        this.maxClients = tcpConnections;
        for (int i = 0; i < maxClients; i++) {
            availableIds.add(i);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        new Thread(this::listenForExitCommand).start();
    }

    public void start() {
        serverSocket = createServerSocket();
        System.out.println("Server started on port " + port);
        while (running.get()) {
            acceptClient();
        }
    }

    private void acceptClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            if (availableIds.peek() == null) {
                clientSocket.getOutputStream().write("Server is full".getBytes());
                clientSocket.close();
                return;
            }
            int clientId = availableIds.poll();
            var client = new ClientTCPHandler(clientSocket, clientId, this);
            numberOfConnections++;
            clients.put(clientId, client);
            tcpConnections.submit(client);
        } catch (RejectedExecutionException e) {
            System.err.println("Task cannot be accepted for execution. Maybe the executor has been shut down or the task limit has been reached: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Failed to accept client connection: " + e.getMessage());
        }
    }

    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create server socket: " + e.getMessage());
        }
    }

    public void stop() {
        for (ClientTCPHandler client : clients.values()) {
            client.sendShutdownMessage();
        }
        tcpConnections.shutdown();
        try {
            if (!tcpConnections.awaitTermination(5, TimeUnit.SECONDS)) {
                tcpConnections.shutdownNow();
            }
        } catch (InterruptedException e) {
            tcpConnections.shutdownNow();
        }
        running.set(false);
        running.set(false);
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close server socket: " + e.getMessage());
        }
    }

    // TODO: doesn't work
    private void listenForExitCommand() {
        Scanner scanner = new Scanner(System.in);
        while (running.get()) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    stop();
                }
            }
        }
    }

    public synchronized void sendToAllClients(String message, int fromId) {
        for (var entry : clients.entrySet()) {
            if (entry.getKey() != fromId) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    public synchronized void removeClient(ClientTCPHandler client) {
        clients.remove(client.getId());
        availableIds.add(client.getId());
        numberOfConnections--;
    }
}
