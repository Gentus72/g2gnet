package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.geooo.dto.ClientDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.ServerFile;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerResponse;

public class Server extends ServerDTO {
    public static final String SERVER_DIRECTORY = "server/";

    public ArrayList<ClientDTO> clients;
    public CCServer ccServer;
    public ServerFile serverFile;

    public static void main(String[] args) {
        Server server = new Server();

        server.startServer();
    }

    public Server() {
        super();
    }

    public Server(String address) {
        super(address);
    }

    public void connectToCC(String ccAddress, String networkUUID) {
        try {
            Socket socket = new Socket(ccAddress, 7000);
            socket.setSoTimeout(30000); // 30 sec
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            Logger.info(String.format("Successfully connected to Server [%s:7000]!", ccAddress));

            outputStream.writeUTF(String.format("REGISTER %s %s", this.getUUID(), InetAddress.getLocalHost().getHostAddress()));
            outputStream.flush();

            String response = inputStream.readUTF();
            String[] responseArgs = response.split(" ");

            if (ServerResponse.valueOf(responseArgs[0]).equals(ServerResponse.SUCCESS)) {
                if (responseArgs.length < 3 || !responseArgs[1].equals(networkUUID)) Logger.warn(String.format("NetworkUUID mismatch! %s != %s -> Updating...", responseArgs[1], networkUUID));

                Logger.info("Successfully connected to CCServer at: " + ccAddress);
                this.ccServer = new CCServer(responseArgs[2]);
                this.ccServer.setNetworkUUID(responseArgs[1]);
                this.serverFile.writeToFile(this);
            } else {
                Logger.error("Error while connecting to CCServer! Didn't receive SUCCESS, received: " + response);
            }

            outputStream.writeUTF("DISCONNECT");
            outputStream.flush();

            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            Logger.error("Error while connecting to CCServer!");
            Logger.exception(e);
            System.exit(1);
        }
    }

    public void startServer() {
        this.clients = new ArrayList<>();

        this.serverFile = new ServerFile(SERVER_DIRECTORY + "serverFile.g2gsrv");

        if (!this.serverFile.getFile().exists()) {
            Logger.warn("No config file detected! Generating blank one - please fill it out!");
            this.setUUID(G2GUtil.getRandomUUID());
            this.serverFile.generateBlankConfig(this);
            // System.exit(0);
        }

        HashMap<String, String> configContent = this.serverFile.getConfigContent();
        this.setUUID(configContent.get("UUID"));

        connectToCC(configContent.get("CCServerAddress"), configContent.get("NetworkUUID"));

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            Logger.info("Server running on port " + SERVER_PORT + "!");

            while (true) {
                Socket newServerSocket = serverSocket.accept();

                ClientHandler newClientHandler = new ClientHandler(this, newServerSocket);
                newClientHandler.run();
            }
        } catch (IOException e) {
            Logger.error("Error while setting up server socket!");
            Logger.exception(e);
        }
    }
}
