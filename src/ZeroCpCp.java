import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ZeroCpCp {
    public static void main(String[] args) throws IOException {
        String sourceFile = "source.txt";
        String destFile = "destination.txt";

        try (
            // 1. Get channels for the source and destination files
            FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
            FileChannel destChannel = new FileOutputStream(destFile).getChannel();
        ) {
            long sourceSize = sourceChannel.size();
            long position = 0;

            // 2. Loop until all bytes are transferred
            // !! This is critical: transferTo may not transfer all bytes at once
            while (position < sourceSize) {
                // 3. The zero-copy call
                long bytesTransferred = sourceChannel.transferTo(position, sourceSize - position, destChannel);
                
                // 4. Update the position
                position += bytesTransferred;
            }
        }
    }
}