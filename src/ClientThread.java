import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;

public class ClientThread {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 9999;
        String fileToSave = "..\\TestSite\\To\\TestFileDownloaded";
        long fileSize = 10L * (1024 * 1024 * 1024);
        int threadCount = 4;

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileToSave, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();

            long partSize = fileSize / threadCount;
            Thread[] threads = new Thread[threadCount];

            long totalStartTime = System.currentTimeMillis();
            for (int i=0; i<threadCount; i++){
                long start = i * partSize;
                long end;
                if (i+1 == threadCount){
                    end = fileSize - 1;
                } else {
                    end = (i+1) * partSize - 1;
                }

                threads[i] = new Thread(new fileReceiver(start, end, serverAddress, port, fileChannel));
                threads[i].start();
                System.out.println("Client Thread : Started thread " + (i+1) + " for range " + start + " to " + end);

            }

            for (Thread t : threads) {
                t.join();
            }
            long totalEndTime = System.currentTimeMillis();
            System.out.println("Client Thread : Total download time: " + (totalEndTime - totalStartTime) + " ms");
        } catch (Exception e) {
            System.out.println("Error : " + e);
        }


    }

    public static class fileReceiver implements Runnable {
        private long start, end;
        private String serverAddress;
        private int port;
        private FileChannel fileChannel;

        public fileReceiver(long start, long end,String serverAddress, int port, FileChannel fileChannel) {
            this.start = start;
            this.end = end;
            this.serverAddress = serverAddress;
            this.port = port;
            this.fileChannel = fileChannel;
        }

        @Override
        public void run() {
            try {
                //connect to server
                SocketChannel serverSocket = SocketChannel.open(new InetSocketAddress(serverAddress, port));
                DataOutputStream out = new DataOutputStream(serverSocket.socket().getOutputStream());
                out.writeLong(start);
                out.writeLong(end);
                out.flush();
                
                System.out.println("Client Thread : Downloading file range " + start + " to " + end);
                fileChannel.transferFrom(serverSocket, start, end - start + 1);
                System.out.println("Client Thread : Download complete! Range: " + start + " - " + end);

            } catch (Exception e) {
                System.out.println("Error : " + e.getStackTrace());
            }
        }
    }
}