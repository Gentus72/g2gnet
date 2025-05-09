package org.geooo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.geooo.util.HashSum;
import org.geooo.util.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class RessourceTest {
    public Ressource ressource;
    public File sourceFile;
    public String ressourceHashSumT = "98e4c860a44c648a4d1cc6f6d33a96b6778d609e6c71f5f6049872da1c493c34";
    public int blockAmountT = 2;

    @Before
    public void setup() {
        sourceFile = new File("res", "test.jpg");

        ressourceHashSumT = HashSum.fromFile(sourceFile);
    }

    @Test
    public void testBlockification() {
        ressource = new Ressource(sourceFile, "Test");

        assertEquals(blockAmountT, ressource.getBlockAmount());
        assertEquals(ressourceHashSumT, ressource.getTotalHashSum());

        File createdDirectory = new File("res/" + ressource.getUUID());
        try {
            Files.move(Path.of(createdDirectory.getPath() + "/test.jpg"), sourceFile.toPath());

            File[] filesToDelete = createdDirectory.listFiles();

            for (File file : filesToDelete) {
                Files.delete(file.toPath());
            }

            Files.delete(createdDirectory.toPath());
        } catch (IOException e) {
            Logger.error("Error while deleting temporary test ressource directory!");
            Logger.exception(e);
        }
    }

    @Test
    public void testReassembly() {
        File temporaryDestinationFile = new File("res/reassembled.jpg");
        ressource = new Ressource(new File("res/b5cb5724fcf5420aa07fe2a948ec11d3", "ressourceFile.g2g"));

        ressource.assembleSourceFile(temporaryDestinationFile);

        String destinationHashSum = HashSum.fromFile(temporaryDestinationFile);

        assertEquals(ressourceHashSumT, ressource.getTotalHashSum());
        assertEquals(ressourceHashSumT, destinationHashSum);

        try {
            Files.delete(temporaryDestinationFile.toPath());
        } catch (IOException e) {
            Logger.error("Error while deleting reassembled file from ressource test!");
            Logger.exception(e);
        }
    }
}
