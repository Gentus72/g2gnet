package org.geooo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;

import org.geooo.dto.ClientHandlerDTO;
import org.geooo.util.EncryptionManager;
import org.geooo.util.FilesRemote;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

public class ClientHandler extends ClientHandlerDTO<Server> {

    public ClientHandler(Server server, Socket serverSocket) {
        super(server, serverSocket);

        this.registerCommand(ServerCommand.ALLOW, this::handleCommandALLOW);
        this.registerCommand(ServerCommand.AUTH, this::handleCommandAUTH);
        this.fallbackFunction = (String[] args) -> {
            Logger.info(String.format("Redirecting client %s to ccServer at: %s", this.client.getUUID(), server.ccServer.getAddress()));
            sendResponse(String.format("REDIRECT %s", server.ccServer.getAddress()));
            running = false;
        };
    }

    public void handleCommandALLOW(String[] args) {
        this.server.addClientPublicKey(args[1]);
        this.server.addAllowedBlockUUID(args[3]);

        try {
            File ressourceDIrectory = new File(Server.getRessourceDirectory() + args[2]);
            if (!ressourceDIrectory.exists()) {
                Logger.info("Ressource directory doesn't exist! Creating it...");
                Files.createDirectory(Path.of(ressourceDIrectory.getPath()));
            }
        } catch (IOException e) {
            Logger.error("Error while creating ressourcefile!");
            Logger.exception(e);
        }

        sendResponse("SUCCESS");
    }

    public void handleCommandAUTH(String[] args) {
        String ressourceUUID = args[1];
        String encryptedUUID = args[2];

        for (PublicKey key : this.server.getClientPublicKeys()) {
            String decryptedUUID = EncryptionManager.decryptWithPublicKey(encryptedUUID, key);

            if (decryptedUUID != null && this.server.getAllowedBlockUUIDs().contains(decryptedUUID)) {
                sendResponse(String.format("SUCCESS %s %s", ressourceUUID, decryptedUUID));

                // ensure ressource directory
                try {
                    File ressourceDirectory = new File(Server.getRessourceDirectory() + ressourceUUID);

                    if (!ressourceDirectory.exists() || !ressourceDirectory.isDirectory()) {
                        Logger.warn(ressourceDirectory.getPath());
                        Logger.warn("Ressource directory doesn't exist! This shouldn't happen! Creating one...");
                        Files.createDirectory(Path.of(Server.getRessourceDirectory() + decryptedUUID));
                    }
                } catch (IOException e) {
                    Logger.error("Error while creating ressource directory!");
                    Logger.exception(e);
                }

                FilesRemote.receiveFile(String.format("%s%s/%s.g2gblock", Server.getRessourceDirectory(), ressourceUUID, decryptedUUID), inputStream);
                sendResponse("SUCCESS");
                return;
            }
        }

        sendResponse("ERROR decryption didn't work! Publickey invalid!");
    }
}
