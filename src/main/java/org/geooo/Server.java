package org.geooo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

public class Server extends ServerDTO {

    public static final int SERVER_PORT = 7000;

    public ArrayList<Client> clients;
    public CCServer ccServer;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        startServer();
    }

    public void startServer() {
        this.clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Logger.info("Server running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                ClientHandler newClientHandler = new ClientHandler(newServerSocket, this);
                newClientHandler.start();
            }
        } catch (IOException e) {
            Logger.error("Error while setting up server socket!");
            Logger.exception(e);
        }
    }
}
