package files;
import com.github.luben.zstd.ZstdInputStream;

import java.io.IOException;
import java.io.InputStream;

public class SkipableZstdInputStream extends ZstdInputStream {

    public SkipableZstdInputStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * #define ZSTD_BLOCKSIZELOG_MAX 17
     * #define ZSTD_BLOCKSIZE_MAX   (1<<ZSTD_BLOCKSIZELOG_MAX)
     * Skips n bytes of input.
     * @param numBytes the number of bytes to skip
     * @return  the actual number of bytes skipped.
     * @exception IOException If an I/O error has occurred.
     */
    public long skip(long numBytes) throws IOException {
        if (numBytes <= 0) {
            return 0;
        }
        long n = numBytes;
        int bufferLen = (int) Math.min(1 << 17, n);
        byte data[] = new byte[bufferLen];
        while (n > 0) {
            int r = read(data, 0, (int) Math.min((long) bufferLen, n));
            if (r < 0) {
                break;
            }
            n -= r;
        }
        return numBytes - n;
    }
}
