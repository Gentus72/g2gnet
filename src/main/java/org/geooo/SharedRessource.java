package org.geooo;

import java.io.File;
import java.util.ArrayList;

public class SharedRessource extends Ressource {
    private ServerNetwork network;
    private ArrayList<ServerDTO> participatingServers;
    private RessourceSharingStrategy sharingStrategy = RessourceSharingStrategy.EVEN_DISTRIBUTION; // TODO implement strategies

    public SharedRessource(File sourceFile, String title) {
        super(sourceFile, title);
    }

    public SharedRessource(File G2GFile) {
        super(G2GFile);
    }

    private void saveBlockHelper(RessourceBlock block) {
        // TODO write block remotely
        // TODO write block and server info to networkFile
    }
}
