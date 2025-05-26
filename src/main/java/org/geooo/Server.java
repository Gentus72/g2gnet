package org.geooo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.dto.ServerDTO;
import org.geooo.util.Logger;

public class Server extends ServerDTO {

    public static final int SERVER_PORT = 7000;

    private static Server serverInstance;
    public ArrayList<Client> clients;

    public static void main(String[] args) {
        startServer();
    }

    public static Server getInstance() {
        if (serverInstance == null) {
            return new Server();
        }

        return serverInstance;
    }

    public static void startServer() {
        ServerFile.initializeServerFile();

        // Ressource res1 = new Ressource(new File("res/test.jpg"), "test_ressource");

        getInstance().clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Logger.info("Server running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                ClientHandler newClientHandler = new ClientHandler(newServerSocket, getInstance());
                newClientHandler.run();
            }
        } catch (IOException e) {
            Logger.error("Error while setting up server socket!");
            Logger.exception(e);
        }
    }
}
