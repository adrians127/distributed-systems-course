package org.example.server;

public class ServerMain {
    private static final int numberOfConnections = 3;
    public static void main(String[] args) {
        int port = parsePort(args);
        Server server = new Server(port, numberOfConnections);
        server.start();
    }

    private static int parsePort(String[] args) {
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Port not provided");
        }
    }
}