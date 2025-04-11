package org.geooo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.util.Logger;

public class Server {

    public static final int SERVER_PORT = 7000;
    public ArrayList<Client> clients;

    public static void main(String[] args) {
        Server server = new Server();
    }

    public Server() {
        ServerFile.initializeServerFile();

        clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Logger.info("ServerSocket running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                ClientHandler newClientHandler = new ClientHandler(newServerSocket, this);
                newClientHandler.run();
            }
        } catch (IOException e) {
            Logger.error("Error while setting up server socket!");
            Logger.exception(e);
        }
    }
}
