package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiConsumer;

import org.geooo.dto.ClientDTO;
import org.geooo.metadata.ClientFile;
import org.geooo.util.ClientCommand;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerResponse;

public final class Client extends ClientDTO {

    public static final String RESSOURCE_DIRECTORY = "client/res/";
    public String currentHost = "localhost";
    public int hostPort = 7000;

    public Socket socket;
    public DataOutputStream outputStream;
    public DataInputStream inputStream;
    public Scanner userInputScanner;

    public ClientFile clientFile;
    public boolean isConnected = false;
    public String[] currentClientInput;
    public HashMap<ClientCommand, BiConsumer<Client, String[]>> registeredClientCommands = new HashMap<>();
    public HashMap<ServerResponse, BiConsumer<Client, String[]>> registeredServerResponses = new HashMap<>();

    public static void main(String[] args) {
        // Ressource.reassemble(RESSOURCE_DIRECTORY, "b438d41d25de4bc3a6c043a6431fb0df", new File(RESSOURCE_DIRECTORY + "out.mp4"));
        Client client = new Client();
        // Ressource.disassemble(RESSOURCE_DIRECTORY, new File(RESSOURCE_DIRECTORY + "test3.mp4"), "myTitle", client.getPublicKeyBase64());
        client.startClient();
    }

    public Client() {
        this.clientFile = new ClientFile(RESSOURCE_DIRECTORY + "clientFile.g2gclient");
        if (this.clientFile.getFile().exists()) {
            this.clientFile.readFromFile(this);
        }

        if (this.getUUID() == null) {
            Logger.warn("No UUID read from clientfile! Generating new one...");
            this.setUUID(G2GUtil.getRandomUUID());
            this.clientFile.writeToFile(this);
        }

        registerCommands();
    }

    public void startClient() {
        this.userInputScanner = new Scanner(System.in);

        while (true) {
            String consolePrefix = this.isConnected ? String.format("%s[%s] $> ", Logger.ANSI_CYAN, this.socket.getInetAddress().getHostAddress()) : Logger.ANSI_RESET + "[CLIENT] $> ";
            System.out.print(consolePrefix);
            currentClientInput = this.userInputScanner.nextLine().split(" ");

            if (this.isConnected) {
                ClientHelper.handleServerInteraction(this, currentClientInput);
            } else {
                ClientHelper.handleClientInteraction(this, currentClientInput);
            }
        }
    }

    // Connect all commands to an according function
    // Makes development and readability easy
    public void registerCommands() {
        registeredClientCommands.put(ClientCommand.CONNECT, ClientHelper::handleClientCommandCONNECT);
        registeredClientCommands.put(ClientCommand.INFO, ClientHelper::handleClientCommandINFO);
        registeredClientCommands.put(ClientCommand.AUTOGET, ClientHelper::handleClientCommandAUTOGET);
        registeredClientCommands.put(ClientCommand.AUTOUPLOAD, ClientHelper::handleClientCommandAUTOUPLOAD);
        registeredClientCommands.put(ClientCommand.EXIT, ClientHelper::handleClientCommandEXIT);

        // Servercommands don't have to be registered, because they get interpreted by the server
        // -> leading to a Serverresponse (which is handled by the client)

        registeredServerResponses.put(ServerResponse.INFO, ClientHelper::handleServerResponseINFO);
        registeredServerResponses.put(ServerResponse.REDIRECT, ClientHelper::handleServerResponseREDIRECT);
        registeredServerResponses.put(ServerResponse.DOWNLOAD, ClientHelper::handleServerResponseDOWNLOAD);
        registeredServerResponses.put(ServerResponse.AUTH, ClientHelper::handleServerResponseAUTH);
        registeredServerResponses.put(ServerResponse.SUCCESS, ClientHelper::handleServerResponseSUCCESS);
        registeredServerResponses.put(ServerResponse.ERROR, (Client client, String[] args) -> Logger.error("Error response from server: " + String.join(" ", args)));
        registeredServerResponses.put(ServerResponse.CLOSE, (Client client, String[] args) -> ClientHelper.disconnect(client));
    }
}

