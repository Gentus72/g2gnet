package org.geooo.dto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import org.geooo.CCServer;
import org.geooo.Server;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

public class ClientHandlerDTO<T extends Server> implements Runnable {

    public Socket serverSocket;
    public ClientDTO client;
    protected T server;
    public DataInputStream inputStream;
    public DataOutputStream outputStream;
    public HashMap<ServerCommand, Consumer<String[]>> registeredCommands;
    public Consumer<String[]> fallbackFunction;
    public boolean running;

    public ClientHandlerDTO(T server, Socket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
        this.running = true;
        this.registeredCommands = new HashMap<>();

        this.client = new ClientDTO(UUID.randomUUID().toString().replace("-", ""));
        this.client.setAddress(this.serverSocket.getInetAddress().getHostAddress());

        Logger.info(String.format("New client [%s] connected!", client.getAddress()));
        this.server.clients.add(this.client);

        registerCommand(ServerCommand.DISCONNECT, this::handleCommandCLOSE);
        registerCommand(ServerCommand.GETBLOCK, this::handleCommandGETBLOCK);
        registerCommand(ServerCommand.STATUS, this::handleCommandSTATUS);

        this.fallbackFunction = (String[] args) -> {
            Logger.error("Fallback function not defined but called!");
            sendResponse("ERROR Internal server error!");
        };
    }

    public void sendResponse(String payload) {
        try {
            this.outputStream.writeUTF(payload);
        } catch (IOException e) {
            Logger.error("Error while sending server response!");
            Logger.exception(e);
        }
    }

    public void setup() {
        try {
            this.inputStream = new DataInputStream(this.serverSocket.getInputStream());
            this.outputStream = new DataOutputStream(this.serverSocket.getOutputStream());
        } catch (IOException e) {
            Logger.error("Error while setting up streams!");
            Logger.exception(e);
        }
    }

    public void handleCommandSTATUS(String[] args) {
        String response = "SUCCESS \nStatus: ";

        if (!(this.server instanceof CCServer)) { // if its not a ccServer
            if (this.server.ccServer == null) {
                response += "DISCONNECTED";
            } else {
                response += "CONNECTED\n";
                response += String.format(" - NetworkUUID: %s\n - CCServerIP: %s\n - AuthorizedKeys:\n", this.server.ccServer.getNetworkUUID(), this.server.ccServer.getAddress());

                for (String publicKey : this.server.getClientPublicKeysBase64()) {
                    response += String.format("   - %s\n", publicKey);
                }
            }
        } else { // if its a ccServer
            CCServer s = (CCServer) this.server;

            response += "CCSERVER\n";
            response += String.format(" - NetworkUUID: %s\n - Connected Servers: %d\n - Stored Ressources: %d\n --- for more Information, request the networkfile --- \n", s.getNetworkUUID(), s.getServers().size(), s.getRessources().size());
        }

        response += "ALLOWED KEYS:\n";
        for (String key : this.server.getClientPublicKeysBase64()) {
            response += key + "\n";
        }

        response += "ALLOWED UUIDS:\n";
        for (String uuid : this.server.getAllowedBlockUUIDs()) {
            response += uuid + "\n";
        }

        sendResponse(response);
    }

    public void handleCommandGETBLOCK(String[] args) {
        // check if ressource directory exists
        String ressourceUUID = args[1];
        String defaultDirectory = T.getRessourceDirectory();
        File ressourceDirectory = new File(defaultDirectory + ressourceUUID + "/");

        String blockUUID = args[2];
        File blockFile = new File(String.format("%s%s/%s.g2gblock", defaultDirectory, ressourceUUID, blockUUID));

        if (!ressourceDirectory.exists() || !blockFile.exists()) {
            sendResponse("ERROR ressource directory or block file doesn't exist!");

            close();
            return; // stop thread
        }

        // handle download
        sendResponse(String.format("DOWNLOAD %s %s", ressourceUUID, blockUUID));

        FilesRemote.sendFile(blockFile, outputStream);
    }

    public void handleCommandCLOSE(String[] args) {
        try {
            Logger.info("Client has closed their connection!");
            this.outputStream.writeUTF("CLOSE closing connection!");
            this.running = false;
            close();
        } catch (IOException e) {
            Logger.error("Error sending CLOSE response inside handleCommandCLOSE()");
            Logger.exception(e);
        }
    }

    public void close() {
        try {
            this.inputStream.close();
            this.outputStream.close();

            this.server.clients.remove(this.client);
        } catch (IOException e) {
            Logger.error("Error while closing streams!");
            Logger.exception(e);
        }
    }

    public final void registerCommand(ServerCommand command, Consumer<String[]> function) {
        this.registeredCommands.put(command, function);
    }

    @Override
    public void run() {
        setup();
        String clientInput = "";

        while (running) {
            try {
                clientInput = this.inputStream.readUTF();
                String[] clientArgs = clientInput.split(" ");
                Logger.info(String.format("Received client command: %s", clientInput));

                ServerCommand command = ServerCommand.valueOf(clientArgs[0]);
                if (!command.hasCorrectArgsAmount(clientArgs.length)) {
                    sendResponse("ERROR wrong number of arguments! Should be " + command.getArgsAmount());
                    continue;
                }

                boolean validCommand = false;

                for (var entry : registeredCommands.entrySet()) {
                    if (command.equals(entry.getKey())) {
                        // call according function
                        validCommand = true;
                        entry.getValue().accept(clientArgs);
                    }
                }

                // if ServerCommand.valueOf(); didn't throw an exception, the client still sent
                // a valid command
                // call fallback function
                if (!validCommand) {
                    this.fallbackFunction.accept(clientArgs);
                }
            } catch (IOException e) {
                if (e instanceof EOFException) {
                    Logger.error("Client disconnected unexpectedly!");
                    return;
                }

                Logger.error("Error while handling client!");
                Logger.exception(e);
                return;
            } catch (IllegalArgumentException e) {
                Logger.warn("Received unknown command: " + clientInput.split(" ")[0]);
                sendResponse("ERROR unkown command! Try again...");
                Logger.exception(e);
            }
        }

        Logger.info("Clienthandler thread exiting!");
    }
}
