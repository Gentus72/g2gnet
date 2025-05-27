package org.geooo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.ChunkedFileReader;
import org.geooo.util.HashSum;
import org.geooo.util.Logger;

public class Ressource extends RessourceDTO {

    public static final int BLOCK_SIZE = 16 * 1024 * 1024; // 16 MiB
    public static final String PARENT_DIRECTORY = "res/"; // directory for all ressources

    private File sourceFile;
    private RessourceDistributionStrategy distributionStrategy; // TODO implement
    private RessourceBlock[] ressourceBlocks; // block amount equals ressourceBlocks.length

    /*
     * Mit diesem Konstruktor wird ein Ordner der Ressource mit Ressource-Blöcken und der Ressource-Datei erstellt.
     * Die Blöcke enthalten Teile der Datei.
     * Die Ressource-Datei enthält wichtige Metadaten, die auf Anfrage an den Client übergeben werden.
     */
    public Ressource(File sourceFile, String title, RessourceDistributionStrategy distributionStrategy) {
        super(title);

        Logger.info("Generating Ressource with title: " + title);

        this.sourceFile = sourceFile;
        this.distributionStrategy = distributionStrategy;

        // UUID generieren
        this.uuid = UUID.randomUUID().toString().replace("-", ""); // dash-less uuid
        Logger.info("UUID of Ressource is: " + this.uuid);

        // Ressource-Ordner erstellen
        new File(PARENT_DIRECTORY + this.uuid).mkdir();

        try {
            long sourceFileBytesAmount = Files.size(Path.of(sourceFile.getPath()));
            Logger.info("Creating ressource from source with " + sourceFileBytesAmount + " bytes!");

            this.totalHashSum = HashSum.fromFile(sourceFile);
            this.blockAmount = (int) Math.ceil((double) sourceFileBytesAmount / BLOCK_SIZE);

            this.ressourceBlocks = new RessourceBlock[this.blockAmount];

            if (blockAmount == 0) {
                Logger.error("Block amount is zero while creating ressource! This cannot be intended!");
            }

            // Blöcke erstellen
            Logger.info("Generating " + this.blockAmount + " blocks!");

            try (ChunkedFileReader chunkedReader = new ChunkedFileReader(sourceFile.getPath(), BLOCK_SIZE)) {
                for (int i = 0; i < this.blockAmount; i++) {
                    String blockUUID = UUID.randomUUID().toString().replace("-", "");
    
                    RessourceBlock newBlock = new RessourceBlock(blockUUID);
    
                    // read next 16 MiB and write to Block
                    newBlock.setData(chunkedReader.readNextChunk());
                    
                    newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));
                    newBlock.writeToServer(new ServerDTO());
    
                    ressourceBlocks[i] = newBlock;
                }
            }

            Logger.info(this.blockAmount + " blocks generated!");
        } catch (IOException e) {
            Logger.error("Error while getting Data from sourceFile: " + this.sourceFile.toPath().toString());
            Logger.exception(e);
        }

        // move sourceFile to ressourceDirectory
        this.sourceFile.renameTo(new File(PARENT_DIRECTORY + this.uuid, this.sourceFile.getName()));
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public RessourceBlock[] getRessourceBlocks() {
        return this.ressourceBlocks;
    }

    @Override
    public int getBlockAmount() {
        return this.blockAmount;
    }
}
