import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class Client {

    public static void main(String[] args) {
        
        String serverAddress = "localhost";
        int port = 9999;
        String fileToSave = "..\\TestSite\\To\\TestFileDownloaded";

        System.out.println("Client : Connecting to " + serverAddress + ":" + port);

        try {
            //Connect to server
            SocketChannel serverSocket = SocketChannel.open();
            serverSocket.connect(new InetSocketAddress(serverAddress, port));
            System.out.println("Client : Connection established with server.");

            //Create file to save incoming data
            FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
            FileChannel fileChannel = fileOutputStream.getChannel();

            System.out.println("Client : Downloading file... Saving as " + fileToSave);

            //Zero Copy file transfer Network->Disk
            long startTime = System.currentTimeMillis();
            long bytesTransferred = fileChannel.transferFrom(serverSocket, 0, Long.MAX_VALUE);
            long endTime = System.currentTimeMillis();
            System.out.println("Client : Download complete! (Received " + bytesTransferred + " bytes)");
            System.out.println("Client : File download time: " + (endTime - startTime) + " ms");

            //Close everything
            fileChannel.close();
            fileOutputStream.close();
            serverSocket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}