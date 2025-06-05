package org.geooo.dto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
        Logger.info(String.format("New client connected with UUID: %s", client.getUUID()));
        this.server.clients.add(this.client);

        registerCommand(ServerCommand.DISCONNECT, this::handleCommandCLOSE);
        registerCommand(ServerCommand.GETBLOCK, this::handleCommandGETBLOCK);

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

    public void handleCommandGETBLOCK(String[] args) {
        // check if ressource directory exists
        String ressourceUUID = args[1];
        File ressourceDirectory = new File(CCServer.CCSERVER_DIRECTORY + ressourceUUID);

        String blockUUID = args[2];
        File blockFile = new File(String.format("%s%s/%s.g2gblock", CCServer.CCSERVER_DIRECTORY, ressourceUUID, blockUUID));

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
                if (!validCommand) this.fallbackFunction.accept(clientArgs);
            } catch (IOException e) {
                Logger.error("Error while handling client!");
                Logger.exception(e);
                return;
            } catch (IllegalArgumentException e) {
                Logger.warn("Received unknown command: " + clientInput);
                sendResponse("ERROR unkown command! Try again...");
            }
        }

        Logger.info("Clienthandler thread exiting!");
    }
}
