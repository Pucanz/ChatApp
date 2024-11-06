package org.codeforall.ChatApp.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientRunnable implements Runnable{

    // local variable type Socket for can use a socket on that thread
    private Socket clientSocket;

    public ClientRunnable(Socket socket){
        this.clientSocket = socket;
    }


    @Override
    public void run() {

        try {
            // BufferedReader to can read messages from server
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //Read and print message from the server when the connection with server was established
            String serverConfirmationMessage = clientIn.readLine();
            System.out.println(serverConfirmationMessage);

            //Loop to continue to receive messages while connection was close
            while (true) {

                String chatMessage = clientIn.readLine();
                if (chatMessage != null) {
                    System.out.print(chatMessage + "\n ");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
