package org.geooo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

import org.geooo.dto.ClientHandlerDTO;
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
        this.server.addAllowedBlockUUID(args[2]);

        sendResponse("SUCCESS");
    }

    public void handleCommandAUTH(String[] args) {
        String encryptedUUID = args[1];

        for (PublicKey key : this.server.getClientPublicKeys()) {
            String decryptedUUID = decryptWithPublicKey(encryptedUUID, key);

            if (decryptedUUID != null && this.server.getAllowedBlockUUIDs().contains(decryptedUUID)) {
                sendResponse("SUCCESS");

                // ensure ressource directory
                try {
                    if (!new File(Server.getRessourceDirectory() + decryptedUUID).exists()) {
                        Logger.warn("Ressource directory doesn't exist! This should happen! Creating one...");
                        Files.createDirectory(Path.of(Server.getRessourceDirectory() + decryptedUUID));
                    }
                } catch (IOException e) {
                    Logger.error("Error while creating ressource directory!");
                    Logger.exception(e);
                }

                FilesRemote.receiveFile(String.format("%s%s/%s.g2gblock", Server.getRessourceDirectory(), decryptedUUID, decryptedUUID), inputStream);
                sendResponse("SUCCESS");
            }
        }
    }

    static String decryptWithPublicKey(String encryptedText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // do nothing, the decryption failed on purpose - the key didn't work!
            return null;
        }
    }
}
