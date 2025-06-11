package org.geooo.util;

public enum ServerCommand implements Command {
    INFO(2), // INFO <NETWORK | RESSOURCE> <? ressourceUUID>
    STATUS(1),
    REGISTER(3), // REGISTER <serverUUID> <serverAddress> // server sends this to the ccServer
    ALLOW(3), // ALLOW <publicKey> <blockUUID> // command from ccserver to allow a client to upload a block
    AUTH(2), // AUTH <encryptedBlockUUID> // command from client to check whether he can upload
    // AUTH <ressourceUUID> // command from client to ccServer to authorize ressource
    GETBLOCK(3), // GETBLOCK <ressourceUUID> <blockUUID>
    PUT(2), // PUT <blockUUID>
    DISCONNECT(1); // CLOSE

    int argsAmount; // amount with command itself

    ServerCommand(int argsAmount) {
        this.argsAmount = argsAmount;
    }

    public int getArgsAmount() {
        return argsAmount;
    }

    @Override
    public boolean hasCorrectArgsAmount(int currentArgsAmount) {
        boolean tof = currentArgsAmount >= argsAmount;
        if (!tof) {
            Logger.error(String.format("Wrong number of arguments supplied for %s! Should be %d", this, argsAmount));
        }

        return tof;
    }
}
