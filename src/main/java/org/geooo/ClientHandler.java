package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

import org.geooo.dto.RessourceDTO;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;

public class ClientHandler extends Thread {

    Socket serverSocket;
    Client client;
    Server server;

    public ClientHandler(Socket serverSocket, Server server) {
        this.serverSocket = serverSocket;
        this.server = server;

        this.client = new Client();
        this.server.clients.add(this.client);
    }

    @Override
    public void run() {
        Logger.info("A client connected!");
        Logger.info("Now handling " + this.server.clients.size() + " clients!");

        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream()); DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            while (true) {
                String clientInput = inputStream.readUTF();
                String[] arguments = clientInput.split(" ");
                ServerCommand clientCommand = ServerCommand.valueOf(arguments[0]);

                switch (clientCommand) {
                    case INFO -> {
                        Logger.info("Sending server information to client!");
                        outputStream.writeUTF("INFO sending server information!");

                        FilesRemote.sendFile(ServerFile.getServerFile(), outputStream);
                    }
                    case GET -> {
                        if (arguments.length <= 1) {
                            Logger.error("Received GET command without additional arguments!");
                            outputStream.writeUTF("ERROR invalid number of arguments!");
                        }

                        String requestedUUID = arguments[1];

                        if (!ServerFile.doesRessourceExist(requestedUUID)) {
                            Logger.error("Received GET command with invalid UUID!");
                            outputStream.writeUTF("ERROR invalid uuid!");
                        }

                        RessourceDTO ressource = ServerFile.getEmptyRessource(requestedUUID);

                        if (ressource == null) {
                            Logger.error("Received GET command but couldnt find ressource! uuid: " + requestedUUID);
                            outputStream.writeUTF("ERROR invalid uuid!");

                            continue;
                        }

                        File ressourceFile = ressource.getRessourceFile();

                        if (ressourceFile == null) {
                            Logger.error("Received GET command with invalid UUID!");
                            outputStream.writeUTF("ERROR invalid uuid!");

                            continue;
                        }

                        Logger.info("Sending ressource file to client! uuid: " + requestedUUID);
                        outputStream.writeUTF("GET sending ressource file!");
                        sleep(2);
                        FilesRemote.sendFile(ressourceFile, outputStream);

                        for (int i = 0; i < ressource.blockAmount; i++) {
                            Logger.info("trying to send block file: " + i + "/" + ressource.blockAmount);
                            String clientBlockInput = inputStream.readUTF();
                            String[] blockArguments = clientBlockInput.split(" ");
                            ServerCommand clientBlockCommand = ServerCommand.valueOf(arguments[0]);

                            switch (clientBlockCommand) {
                                case GET -> {
                                    if (blockArguments.length < 3 || !blockArguments[1].equals("block")) {
                                        Logger.info(String.valueOf(blockArguments.length));
                                        Logger.error("Received subsequent GET wrong additional arguments even though it should be 'block'! Command: " + clientBlockInput);
                                        outputStream.writeUTF("ERROR no arguments! should be 'block'!");

                                        continue;
                                    }

                                    String blockUUID = blockArguments[2];
                                    File blockFile = new File(Ressource.PARENT_DIRECTORY + requestedUUID + "/" + blockUUID + ".g2gblock");

                                    if (!Files.exists(blockFile.toPath())) {
                                        Logger.error("Received subsequent GET with wrong uuid: " + blockUUID);
                                        outputStream.writeUTF("ERROR wrong block uuid: " + blockUUID);

                                        continue;
                                    }

                                    Logger.info("Sending block file: " + blockUUID);
                                    FilesRemote.sendFile(blockFile, outputStream);
                                }
                                default -> {
                                    Logger.error("Received wrong subsequent command! Should be 'GET'!");
                                    outputStream.writeUTF("ERROR wrong command! should be 'GET'");

                                    continue;
                                }
                            }
                        }
                    }
                    case CLOSE -> {
                        Logger.info("A client has closed their connection!");
                        outputStream.writeUTF("CLOSE closing connection!");

                        inputStream.close();
                        serverSocket.close();

                        this.server.clients.remove(this.client);

                        return; // stop thread
                    }
                    default -> {
                        Logger.warn("Received unknown client command: " + clientInput);

                        outputStream.writeUTF("ERROR unkown command!");
                    }
                }
            }
        } catch (IOException e) {
            Logger.error("Error while setting up client handler!");
            Logger.exception(e);
        } catch (InterruptedException ex) {
        }

        this.server.clients.remove(this.client);
    }

    private void sleep(int seconds) throws InterruptedException {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Logger.error("Error while sleeping thread ClientHandler lol!");
            Logger.exception(e);
        }
    }
}
