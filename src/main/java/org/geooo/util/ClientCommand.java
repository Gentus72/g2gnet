package org.geooo.util;

public enum ClientCommand implements Command {
    DISASSEMBLE(3), // DISASSEMBLE <filename> <ressourceTitle>
    REASSEMBLE(2), // REASSEMBLE <ressourceUUID>
    AUTOGET(2), // AUTOGET <ressourceUUID>
    AUTOUPLOAD(2),
    FULLUPLOAD(3), // FULLUPLOAD <networkUUID> <ressourceUUID>
    CONNECT(2),
    INFO(3),
    HELP(1),
    EXIT(1);

    int argsAmount; // amount with command itself

    ClientCommand(int argsAmount) {
        this.argsAmount = argsAmount;
    }

    @Override
    public boolean hasCorrectArgsAmount(int currentArgsAmount) {
        boolean tof = currentArgsAmount == argsAmount;
        if (!tof) {
            Logger.error(String.format("Wrong number of arguments supplied for %s! Should be %d", this, argsAmount));
        }

        return tof;
    }
}
