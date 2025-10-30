import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class JioChannel {
    public static void main(String[] args) {
        JioChannel channel = new JioChannel();
        String From = "D:\\Nilqua\\Study Y3\\OS\\ZeroCopy\\TestSite\\From\\TestFile";
        String To = "D:\\Nilqua\\Study Y3\\OS\\ZeroCopy\\TestSite\\To\\TestFile";

        try {
            long start, end;
            System.out.println("Performance Comparison:");
            System.out.print("Normal Copy : ");
            start = System.currentTimeMillis();
            channel.copy(From, To);
            end = System.currentTimeMillis();
            long timeNormal = end - start;
            System.out.print(timeNormal + " millisecond");
            System.err.println();

            System.out.print("Zero Copy : ");
            start = System.currentTimeMillis();
            channel.zeroCopy(From, To);
            end = System.currentTimeMillis();
            long timeZero = end - start;
            System.out.print(timeZero + " millisecond");
            System.out.println();
            System.out.println("Difference : " + (timeNormal - timeZero) + " millisecond");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copy(String from, String to) throws IOException {
        byte[] data = new byte[8 * 1024];
        FileInputStream fis = null;
        FileOutputStream fos = null;
        long bytesToCopy = new File(from).length();
        long bytesCopied = 0;
        try {
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            while (bytesCopied < bytesToCopy) {
                fis.read(data);
                fos.write(data);
                bytesCopied += data.length;
            }
            fos.flush();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public void zeroCopy(String from, String to) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();
            source.transferTo(0, source.size(), destination);
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
