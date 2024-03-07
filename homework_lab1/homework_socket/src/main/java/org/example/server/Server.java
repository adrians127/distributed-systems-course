package org.example.server;

import java.io.IOException;
import java.net.DatagramPacket;
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
    public static final int bufferForUdpMessage = 1024;
    private final int tcpPort;
    private final int udpPort;
    private ServerSocket serverSocket; // TCP
    private DatagramSocket datagramSocket; // UDP
    private final ExecutorService tcpConnections;
    private final Map<Integer, ClientTCPHandler> clients = new HashMap<>();
    private final Queue<Integer> availableIds = new LinkedList<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public Server(int tcpPort, int tcpConnections, int udpPort) {
        this.tcpPort = tcpPort;
        this.tcpConnections = Executors.newFixedThreadPool(tcpConnections);
        this.udpPort = udpPort;
        for (int i = 0; i < tcpConnections; i++) {
            availableIds.add(i);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void start() {
        new Thread(this::listenForExitCommand).start();
        serverSocket = createServerSocket();
        datagramSocket = createDatagramSocket();
        var udpThread = new Thread(this::handleUdpPackets);
        udpThread.start();
        System.out.println("TCP Server started on port " + tcpPort);
        System.out.println("UDP Server started on port " + udpPort);
        while (running.get()) {
            acceptClient();
        }
    }

    private DatagramSocket createDatagramSocket() {
        try {
            return new DatagramSocket(udpPort);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create datagram socket: " + e.getMessage());
        }
    }

    private void handleUdpPackets() {
        byte[] buffer = new byte[bufferForUdpMessage];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                datagramSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Got UDP message: " + message);
                sendTcpMessage(message, -1);
            } catch (IOException e) {
                System.err.println("Failed to receive UDP packet: " + e.getMessage());
            }
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
            return new ServerSocket(tcpPort);
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

    public synchronized void sendTcpMessage(String message, int fromId) {
        for (var entry : clients.entrySet()) {
            if (entry.getKey() != fromId) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(ClientTCPHandler client) {
        clients.remove(client.getId());
        availableIds.add(client.getId());
    }
}
