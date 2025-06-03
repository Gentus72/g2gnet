package org.geooo.util;

public enum ClientCommand {
    DISASSEMBLE(2),
    REASSEMBLE(2),
    CONNECT(2),
    HELP(1);

    int argsAmount; // amount with command itself

    ClientCommand(int argsAmount) {
        this.argsAmount = argsAmount;
    }

    public boolean hasCorrectArgsAmount(int currentArgsAmount) {
        boolean tof = currentArgsAmount == argsAmount;
        if (!tof) Logger.error(String.format("Wrong number of arguments supplied for %s! Should be %d", this, argsAmount));

        return tof;
    }
}
