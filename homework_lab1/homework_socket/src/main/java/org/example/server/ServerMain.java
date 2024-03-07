package org.example.server;

public class ServerMain {
    private static final int numberOfTcpConnections = 3;
    public static void main(String[] args) {
        int tcpPort = parseTcpPort(args);
        int udpPort = parseUdpPort(args);
        Server server = new Server(tcpPort, numberOfTcpConnections, udpPort);
        server.start();
    }

    private static int parseTcpPort(String... args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Tcp port not provided");
        }
    }
    private static int parseUdpPort(String... args) {
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Udp port not provided");
        }
    }
}