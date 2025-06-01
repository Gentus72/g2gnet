package org.geooo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.util.Logger;

public class CCServer extends Server {
    public static final String RESSOURCE_DIRECTORY = "ccserver/";

    // network info
    private String networkUUID;
    private ArrayList<ServerDTO> servers;
    private ArrayList<RessourceDTO> ressources;

    public static void main(String[] args) {
        new CCServer();
    }

    public CCServer() {
        startServer();
    }

    public CCServer(String address) {
        super(address);
    }

    @Override
    public void startServer() {
        this.clients = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.ressources = new ArrayList<>();

        NetworkFile.readFromFile(this); // if it doesn't exist, it will do nothing and just show an error in the console
        NetworkFile.updateRessources(this);
        NetworkFile.writeToFile(this);

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 50, null)) {
            Logger.info("Server running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                CCClientHandler newClientHandler = new CCClientHandler(newServerSocket, this);
                newClientHandler.start();
            }
        } catch (IOException e) {
            Logger.error("Error while setting up server socket!");
            Logger.exception(e);
        }
    }

    public String getNetworkUUID() {
        return this.networkUUID;
    }

    public void setNetworkUUID(String networkUUID) {
        this.networkUUID = networkUUID;
    }

    public ArrayList<ServerDTO> getServers() {
        return this.servers;
    }

    public void setServers(ArrayList<ServerDTO> servers) {
        this.servers = servers;
    }

    public ArrayList<RessourceDTO> getRessources() {
        return this.ressources;
    }

    public void setRessources(ArrayList<RessourceDTO> ressources) {
        this.ressources = ressources;
    }
}
