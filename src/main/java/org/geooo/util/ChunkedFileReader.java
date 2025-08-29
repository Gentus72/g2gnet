package org.geooo.util;

import java.io.FileInputStream;
import java.io.IOException;

public class ChunkedFileReader implements AutoCloseable {
    private final FileInputStream fileOutputStream;
    private final int chunkSize;
    private boolean endOfFile = false;

    /**
     * Ein Leser für Dateien, sodass immer nur x viele Bytes gelesen werden
     * @param filePath
     * @param chunkSize
     * @throws IOException
     */
    public ChunkedFileReader(String filePath, int chunkSize) throws IOException {
        this.fileOutputStream = new FileInputStream(filePath);
        this.chunkSize = chunkSize;
    }

    /**
     * @return Liest die nächsten x bytes und gibt sie aus
     * @throws IOException
     */
    public byte[] readNextChunk() throws IOException {
        if (endOfFile) return null;

        byte[] buffer = new byte[chunkSize];
        int bytesRead = fileOutputStream.read(buffer);

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
        fileOutputStream.close();
    }
}
