package org.geooo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.NetworkFile;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;

public class CCServer extends HostServer {

    // network info
    private String networkUUID;
    private ArrayList<ServerDTO> servers;
    private ArrayList<RessourceDTO> ressources;
    private NetworkFile networkFile;
    private String networkLabel;

    /**
     * Hauptmethode des CCServers
     * @param args
     */
    public static void main(String[] args) {
        CCServer server = new CCServer();
        server.startServer(args);
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

    @Override
    public void startServer(String[] args) {
        if (this.networkFile.getFile().exists()) {
            this.networkFile.readFromFile(this);
        } else {
            Logger.warn("No network file detected! Trying to generate from command line arguments...");

            for (String arg : args) {
                if (arg.contains("=")) {
                    String prefix = arg.split("=")[0];
                    String value = arg.split("=")[1];

                    switch (prefix) {
                        case "--networkLabel" -> {
                            this.setNetworkLabel(value);
                        }
                        case "--networkUUID" -> {
                            this.setNetworkUUID(value);
                        }
                        default -> {
                            Logger.warn("unkown command line argument: " + arg);
                        }
                    }
                }
            }
        }

        if (this.getUUID() == null) {
            this.setUUID(G2GUtil.getRandomUUID());
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

    /**
     * @return Gibt die UUID des Netzwerks aus
     */
    public String getNetworkUUID() {
        return this.networkUUID;
    }

    /**
     * Setzt die UUID des Netzwerks
     * @param networkUUID
     */
    public void setNetworkUUID(String networkUUID) {
        this.networkUUID = networkUUID;
    }

    /**
     * @return gibt eine Liste aller HostServer des Netzwerks aus
     */
    public ArrayList<ServerDTO> getServers() {
        return this.servers;
    }

    /**
     * Setzt die Liste aller HostServer des Netzwerks
     * @param servers
     */
    public void setServers(ArrayList<ServerDTO> servers) {
        this.servers = servers;
    }

    /**
     * Fügt einen HostServer zur Liste hinzu
     * @param server
     */
    public void addServer(ServerDTO server) {
        this.servers.add(server);
        this.networkFile.writeToFile(this);
    }

    /**
     * @return Gibt eine Liste aller verfügbaren Ressourcen aus
     */
    public ArrayList<RessourceDTO> getRessources() {
        return this.ressources;
    }

    /**
     * Setzt die Liste aller verfügbaren Ressourcen
     * @param ressources
     */
    public void setRessources(ArrayList<RessourceDTO> ressources) {
        this.ressources = ressources;
    }

    /**
     * Fügt eine Ressource zur Liste aller verfügbaren Ressourcen hinzu
     * @param ressource
     */
    public void addRessource(RessourceDTO ressource) {
        Logger.warn("Debug adding ressource: " + ressource.getUUID());
        this.ressources.add(ressource);
        this.networkFile.writeToFile(this);
    }

    /**
     * @return Gibt die Netzwerkdatei aus
     */
    public NetworkFile getNetworkFile() {
        return this.networkFile;
    }

    /**
     * @return Gibt den Pfad des Ordners an, wo alle Daten des CCServers gespeichert werden
     */
    public static String getRessourceDirectory() {
        return "ccserver/";
    }

    /**
     * @return Gibt das Label des Netzwerks aus
     */
    public String getNetworkLabel() {
        return this.networkLabel;
    }

    /**
     * Setzt das Label des Netzwerks
     * @param networkLabel
     */
    public void setNetworkLabel(String networkLabel) {
        this.networkLabel = networkLabel;
    }
}
