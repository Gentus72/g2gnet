    package org.geooo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.geooo.util.Logger;

public class CCClientHandler extends ClientHandler {

    public CCClientHandler(Socket serverSocket, Server server) {
        super(serverSocket, server);
    }

    @Override
    public void run() {
        Logger.info("A client connected!");
        Logger.info("Now handling " + this.server.clients.size() + " clients!");

        try (DataInputStream inputStream = new DataInputStream(serverSocket.getInputStream()); DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());) {
            while (true) {
                String clientInput = inputStream.readUTF();
                ServerCommand command = getCommandFromClientInput(clientInput);
                String[] arguments = clientInput.split(" ");

                switch (command) {
                    case INFO -> {
                        Logger.info("Sending server information to client!");
                        outputStream.writeUTF("INFO sending server information!");
                    }
                    case GET -> {
                        if (arguments.length <= 1) {
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
}
