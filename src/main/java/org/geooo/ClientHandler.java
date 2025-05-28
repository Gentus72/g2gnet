package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import org.geooo.util.Logger;
import org.geooo.dto.ClientDTO;

public class ClientHandler extends Thread {
    Socket serverSocket;
    ClientDTO client;
    Server server;

    public ClientHandler(Socket serverSocket, Server server) {
        this.serverSocket = serverSocket;
        this.server = server;

        this.client = new ClientDTO(UUID.randomUUID().toString().replace("-", ""));
        Logger.info(String.format("New client connected with UUID: %s", client.getUUID()));
        this.server.clients.add(this.client);
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream()); DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            String clientInput = inputStream.readUTF();
            ServerCommand command = getCommandFromClientInput(clientInput);

            if (command.equals(ServerCommand.GETBLOCK)) {
                // handle download
            } else {
                Logger.info(String.format("Redirecting client %s to ccServer at: %s", this.client.getUUID(), this.server.ccServer.getAddress()));
                outputStream.writeUTF(String.format("REDIRECT %s", server.ccServer.getAddress()));
            }
        } catch (IOException e) {
            Logger.error("Error while handling client request!");
            Logger.exception(e);
        }
    }

    public ServerCommand getCommandFromClientInput(String clientInput) {
        return ServerCommand.valueOf(clientInput.split(" ")[0]);
    }
}
