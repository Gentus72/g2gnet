package org.geooo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;

import org.geooo.dto.ClientHandlerDTO;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

/*
 * Der Thread für den Umgang zwischen HostServer und Client
 * Beinhaltet Methoden / Befehle für den Upload
 */
public class HostClientHandler extends ClientHandlerDTO<HostServer> {

    public HostClientHandler(HostServer server, Socket serverSocket) {
        super(server, serverSocket);

        this.registerCommand(ServerCommand.ALLOW, this::handleCommandALLOW);
        this.registerCommand(ServerCommand.AUTH, this::handleCommandAUTH);
        this.fallbackFunction = (String[] args) -> {
            Logger.info(String.format("Redirecting client %s to ccServer at: %s", this.client.getUUID(), server.ccServer.getAddress()));
            sendResponse(String.format("REDIRECT %s", server.ccServer.getAddress()));
            running = false;
        };
    }

    // ALLOW <clientPublicKey> <ressourceUUID> <blockUUID>
    // Ein Befehl vom CCServer um den Upload eines bestimmten Blocks
    // und eines bestimmten Clients zu erlauben
    public void handleCommandALLOW(String[] args) {
        this.server.addClientPublicKey(args[1]);
        this.server.addAllowedBlockUUID(args[3]);

        try {
            File ressourceDIrectory = new File(HostServer.getRessourceDirectory() + args[2]);
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

    // AUTH <ressourceUUID> <encryptedBlockUUID>
    // Anfrage des Clients für den Upload eines Blocks
    // Gibt SUCCESS zurück wenn die Block-UUID mit einem der
    // verfügbaren Publickeys (von Clients) entschlüsselt werden konnte
    // Der Client sendet dann die Blockdatei
    public void handleCommandAUTH(String[] args) {
        String ressourceUUID = args[1];
        String encryptedUUID = args[2];

        for (PublicKey key : this.server.getClientPublicKeys()) {
            String decryptedUUID = G2GUtil.decryptWithPublicKey(encryptedUUID, key);

            if (decryptedUUID != null && this.server.getAllowedBlockUUIDs().contains(decryptedUUID)) {
                try {
                    File ressourceDirectory = new File(HostServer.getRessourceDirectory() + ressourceUUID);

                    if (!ressourceDirectory.exists() || !ressourceDirectory.isDirectory()) {
                        Logger.warn(ressourceDirectory.getPath());
                        Logger.warn("Ressource directory doesn't exist! This shouldn't happen! Creating one...");
                        Files.createDirectory(Path.of(HostServer.getRessourceDirectory() + decryptedUUID));
                    }
                } catch (IOException e) {
                    Logger.error("Error while creating ressource directory!");
                    sendResponse("ERROR Internal server error!");
                    Logger.exception(e);
                    return;
                }

                sendResponse(String.format("SUCCESS %s %s", ressourceUUID, decryptedUUID));
                G2GUtil.receiveFileRemote(String.format("%s%s/%s.g2gblock", HostServer.getRessourceDirectory(), ressourceUUID, decryptedUUID), inputStream);
                sendResponse("SUCCESS");
                return;
            }
        }

        sendResponse("ERROR decryption didn't work! Publickey invalid!");
    }
}
