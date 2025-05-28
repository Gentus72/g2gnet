package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import org.geooo.dto.ClientDTO;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;

// TODO temporary, inheritance should be fixed!
public class CCClientHandler extends Thread {
    Socket serverSocket;
    ClientDTO client;
    CCServer server;

    public CCClientHandler(Socket serverSocket, CCServer server) {
        this.serverSocket = serverSocket;
        this.server = server;

        this.client = new ClientDTO(UUID.randomUUID().toString().replace("-", ""));
        Logger.info(String.format("New client connected with UUID: %s", client.getUUID()));
        this.server.clients.add(this.client);
    }

    @Override
    public void run() {
        Logger.info("Now handling " + this.server.clients.size() + " clients!");

        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            while (true) {
                String clientInput = inputStream.readUTF();
                Logger.info(String.format("Received client command: %s", clientInput));
                ServerCommand command = getCommandFromClientInput(clientInput);
                String[] clientArgs = clientInput.split(" ");

                switch (command) {
                    case INFO -> {
                        if (clientArgs.length <= 1) {
                            Logger.error("Received INFO command without additional arguments!");
                            outputStream.writeUTF("ERROR invalid number of arguments!");
                            outputStream.flush();
                        }

                        switch (clientArgs[1]) {
                            case "NETWORK" -> {
                                outputStream.writeUTF(String.format("INFO NETWORK %s", this.server.getNetworkUUID()));
                                outputStream.flush();

                                FilesRemote.sendFile(new File("networkFile.g2gnet"), outputStream);
                            }
                            case "RESSOURCE" -> {
                                // check that ressourcefile exists
                                String ressourceUUID = clientArgs[2];
                                File ressourceFile = new File(
                                        String.format("%s%s.g2g", Server.RESSOURCE_DIRECTORY, ressourceUUID));

                                if (!ressourceFile.exists()) {
                                    outputStream.writeUTF(String.format("ERROR ressource %s doesn't exist!", ressourceUUID));
                                    outputStream.flush();
                                } else {
                                    outputStream.writeUTF(String.format("INFO RESSOURCE %s"));
                                    outputStream.flush();

                                    FilesRemote.sendFile(ressourceFile, outputStream);
                                }
                            }
                            default -> {
                                Logger.error("Received INFO command with wrong additional commands!");
                                outputStream.writeUTF("ERROR wrong arguments for command type INFO!");
                                outputStream.flush();
                            }
                        }
                    }
                    case GET -> {
                        if (clientArgs.length <= 1) {
                            Logger.error("Received GET command without additional arguments!");
                            outputStream.writeUTF("ERROR invalid number of arguments!");
                        }

                        // String requestedUUID = arguments[1];
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
        }

        this.server.clients.remove(this.client);
    }

    public ServerCommand getCommandFromClientInput(String clientInput) {
        return ServerCommand.valueOf(clientInput.split(" ")[0]);
    }
}
