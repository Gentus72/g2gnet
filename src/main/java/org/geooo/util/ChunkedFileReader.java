package org.geooo.util;

import java.io.FileInputStream;
import java.io.IOException;

public class ChunkedFileReader implements AutoCloseable {
    private final FileInputStream fis;
    private final int chunkSize;
    private boolean endOfFile = false;

    public ChunkedFileReader(String filePath, int chunkSize) throws IOException {
        this.fis = new FileInputStream(filePath);
        this.chunkSize = chunkSize;
    }

    public byte[] readNextChunk() throws IOException {
        if (endOfFile) return null;

        byte[] buffer = new byte[chunkSize];
        int bytesRead = fis.read(buffer);

        if (bytesRead == -1) {
            endOfFile = true;
            return null;
        }

        if (bytesRead < chunkSize) {
            byte[] actualBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, actualBytes, 0, bytesRead);
            return actualBytes;
        }

        return buffer;
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }
}
