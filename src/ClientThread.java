import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;

public class ClientThread {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 9999;
        String fileToSave = "..\\TestSite\\To\\TestFileDownloaded";
        int threadCount = 8;
        
        try {
            //connect to server
            SocketChannel serverSocket = SocketChannel.open();
            serverSocket.connect(new InetSocketAddress(serverAddress, port));
            System.out.println("Client : Connected to server at " + serverAddress + ":" + port);
            DataOutputStream out = new DataOutputStream(serverSocket.socket().getOutputStream());
            out.writeUTF("SIZE");

            //read file size from server
            DataInputStream in = new DataInputStream(serverSocket.socket().getInputStream());
            long fileSize = in.readLong();
            long partSize = fileSize / threadCount;
            Thread[] threads = new Thread[threadCount];
            System.out.println("Client : File size is " + fileSize + " bytes.");
            System.out.println("Client : Starting " + threadCount + " threads");

            //start threads to download
            long startTime = System.currentTimeMillis();
            for (int i=0; i<threadCount; i++) {
                long start = i * partSize;
                long end; 
                //last thread takes the remainder
                if (i == threadCount - 1) {
                    end = fileSize - 1;
                } else {
                    end = partSize + start - 1;
                }

                //start file receiver
                FileReceiver receiver = new FileReceiver(serverAddress, port, fileToSave, start, end);
                threads[i] = new Thread(receiver);
                threads[i].start();
                System.out.println("Client : Started thread for bytes " + start + " to " + end);
            }

            for (Thread thread : threads) {
                thread.join();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Client : File download completed in " + (endTime - startTime) + " ms.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FileReceiver implements Runnable {
        private String serverAddress;
        private int port;
        private String fileToSave;
        private long start;
        private long end;

        public FileReceiver(String serverAddress, int port, String fileToSave, long start, long end) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.fileToSave = fileToSave;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                //connect to server
                SocketChannel serverSocket = SocketChannel.open();
                serverSocket.connect(new InetSocketAddress(serverAddress, port));

                //send start and end positions to server
                DataOutputStream out = new DataOutputStream(serverSocket.socket().getOutputStream());
                out.writeUTF("GET");
                out.writeLong(start);
                out.writeLong(end);
                out.flush();
                System.out.println("Client Thread : Requested bytes " + start + " to " + end);

                //open random access file to write
                RandomAccessFile raf = new RandomAccessFile(fileToSave, "rw");
                FileChannel fc = raf.getChannel();
                fc.position(start);

                //zero copy network->disk
                long total = 0;
                long size = end - start + 1;
                while (total < size) {
                    long transferred = fc.transferFrom(serverSocket, start + total, size - total);
                    total += transferred;
                }
                System.out.println("Client Thread : Completed downloading bytes " + start + " to " + end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
