package org.example.client;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static final String HOST = "127.0.0.1";
    private final int tcpPort;
    private final int udpPort;
    private final String nickname;
    private Socket tcpSocket;
    private DatagramSocket udpSocket;



    public Client(int tcpPort, int udpPort, String nickname) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.nickname = nickname;
    }
    // TODO : NAPRAW TO GÓWNO PONIŻEJ BO BEZ SENSU ZE JEST OUTPUT WRZUCANY I INPUT DO TYCH FUNCKJI
    public void start() {
        try {
            tcpSocket = createConnection();
            udpSocket = createUdpSocket();
            BufferedReader input = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            PrintWriter output = new PrintWriter(tcpSocket.getOutputStream(), true);
//            executorService.submit(() -> handleSendingMessages(output));
//            executorService.submit(this::listenForUdpMessages);
            var tcpThread = new Thread(() -> handleSendingMessages(output));
            var udpThread = new Thread(this::listenForUdpMessages);
            tcpThread.start();
            udpThread.start();
            handleReceivingMessages(input);
            tcpSocket.close();
            System.exit(0);
            tcpThread.interrupt();
            udpThread.interrupt();
        } catch (IOException e) {
            System.err.println("Failed to connect to the server: " + e.getMessage());
        }
    }

    private DatagramSocket createUdpSocket() {
        try {
            return new DatagramSocket();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create UDP socket: " + e.getMessage());
        }
    }

    private void handleReceivingMessages(BufferedReader input) {
        try {
            String incomingMessage;
            while ((incomingMessage = input.readLine()) != null) {
                System.out.println(incomingMessage);
                if (incomingMessage.equals("Server is shutting down")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read message from the server: " + e.getMessage());
        }
    }

    private void listenForUdpMessages() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                udpSocket.receive(packet);
                System.out.println("Received UDP message: " + new String(packet.getData(), 0, packet.getLength()));
            } catch (IOException e) {
                System.err.println("Failed to receive UDP message: " + e.getMessage());
            }
        }
    }

    private void handleSendingMessages(PrintWriter output) {
        try {
            output.println(nickname);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equals("U")) {
                    sendUdpMessage();
                } else {
                    output.println(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read message from the user: " + e.getMessage());
        }
    }

    private void sendUdpMessage() {
        String message = "Na razie jakas przykladowa wiadomosc!";
        byte[] buffer = message.getBytes();
        try {
            udpSocket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), udpPort));
        } catch (IOException e) {
            System.err.println("Couldn't send udp message " + e.getMessage());
        }
    }

    private Socket createConnection() {
        try {
            System.out.println("Connecting to the server with port " + tcpPort + "...");
            Socket socket = new Socket(HOST, tcpPort);
            System.out.println("successfully connected to the server");
            System.out.println("Write 'exit' if you wish to disconnect from the server");
            return socket;
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to the server " + e.getMessage());
        }
    }
}
