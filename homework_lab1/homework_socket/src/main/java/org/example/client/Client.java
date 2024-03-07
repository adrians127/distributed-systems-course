package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    public static final String HOST = "127.0.0.1";
    private final int port;
    private final String nickname;


    public Client(int port, String nickname) {
        this.port = port;
        this.nickname = nickname;
    }

    public void start() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Socket socket = createConnection();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(nickname);

            executorService.submit(() -> handleSendingMessages(output));
            handleReceivingMessages(input);

            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Failed to connect to the server: " + e.getMessage());
        } finally {
            executorService.shutdownNow();
        }
    }

    private void handleReceivingMessages(BufferedReader input) {
        try {
            String incommingMessage;
            while ((incommingMessage = input.readLine()) != null) {
                System.out.println(incommingMessage);
                if (incommingMessage.equals("Server is shutting down")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read message from the server: " + e.getMessage());
        }
    }

    private void handleSendingMessages(PrintWriter output) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = reader.readLine()) != null) {
                output.println(message);
            }
        } catch (IOException e) {
            System.err.println("Failed to read message from the user: " + e.getMessage());
        }
    }

    private Socket createConnection() throws IOException {
        System.out.println("Connecting to the server with port " + port + "...");
        Socket socket = new Socket(HOST, port);
        System.out.println("successfully connected to the server");
        System.out.println("Write 'exit' if you wish to disconnect from the server");
        return socket;
    }
}
