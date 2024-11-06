package org.codeforall.ChatApp.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    int portNumber = 8085;
    List<ServerWorker> serverWorkers = new ArrayList<>();

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for a client connection");

            for (int i = 0; i < 10; i++) {
                Socket clientSocket = serverSocket.accept();
                ServerWorker serverWorker = new ServerWorker(clientSocket);
                Thread t1 = new Thread(serverWorker);
                serverWorkers.add(serverWorker);
                System.out.println("Client connected to the server");
                t1.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToAll(String message) {

        for (int i = 0; i < serverWorkers.size(); i++) {
            ServerWorker serverWorker = serverWorkers.get(i);
            serverWorker.send(message);
        }
    }

    public void kick(String executorName, String targetName) {
        boolean found = false;
        for (int i = 0; i < serverWorkers.size(); i++) {
            ServerWorker worker = serverWorkers.get(i);
            if (worker.clientName.equals(targetName)) {
                found = true;
                worker.send("You have been kicked from the chat by " + executorName);
                sendToAll("'" + targetName + "' has been kicked from the chat by '" + executorName + "'.");

                // If client exist, remove the client, close the socket and remove from serverWorkers list
                worker.isRunning = false;

                try {
                    worker.clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                serverWorkers.remove(i);
                System.out.println("'" + targetName + "' has been kicked from the chat by '" + executorName + "'.");
                break;
            }
        }
        // If client name not exists, print message for executor
        if (!found) {
            for (ServerWorker worker : serverWorkers) {
                if (worker.clientName.equals(executorName)) {
                    worker.send("Error: User '" + targetName + "' not found in the chat.");
                    break;
                }
            }
            System.out.println("Kick attempt by '" + executorName + "' failed: User '" + targetName + "' not found.");
        }
    }

    //Inner class to implements Runnable and create a thread
    class ServerWorker implements Runnable {

        Socket clientSocket;
        private String clientName = "";
        private boolean isRunning = true;

        public ServerWorker(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {

            try {
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("|| 😈 Welcome to our chat 😈 ||");
                out.println("Please enter your name: ");
                this.clientName = serverIn.readLine();
                sendToAll("'" + clientName + "'" + " has joined the chat! Welcome " + clientName + "! 😁");
                System.out.println("'" + clientName + "'" + " has joined the chat!");
                while (isRunning) {
                    String clientMessage = serverIn.readLine();
                    if (clientMessage == null || clientMessage.trim().isEmpty()) {
                        continue;
                    }
                    if (clientMessage.startsWith("/")) {
                        checkMessage(clientMessage);
                    } else {
                        sendToAll(clientName + ": " + clientMessage);
                        System.out.println(clientName + ": " + clientMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected to the server");
                //throw new RuntimeException(e);
            }
        }

        public void send(String message) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);

            } catch (
                    IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Method to commands
        public void checkMessage(String message) throws IOException {
            if (message.startsWith("/name ")) {
                String newName = message.substring(6).trim();
                sendToAll("'" + clientName + "'" + " was changed name to " + "'" + newName + "'");
                System.out.println("'" + clientName + "'" + " was changed name to " + "'" + newName + "'");
                clientName = newName;
            } else if (message.startsWith("/quit")) {
                quit(this);
            } else if (message.startsWith("/kick ")) {
                String clientToKick = message.substring(6).trim();
                Server.this.kick(clientName, clientToKick);
            }
        }

        // Method to client quit from the server
        public void quit(ServerWorker serverWorker) {
            isRunning = false;
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    sendToAll("'" + clientName + "'" + " was left the chat. 😞");
                    System.out.println("'" + clientName + "'" + " was disconnected.");
                    clientSocket.close();
                    serverWorkers.remove(serverWorker);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

