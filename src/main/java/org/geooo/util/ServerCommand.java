package org.geooo.util;

public enum ServerCommand {
    INFO(2), // INFO <NETWORK | RESSOURCE>
    REGISTER(3), // REGISTER <serverUUID> <serverAddress> // server sends this to the ccServer
    ALLOW(4), // ALLOW <publicKey> <blockUUID> // command from ccserver to allow a client to upload a block
    AUTH(3), // AUTH <encryptedBlockUUID> // command from client to check whether he can upload
    GETBLOCK(2), // GETBLOCK <blockUUID>
    PUT(2), // PUT <blockUUID>
    CLOSE(1); // CLOSE

    int argsAmount; // amount with command itself

    ServerCommand(int argsAmount) {
        this.argsAmount = argsAmount;
    }

    public int getArgsAmount() {
        return argsAmount;
    }

    public boolean hasCorrectArgsAmount(int currentArgsAmount) {
        boolean tof = currentArgsAmount == argsAmount;
        if (!tof) Logger.error(String.format("Wrong number of arguments supplied for %s! Should be %d", this, argsAmount));

        return tof;
    }
}
