import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;

public class ServerThread {
    public static void main(String[] args) {
        int port = 9999;
        String fileToSend = "..\\TestSite\\From\\TestFileSent";
        try {
            //open server socket channel
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Server : Listening on Address " + serverSocket.getLocalAddress());
            
            while(true){
                //wait for a client to connect
                SocketChannel clientSocket = serverSocket.accept();
                System.out.println("Server : Accepted Connection from " + clientSocket.getRemoteAddress());
                
                //client connect request file size?
                DataInputStream in = new DataInputStream(clientSocket.socket().getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.socket().getOutputStream());
                String cmd = in.readUTF();
                if(cmd.equals("SIZE")){
                    System.out.println("Server : Client requested file size.");
                    FileInputStream fis = new FileInputStream(fileToSend);
                    FileChannel fc  = fis.getChannel();
                    long fileSize = fc.size();
                    System.out.println("Server : Sending file size of " + fileSize + " bytes");
                    out.writeLong(fileSize);
                    out.flush();
                } else if (cmd.equals("GET")) {
                    //start a new thread to handle the file sending
                    FileSender sender = new FileSender(clientSocket, fileToSend);
                    Thread thread = new Thread(sender);
                    thread.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FileSender implements Runnable {
        private SocketChannel clientSocket;
        private String fileToSend;

        public FileSender(SocketChannel clientSocket, String fileToSend) {
            this.clientSocket = clientSocket;
            this.fileToSend = fileToSend;
        }

        @Override
        public void run() {
            try {
                //open file channel
                FileInputStream fis = new FileInputStream(fileToSend);
                FileChannel fc = fis.getChannel();

                //read start and end positions from client
                DataInputStream in = new DataInputStream(clientSocket.socket().getInputStream());
                long start = in.readLong();
                long end = in.readLong();
                long size = end - start + 1;
                System.out.println("Server Thread : Client requested " + start + " to " + end);

                //zero copy transfer
                System.out.println("Server Thread : Start sending from " + start + " to " + end);
                long total = 0;
                while (total < size) {
                    long transferred = fc.transferTo(start + total, size - total, clientSocket);
                    total += transferred; 
                }
                System.out.println("Server Thread : Completed sending bytes " + start + " to " + end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
