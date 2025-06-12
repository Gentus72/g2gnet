package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.geooo.dto.ClientHandlerDTO;
import org.geooo.dto.RessourceBlockDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.metadata.RessourceFile;
import org.geooo.util.G2GUtil;
import org.geooo.util.Logger;
import org.geooo.util.ServerCommand;

/*
 * Der Thread für den Umgang zwischen CCServer und Client
 * Beinhaltet Methoden / Befehle zum Authorisieren von Uploads und
 * zum registrieren von Servern im Netzwerk
 */
public class CCClientHandler extends ClientHandlerDTO<CCServer> {

    public CCClientHandler(CCServer server, Socket serverSocket) {
        super(server, serverSocket);

        registerCommand(ServerCommand.INFO, this::handleCommandINFO);
        registerCommand(ServerCommand.AUTH, this::handleCommandAUTH);
        registerCommand(ServerCommand.REGISTER, this::handleCommandREGISTER);
    }

    // INFO <NETWORK | RESSOURCE> <networkUUID | ressourceUUID>
    // Lässt den Client Netzwerk- / Ressourcendateien herunterladen
    // Gibt ERROR zurück, wenn das Netzwerk / die Ressource nicht gefunden wurde
    public void handleCommandINFO(String[] args) {
        switch (args[1]) {
            case "NETWORK" -> {
                sendResponse(String.format("INFO NETWORK %s", this.server.getNetworkUUID()));
                G2GUtil.sendFileRemote(this.server.getNetworkFile().getFile(), outputStream);
            }
            case "RESSOURCE" -> {
                // check that ressourcefile exists
                String ressourceUUID = args[2];
                File ressourceFile = new File(String.format("%s%s.g2g", CCServer.getRessourceDirectory(), ressourceUUID));

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

                    G2GUtil.sendFileRemote(ressourceFile, outputStream);
                }
            }
            default -> {
                Logger.error("Received INFO command with wrong additional commands!");
                sendResponse("ERROR wrong arguments for command type INFO!");
            }
        }
    }

    // AUTH <ressourceUUID>
    // Authorisiert den Upload einer Ressource
    // Gibt AUTH SUCCESS zurück, um den Upload zu gewähren
    // Danach sendet der Client die unvollständige Ressourcendatei
    // Der CCServer füllt sie aus und gibt sie vervollständigt
    // mit IP-Addressen zurück, wo jeder einzelne Block hochgeladen werden kann
    public void handleCommandAUTH(String[] args) {
        ArrayList<String> locations = new ArrayList<>(); // avoid repeated call
        for (ServerDTO server : this.server.getServers()) {
            locations.add(server.getAddress());
        }

        locations.add(this.server.getAddress()); // add ccserver because it can also accept and deliver blocks
        int currentIndex = 0;
        Logger.info(Arrays.toString(locations.toArray()));

        sendResponse("AUTH SUCCESS " + args[1]); // add ressourceUUID

        String ressourceFilePath = CCServer.getRessourceDirectory() + args[1] + ".g2g";
        G2GUtil.receiveFileRemote(ressourceFilePath, inputStream);
        Logger.info("Received temporary ressourcefile!");

        RessourceFile ressourceFile = new RessourceFile(ressourceFilePath);

        HashMap<Integer, String> blockLocations = new HashMap<>();
        String clientPublicKey = ressourceFile.getConfigContent().get("PublicKey");

        // send allow to all servers
        for (RessourceBlockDTO block : ressourceFile.getBlocks()) {
            // while send ALLOW wasn't successfull, try another one
            while (!sendAllow(locations.get(currentIndex), clientPublicKey, args[1], block.getUUID())) {
                locations.remove(currentIndex);

                currentIndex++;
                if (currentIndex >= locations.size()) {
                    currentIndex = 0;
                }
            }

            blockLocations.put(block.getSequenceID(), locations.get(currentIndex));

            currentIndex++;
            if (currentIndex >= locations.size()) {
                currentIndex = 0;
            }
        }

        ressourceFile.replaceBlockLocations(blockLocations);
        G2GUtil.sendFileRemote(ressourceFile.getFile(), outputStream);
        Logger.info("Sent assembled ressourcefile!");
    }

    // REGISTER <serverUUID>
    // Fügt einen Server zum Netzwerk hinzu
    public void handleCommandREGISTER(String[] args) {
        boolean alreadyInList = false;

        for (ServerDTO s : this.server.getServers()) {
            if (s != null && s.getUUID().equals(args[1])) {
                // server already in list
                alreadyInList = true;
                break;
            }
        }

        if (!alreadyInList) {
            this.server.addServer(new ServerDTO(args[1], this.serverSocket.getInetAddress().getHostAddress()));
        }

        try {
            sendResponse(String.format("SUCCESS %s %s", this.server.getNetworkUUID(), InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            Logger.error("Error while geting local ipv4address!");
            Logger.exception(e);
            sendResponse("ERROR Internal server error!");
        }
    }

    // Methode um den ALLOW Befehl an die HostServer zu senden
    // um den Upload eines Clients zu erlauben
    public boolean sendAllow(String address, String clientPublicKey, String ressourceUUID, String blockUUID) {
        String response;

        try (Socket tmpSocket = new Socket(address, 7000); DataOutputStream tmpOutputStream = new DataOutputStream(tmpSocket.getOutputStream()); DataInputStream tmpInputStream = new DataInputStream(tmpSocket.getInputStream());) {
            Logger.info(String.format("Sending ALLOW to %s", address));
            tmpOutputStream.writeUTF(String.format("ALLOW %s %s %s", clientPublicKey, ressourceUUID, blockUUID));
            tmpOutputStream.flush();

            response = tmpInputStream.readUTF();

            if (!response.contains("SUCCESS")) {
                Logger.error(String.format("ALLOW on [%s] failed!", address));
                return false;
            }

            Logger.success(String.format("ALLOW on [%s] was successfull!", address));
            tmpOutputStream.writeUTF("DISCONNECT");
            tmpOutputStream.flush();
        } catch (IOException e) {
            Logger.error("Error while sending allow command to " + address);
            return false;
        }

        return response.contains("SUCCESS");
    }
}
