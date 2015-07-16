package proiectsocket;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static ServerSocket serverSocket;
    private static final int PORT = 1234;
    private static Map<String, ClientHandler> users;

    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
            users = new HashMap();
        } catch (IOException ioEx) {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        do {
            Socket client = serverSocket.accept();
            Scanner input = new Scanner(client.getInputStream());
            PrintWriter output = new PrintWriter(client.getOutputStream(), true);
            String username = input.nextLine();

            while (users.containsKey(username)) {
                output.println("Username already exists!");
                username = input.nextLine();
            }
            output.println("Logged in successfully!");
            System.out.println("New user: " + username);

            ClientHandler handler = new ClientHandler(client, username);
            users.put(username, handler);
            handler.start();

        } while (true);
    }

    private static class ClientHandler extends Thread {

        private Socket client;
        private Scanner input;
        private PrintWriter output;
        private String username;

        public ClientHandler(Socket socket, String name) {
            client = socket;
            username = name;

            try {
                input = new Scanner(client.getInputStream());
                output = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException ioEx) {
                System.out.println(ioEx);
            }
        }

        @Override
        public void run() {
            String command;
            String name;
            String message;

            //commands
            command = input.nextLine();

            while (!command.equalsIgnoreCase("QUIT")) {
                System.out.println(username + " -> " + command);
                if (command.equalsIgnoreCase("LIST")) {
                    if (users.size() == 1) {
                        output.println("You are the one and only!");
                    } else {
                        output.println("Users:");
                        for (String user : users.keySet()) {
                            output.println(user);
                        }
                    }
                } else if (command.length() > 4 && command.substring(0, 4).equalsIgnoreCase("MSG ")) {
                    command = command.substring(4);
                    name = command.substring(0, command.indexOf(' '));
                    message = command.substring(command.indexOf(' ') + 1);
                    for (Map.Entry<String, ClientHandler> user : users.entrySet()) {
                        if (user.getKey().equals(name)) {
                            ClientHandler h = user.getValue();
                            h.sendToOne(username, message);
                        }
                    }
                } else if (command.length() > 6 && command.substring(0, 6).equalsIgnoreCase("BCAST ")) {
                    message = command.substring(6);
                    for (Map.Entry<String, ClientHandler> user : users.entrySet()) {
                        if (!user.getKey().equals(username)) {
                            ClientHandler h = user.getValue();
                            h.sendToAll(username, message);
                        }
                    }
                } else if (command.length() > 5 && command.substring(0, 5).equalsIgnoreCase("NICK ")) {
                    String newname = command.substring(5);
                    if (users.containsKey(newname)) {
                        output.println("Username already exists!");
                    } else {
                        users.put(newname, users.get(username));
                        users.remove(username);
                        username = newname;
                    }
                } else {
                    output.println("Unknown command! Type HELP for a list of commands!");
                }
                command = input.nextLine();
            }

            try {
                if (client != null) {
                    System.out.println("Closing down connection...");
                    users.remove(username);
                    client.close();
                    System.out.println(username + " disconnected");
                }
            } catch (IOException ioEx) {
                System.out.println("Unable to disconnect!");
            }
        }

        private void sendToOne(String name, String message) {
            output.println("!!" + name + "-->" + message);
        }

        private void sendToAll(String name, String message) {
            output.println(name + "-->" + message);
        }
    }

}
