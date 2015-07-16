package proiectsocket;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private static InetAddress host;
    private static final int PORT = 1234;
    private static Scanner networkInput;
    private static PrintWriter networkOutput;
    private static Socket socket = null;
    private static Thread serverResponse;

    public static void main(String[] args) {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException uhEx) {
            System.out.println("\nHost ID not found!\n");
            System.exit(1);
        }
        sendMessages();
    }

    private static void sendMessages() {

        try {
            socket = new Socket(host, PORT);

            networkInput = new Scanner(socket.getInputStream());
            networkOutput = new PrintWriter(socket.getOutputStream(), true);

            Scanner userEntry = new Scanner(System.in);

            String username;
            String message;
            String response;

            //log in
            do {
                System.out.print("Username: ");
                username = userEntry.nextLine();
                if (username.equals("")) {
                    response = "Invalid username";
                } else {
                    networkOutput.println(username);
                    response = networkInput.nextLine();
                }
                System.out.println(response);
            } while (!response.equals("Logged in successfully!"));
            System.out.println("Type HELP for a list of commands\n");

            serverResponse = new Thread(new ServerHandler());
            serverResponse.start();
            //commands
            do {
                message = userEntry.nextLine();
                if (message.equalsIgnoreCase("HELP")) {
                    showHELP();
                } else {
                    networkOutput.println(message);
                }
            } while (!message.equalsIgnoreCase("QUIT"));
        } catch (IOException ioEx) {
            System.out.println(ioEx);
        } finally {
            try {
                System.out.println("\nClosing connection...");
                socket.close();
                System.out.println("\nDisconected!");
                System.exit(0);
            } catch (IOException ioEx) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }

    private static void showHELP() {
        System.out.println("\nLIST                \t Vizualizare lista utilizatori");
        System.out.println("MSG username message\t Trimite mesaj privat unui utilizator");
        System.out.println("BCAST               \t Trimite mesaj tuturor utilizatorilor");
        System.out.println("NICK newusername    \t Schimbare username");
        System.out.println("QUIT                \t Deconectare\n");
    }

    static class ServerHandler extends Thread {

        @Override
        public void run() {
            String response;

            while (socket.isBound()) {
                try {
                    response = networkInput.nextLine();
                    System.out.println(response);
                } catch (Exception e) {
                }
            }
        }
    }
}
