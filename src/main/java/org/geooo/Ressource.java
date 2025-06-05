package org.geooo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import org.geooo.dto.RessourceDTO;
import org.geooo.dto.ServerDTO;
import org.geooo.util.ChunkedFileReader;
import org.geooo.util.G2GUUID;
import org.geooo.util.HashSum;
import org.geooo.util.Logger;
import org.geooo.util.RessourceDistributionStrategy;

public class Ressource extends RessourceDTO {

    public static final int BLOCK_SIZE = 16 * 1024 * 1024; // 16 MiB
    public static final String PARENT_DIRECTORY = "res/"; // directory for all ressources

    private File sourceFile;
    @SuppressWarnings("unused")
    private RessourceDistributionStrategy distributionStrategy; // TODO implement
    private HashMap<RessourceBlock, String> blockLocations;

    public Ressource(File sourceFile, String title, RessourceDistributionStrategy distributionStrategy) {
        super(title);

        Logger.info("Generating Ressource with title: " + title);

        this.sourceFile = sourceFile;
        this.distributionStrategy = distributionStrategy;
        this.blockLocations = new HashMap<>();

        // UUID generieren
        this.uuid = UUID.randomUUID().toString().replace("-", ""); // dash-less uuid
        Logger.info("UUID of Ressource is: " + this.uuid);

        try {
            long sourceFileBytesAmount = Files.size(Path.of(sourceFile.getPath()));
            Logger.info("Creating ressource from source with " + sourceFileBytesAmount + " bytes!");

            this.totalHashSum = HashSum.fromFile(sourceFile);
            this.blockAmount = (int) Math.ceil((double) sourceFileBytesAmount / BLOCK_SIZE);

            if (blockAmount == 0) {
                Logger.error("Block amount is zero while creating ressource! This cannot be intended!");
            }

            // Blöcke erstellen
            Logger.info("Generating " + this.blockAmount + " blocks!");

            // chunked reader to not load everything into memory
            try (ChunkedFileReader chunkedReader = new ChunkedFileReader(sourceFile.getPath(), BLOCK_SIZE)) {
                for (int i = 0; i < this.blockAmount; i++) {
                    String blockUUID = G2GUUID.getRandomUUID();

                    RessourceBlock newBlock = new RessourceBlock(blockUUID);

                    // read next 16 MiB and write to Block
                    newBlock.setData(chunkedReader.readNextChunk());
                    newBlock.setSequenceID(i);

                    newBlock.setHashSum(HashSum.fromBytes(newBlock.getData()));

                    ServerDTO destinationServer = new ServerDTO("123", "localhost");
                    newBlock.writeToServer(destinationServer);
                    blockLocations.put(newBlock, destinationServer.getAddress());
                }
            }

            Logger.info(this.blockAmount + " blocks generated!");
        } catch (IOException e) {
            Logger.error("Error while getting Data from sourceFile: " + this.sourceFile.toPath().toString());
            Logger.exception(e);
        }
    }

    // assuming block hashsums have been verified upon download
    public static void reassembleSourceFile(File G2GFile, File[] blocks, String destinationDirectory) {
        String hashSum = null;
        String sourceFileName = null;
        int blockAmount = -1;
        File outputFile;
        HashMap<Integer, String> blockSequence = new HashMap<>();

        if (G2GFile == null || blocks == null || blocks.length == 0) {
            Logger.error("Some files supplied for reassembly didn't exist!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(G2GFile))) {
            String nextLine = reader.readLine();

            while (nextLine != null) {
                // if its a block
                if (!nextLine.contains(":")) {
                    String[] elements = nextLine.split(",");

                    blockSequence.put(Integer.valueOf(elements[3]), elements[0]);
                } else {
                    String value = nextLine.split(" ")[1];

                    switch (nextLine.substring(0, 5)) {
                        case "HashS" -> {
                            hashSum = value;
                        }
                        case "Amoun" -> {
                            blockAmount = Integer.parseInt(value);
                        }
                        case "Sourc" -> {
                            sourceFileName = value;
                        }
                    }
                }

                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            Logger.error("Error while reading G2GFile!");
            Logger.exception(e);
        }

        if (hashSum == null || sourceFileName == null || blockAmount == -1) {
            Logger.error("Some variables couldn't be extracted from the ressourcefile!");
            return;
        }

        if (blockAmount != blocks.length) {
            Logger.error("Mismatch between length of blockFile-array and blockamount from ressourcefile! Continuing...");
        }

        outputFile = new File(destinationDirectory, sourceFileName);

        try (FileOutputStream writer = new FileOutputStream(outputFile)) {
            Logger.info(String.format("Reassembling %d blocks!", blockAmount));

            for (int i = 0; i < blockAmount; i++) {
                String blockUUID = blockSequence.get(i);
                File blockFile = new File(blocks[0].getParent(), blockUUID + ".g2gblock");

                writer.write(Files.readAllBytes(Path.of(blockFile.getPath())));
            }
        } catch (IOException e) {
            Logger.error("Error while reading G2GFile!");
            Logger.exception(e);
        }

        if (!hashSum.equals(HashSum.fromFile(outputFile))) {
            Logger.error("HashSum mismatch between ressourcefile and reassembled file!");
        }

        Logger.info("Successfully reassembled sourcefile!");
    }

    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public int getBlockAmount() {
        return this.blockAmount;
    }

    public HashMap<RessourceBlock, String> getBlockLocations() {
        return this.blockLocations;
    }
}
