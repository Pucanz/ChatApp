package org.codeforall.ChatApp.Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


        String hostname = "localhost";
        int portNumber = 8085;

        try {
            // Create a socket
            Socket clientSocket = new Socket(hostname, portNumber);

            // Create and start the thread
            Thread t1 = new Thread(new ClientRunnable(clientSocket));
            t1.start();

            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            // Scanner to client can write messages (input)
            Scanner scanner = new Scanner(System.in);


            while (true) {
                String clientMessage = scanner.nextLine();
                if (clientMessage.equalsIgnoreCase("quit")) {
                    System.out.println("Exit chat!");
                    clientSocket.close();
                    scanner.close();
                    break;
                } else {
                    clientOut.println(clientMessage);
                }


            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }
}
