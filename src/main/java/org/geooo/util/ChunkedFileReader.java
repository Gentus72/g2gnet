package org.geooo.util;

import java.io.File;

public class ChunkedFileReader implements AutoCloseable {
    public static File file;
    public static long chunkSize; // in bytes

    public static byte[] nextChunk() {
        return null;
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }
}
