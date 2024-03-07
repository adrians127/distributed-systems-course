package org.example.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServerClient instanceType = parser(args);
        startNewProcess(instanceType, args);
    }

    private static ServerClient parser(String[] args) {
        if (args.length > 3) {
            throw new IllegalArgumentException("Invalid arguments!");
        }
        if (args[0].equals("server")) {
            return ServerClient.SERVER;
        } else if (args[0].equals("client")) {
            return ServerClient.CLIENT;
        } else {
            throw new IllegalArgumentException("Unknown argument: " + args[0]);
        }
    }

    private static void startNewProcess(ServerClient instanceType, String... args) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = instanceType == ServerClient.SERVER ? "org.example.server.ServerMain" : "org.example.client.ClientMain";
        List<String> command = new ArrayList<>(Arrays.asList(javaBin, "-cp", classpath, className));
        command.addAll(List.of(args));
        ProcessBuilder builder = new ProcessBuilder(command);

        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = builder.start();
        } catch (IOException e) {
            System.err.println("Starting process failed: " + e.getMessage());
        }
    }
}