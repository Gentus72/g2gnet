package org.geooo;

import java.net.Socket;

import org.geooo.dto.ClientHandlerDTO;
import org.geooo.util.Logger;

public class ClientHandler extends ClientHandlerDTO {

    public ClientHandler(Server server, Socket serverSocket) {
        super(server, serverSocket);

        this.fallbackFunction = (String[] args) -> {
            Logger.info(String.format("Redirecting client %s to ccServer at: %s", this.client.getUUID(), this.server.ccServer.getAddress()));
            sendResponse(String.format("REDIRECT %s", server.ccServer.getAddress()));
        };
    }
}