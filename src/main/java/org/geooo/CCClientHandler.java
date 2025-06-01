package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.UUID;

import org.geooo.dto.ClientDTO;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

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

                                FilesRemote.sendFile(new File(CCServer.RESSOURCE_DIRECTORY + "networkFile.g2gnet"), outputStream);
                            }
                            case "RESSOURCE" -> {
                                // check that ressourcefile exists
                                String ressourceUUID = clientArgs[2];
                                File ressourceFile = new File(String.format("%s%s.g2g", CCServer.RESSOURCE_DIRECTORY, ressourceUUID));

                                if (!ressourceFile.exists()) {
                                    outputStream.writeUTF(String.format("ERROR ressource %s doesn't exist!", ressourceUUID));
                                    outputStream.flush();
                                } else {
                                    outputStream.writeUTF(String.format("INFO RESSOURCE %s", ressourceUUID));
                                    outputStream.flush();

                                    Thread.sleep(500);
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
                    case GETBLOCK -> {
                        if (clientArgs.length < 3) {
                            outputStream.writeUTF("ERROR insufficient arguments! Format is: GETBLOCK <ressourceUUID> <blockUUID>");
                            outputStream.flush();

                            inputStream.close();
                            outputStream.close();
                            return;
                        }

                        // check if ressource directory exists
                        String ressourceUUID = clientArgs[1];
                        File ressourceDirectory = new File(CCServer.RESSOURCE_DIRECTORY + ressourceUUID);

                        String blockUUID = clientArgs[2];
                        File blockFile = new File(String.format("%s%s/%s.g2gblock", CCServer.RESSOURCE_DIRECTORY, ressourceUUID, blockUUID));

                        if (!ressourceDirectory.exists() || !blockFile.exists()) {
                            outputStream.writeUTF("ERROR ressource directory or block file doesn't exist!");
                            outputStream.flush();

                            inputStream.close();
                            outputStream.close();
                            return;
                        }

                        // handle download
                        outputStream.writeUTF(String.format("DOWNLOAD %s %s", ressourceUUID, blockUUID));
                        outputStream.flush();

                        FilesRemote.sendFile(blockFile, outputStream);
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
        } catch (Exception e) {
            Logger.error("Error while setting up client handler!");
            Logger.exception(e);
        }

        this.server.clients.remove(this.client);
    }

    public ServerCommand getCommandFromClientInput(String clientInput) {
        return ServerCommand.valueOf(clientInput.split(" ")[0]);
    }
}
