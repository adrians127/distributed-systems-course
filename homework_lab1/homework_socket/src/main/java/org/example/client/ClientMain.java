package org.example.client;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        final int tcpPort = parseTcpPort(args);
        final int udpPort = parseUdpPort(args);
        final String nickname = parseNickname(args);
        Client client = new Client(tcpPort, udpPort, nickname);
        client.start();
    }

    private static int parseTcpPort(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Port not provided");
        }
    }

    private static int parseUdpPort(String[] args) {
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Port not provided");
        }
    }

    private static String parseNickname(String[] args) {
        try {
            return args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(("Nickname not provided"));
            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("\n");
            System.out.println("Enter your nickname: ");
            return scanner.next();
        }
    }
}
