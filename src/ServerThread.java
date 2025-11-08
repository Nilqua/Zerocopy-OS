import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.DataInputStream;

public class ServerThread {
    public static void main(String[] args) {

        int port = 9999;
        String fileToSend = "..\\TestSite\\From\\TestFileSent";

        try {
            // Open the server socket channel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("Server : Listening on Address " + serverSocket.getLocalAddress());

        while(true){
            //wait for a client to connect
            SocketChannel clientSocket = serverSocket.accept();
            System.out.println("Server : Connection accepted from " + clientSocket.getRemoteAddress());

            //Start a new thread to handle the file sending
            Thread thread = new Thread(new fileSender(clientSocket, fileToSend));
            thread.start();
        }
        } catch (Exception e) {
            System.out.println("Error : " + e.getStackTrace());
        }
    }

    public static class fileSender implements Runnable {
        private SocketChannel clientSocket;
        private String fileToSend;

        public fileSender(SocketChannel clientSocket, String fileToSend) {
            this.clientSocket = clientSocket;
            this.fileToSend = fileToSend;
        }

        @Override
        public void run() {
            try {
            //Prepare the file to send
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            FileChannel fileChannel = fileInputStream.getChannel();

            //read what client range needs
            DataInputStream in = new DataInputStream(clientSocket.socket().getInputStream());
            long start = in.readLong();
            long end = in.readLong();
            long bytesToSend = end - start + 1;
            System.out.println("Server : Sending file range " + start + " to " + end + " (" + bytesToSend + " bytes)");

            //Zero Copy file transfer Disk->Network
            long total = 0;
            while (total < bytesToSend){
                long bytesSent = fileChannel.transferTo(start + total, bytesToSend - total, clientSocket);
                total += bytesSent;
                System.out.print("\rServer : File transfer (Sent " + total + " bytes)");
            }
            } catch (Exception e) {
                System.out.println();
                System.out.println("Error : " + e.getStackTrace());
            }
        }
    }
}