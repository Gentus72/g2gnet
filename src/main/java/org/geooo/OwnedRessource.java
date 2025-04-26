package org.geooo;

import java.io.File;

public class OwnedRessource extends Ressource {

    private String serverUUID;

    public OwnedRessource(File sourceFile, String title) {
        super(sourceFile, title);
    }

    public OwnedRessource(File G2GFile) {
        super(G2GFile);
    }
}
