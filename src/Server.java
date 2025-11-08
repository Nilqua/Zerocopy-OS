import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {

    public static void main(String[] args) {

        int port = 9999;
        String fileToSend = "..\\TestSite\\From\\TestFileSent";

        while(true){
            try {
            // Open the server socket channel
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Server : Listening on Address " + serverSocket.getLocalAddress());

            //wait for a client to connect
            SocketChannel clientSocket = serverSocket.accept();
            System.out.println("Server : Connection accepted from " + clientSocket.getRemoteAddress());

            //Prepare the file to send
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            FileChannel fileChannel = fileInputStream.getChannel();
            
            long fileSize = fileChannel.size();
            System.out.println("Server : Sending file size of " + fileSize + " bytes");

            //Zero Copy file transfer Disk->Network
            //keep track of bytes transferred
            long startTime = System.currentTimeMillis();
            long total = 0;
            while (total < fileSize){
                long bytesTransferred = fileChannel.transferTo(total, fileSize-total, clientSocket);
                total += bytesTransferred;
                System.out.print("\rServer : File transfer (Sent " + total + "/" + fileSize + " bytes)");
            }
            long endTime = System.currentTimeMillis();
            System.out.println("\nServer : File transfer time: " + (endTime - startTime) + " ms");

            //Close everything
            fileChannel.close();
            fileInputStream.close();
            clientSocket.close();
            serverSocket.close();

            } catch (Exception e) {
                System.out.println();
                System.out.println("Error : " + e);
                break;
            }
        }
    }
}