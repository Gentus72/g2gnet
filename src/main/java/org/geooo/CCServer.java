package org.geooo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.util.Logger;

public class CCServer extends HostServer {

    // network info
    private String networkUUID;
    private ArrayList<ServerDTO> servers;
    private ArrayList<RessourceDTO> ressources;
    private NetworkFile networkFile;

    public static void main(String[] args) {
        CCServer server = new CCServer();
        server.startServer();
    }

    public CCServer() {
        super();

        this.clients = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.ressources = new ArrayList<>();
        this.networkFile = new NetworkFile(getRessourceDirectory() + "networkFile.g2gnet");
    }

    public CCServer(String address) {
        super(address);
    }

    public void startServer() {
        if (this.networkFile.getFile().exists()) {
            this.networkFile.readFromFile(this);
        }
        this.networkFile.updateRessources(this);
        this.networkFile.writeToFile(this);

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            Logger.info("Server running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                CCClientHandler newClientHandler = new CCClientHandler(this, newServerSocket);
                newClientHandler.run();
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

    public void addServer(ServerDTO server) {
        this.servers.add(server);
        this.networkFile.writeToFile(this);
    }

    public ArrayList<RessourceDTO> getRessources() {
        return this.ressources;
    }

    public void setRessources(ArrayList<RessourceDTO> ressources) {
        this.ressources = ressources;
    }

    public NetworkFile getNetworkFile() {
        return this.networkFile;
    }

    public static String getRessourceDirectory() {
        return "ccserver/";
    }
}
