package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.geooo.dto.ClientDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.TemporaryRessourceFile;
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
                String[] clientArgs = clientInput.split(" ");
                Logger.info(String.format("Received client command: %s", clientInput));

                try {
                    ServerCommand command = ServerCommand.valueOf(clientInput);
                    if (!command.hasCorrectArgsAmount(clientArgs.length)) {
                        sendResponse(outputStream, "ERROR wrong number of arguments! Should be " + command.getArgsAmount());
                        continue;
                    }

                    switch (command) {
                        case INFO -> {
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
                        case AUTH -> {
                            sendResponse(outputStream, "SUCCESS");

                            ArrayList<ServerDTO> servers = server.getServers();
                            int currentIndex = 0;

                            FilesRemote.receiveFile("tmpRessourceFile.g2gtmp", inputStream);
                            Logger.info("Received temporary ressourcefile!");

                            TemporaryRessourceFile tmpFile = new TemporaryRessourceFile("tmpRessourceFile.g2gtmp");

                            HashMap<Integer, String> blockLocations = new HashMap<>();
                            String clientPublicKey = tmpFile.getConfigContentFromFile(tmpFile.file).get("PublicKey");

                            // send allow to all servers
                            for (RessourceBlockDTO block : tmpFile.getBlocks()) {
                                // while send ALLOW wasn't successfull, try another one
                                while (!sendAllow(servers.get(currentIndex).getAddress(), clientPublicKey, clientInput)) {
                                    servers.remove(currentIndex);

                                    currentIndex++;
                                    if (currentIndex >= servers.size()) currentIndex = 0;
                                }

                                blockLocations.put(block.getSequenceID(), servers.get(currentIndex).getAddress());

                                currentIndex++;
                                if (currentIndex >= servers.size()) currentIndex = 0;
                            }

                            // assemble full ressource file
                            File ressourceFile = tmpFile.convertToRessourceFile(CCServer.RESSOURCE_DIRECTORY, "tmpRessourceFile.g2gtmp", blockLocations);

                            if (ressourceFile == null || clientPublicKey == null) {
                                Logger.error("Error while processing tmpfile!");
                                sendResponse(outputStream, "ERROR Internal server error!");
                            }

                            sendResponse(outputStream, "SUCCESS");
                            FilesRemote.sendFile(ressourceFile, outputStream);
                        }
                        case GETBLOCK -> {
                            // check if ressource directory exists
                            String ressourceUUID = clientArgs[1];
                            File ressourceDirectory = new File(CCServer.RESSOURCE_DIRECTORY + ressourceUUID);
    
                            String blockUUID = clientArgs[2];
                            File blockFile = new File(String.format("%s%s/%s.g2gblock", CCServer.RESSOURCE_DIRECTORY, ressourceUUID, blockUUID));
    
                            if (!ressourceDirectory.exists() || !blockFile.exists()) {
                                sendResponse(outputStream, "ERROR ressource directory or block file doesn't exist!");
    
                                inputStream.close();
                                outputStream.close();
                                return; // stop thread
                            }
    
                            // handle download
                            sendResponse(outputStream, String.format("DOWNLOAD %s %s", ressourceUUID, blockUUID));
    
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
                            sendResponse(outputStream, "ERROR unknown command!");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Logger.error("Malformed client command!");
                }
            }
        } catch (IOException | InterruptedException e) {
            Logger.error("Error while setting up client handler!");
            Logger.exception(e);
        }

        this.server.clients.remove(this.client);
    }

    public void sendResponse(DataOutputStream outputStream, String payload) {
        try {
            outputStream.writeUTF(payload);
        } catch (IOException e) {
            Logger.error("Error while sending server response!");
            Logger.exception(e);
        }
    }

    public boolean sendAllow(String address, String clientPublicKey, String blockUUID) {
        try (Socket tmpSocket = new Socket(address, 7000);
            DataOutputStream tmpOutputStream = new DataOutputStream(tmpSocket.getOutputStream());
            DataInputStream tmpInputStream = new DataInputStream(tmpSocket.getInputStream());) {

            tmpOutputStream.writeUTF(String.format("ALLOW %s %s", clientPublicKey, blockUUID));

            String response = tmpInputStream.readUTF();
            return response.contains("SUCCESS");
        } catch (IOException e) {
            Logger.error("Error while sending allow command to " + address);
            return false;
        }
    }
}
