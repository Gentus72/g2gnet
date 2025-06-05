package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.geooo.dto.ClientHandlerDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.TemporaryRessourceFile;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

public class CCClientHandler extends ClientHandlerDTO<CCServer> {

    public CCClientHandler(CCServer server, Socket serverSocket) {
        super(server, serverSocket);

        registerCommand(ServerCommand.INFO, this::handleCommandINFO);
        registerCommand(ServerCommand.AUTH, this::handleCommandAUTH);
        registerCommand(ServerCommand.REGISTER, this::handleCommandREGISTER);
    }

    public void handleCommandINFO(String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                sendResponse(String.format("INFO NETWORK %s", this.server.getNetworkUUID()));
                FilesRemote.sendFile(this.server.getNetworkFile().getFile(), outputStream);
            }
            case "RESSOURCE" -> {
                // check that ressourcefile exists
                String ressourceUUID = args[2];
                File ressourceFile = new File(String.format("%s%s.g2g", CCServer.CCSERVER_DIRECTORY, ressourceUUID));

                if (!ressourceFile.exists()) {
                    sendResponse(String.format("ERROR ressource %s doesn't exist!", ressourceUUID));
                } else {
                    sendResponse(String.format("INFO RESSOURCE %s", ressourceUUID));

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Logger.error("Error while sleeping lol...");
                        Logger.exception(e);
                    }

                    FilesRemote.sendFile(ressourceFile, outputStream);
                }
            }
            default -> {
                Logger.error("Received INFO command with wrong additional commands!");
                sendResponse("ERROR wrong arguments for command type INFO!");
            }
        }
    }

    public void handleCommandAUTH(String[] args) {
        ArrayList<ServerDTO> servers = server.getServers(); // avoid repeated call
        servers.add(this.server); // add ccserver because it can also accept and deliver blocks
        int currentIndex = 0;

        sendResponse("SUCCESS");

        FilesRemote.receiveFile("tmpRessourceFile.g2gtmp", inputStream);
        Logger.info("Received temporary ressourcefile!");

        TemporaryRessourceFile tmpFile = new TemporaryRessourceFile("tmpRessourceFile.g2gtmp");

        HashMap<Integer, String> blockLocations = new HashMap<>();
        String clientPublicKey = tmpFile.getConfigContent().get("PublicKey");

        // send allow to all servers
        for (RessourceBlockDTO block : tmpFile.getBlocks()) {
            // while send ALLOW wasn't successfull, try another one
            while (!sendAllow(servers.get(currentIndex).getAddress(), clientPublicKey, block.getUUID())) {
                servers.remove(currentIndex);

                currentIndex++;
                if (currentIndex >= servers.size()) currentIndex = 0;
            }

            blockLocations.put(block.getSequenceID(), servers.get(currentIndex).getAddress());

            currentIndex++;
            if (currentIndex >= servers.size()) currentIndex = 0;
        }

        // assemble full ressource file
        File ressourceFile = tmpFile.convertToRessourceFile(CCServer.CCSERVER_DIRECTORY, "tmpRessourceFile.g2gtmp", blockLocations);

        if (ressourceFile == null || clientPublicKey == null) {
            Logger.error("Error while processing tmpfile!");
            sendResponse("ERROR Internal server error!");
        }

        sendResponse("SUCCESS");
        FilesRemote.sendFile(ressourceFile, outputStream);
    }

    public void handleCommandREGISTER(String[] args) {
        boolean alreadyInList = false;

        for (ServerDTO s : this.server.getServers()) {
            if (s != null && s.getUUID().equals(args[1])) {
                // server already in list
                alreadyInList = true;
                break;
            }
        }

        if (!alreadyInList) this.server.addServer(new ServerDTO(args[1], args[2]));

        sendResponse(String.format("SUCCESS %s %s", this.server.getNetworkUUID(), "172.20.0.10"));
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
