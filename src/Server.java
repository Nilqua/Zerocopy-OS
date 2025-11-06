import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {

    public static void main(String[] args) {

        int port = 9999;
        String fileToSend = "file_to_send.txt"; // <-- ไฟล์ที่ท่านเจนายสร้างไว้

        System.out.println("เซิร์ฟเวอร์: กำลังเปิดบ้านรอที่ Port " + port);

        try {
            // 1. เปิดช่องทางรอรับ (เหมือนเปิดประตูบ้าน)
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));

            // 2. รอคนมาเคาะประตู (รับ Client)
            // (แบบง่าย ๆ คือรับแค่คนเดียวแล้วปิดเลย)
            SocketChannel clientSocket = serverSocket.accept();
            System.out.println("เซิร์ฟเวอร์: มีคนมาเชื่อมต่อแล้ว! จาก: " + clientSocket.getRemoteAddress());

            // 3. เปิดไฟล์ที่จะส่ง (เตรียมหยิบของ)
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            FileChannel fileChannel = fileInputStream.getChannel();
            
            long fileSize = fileChannel.size();
            System.out.println("เซิร์ฟเวอร์: กำลังจะส่งไฟล์ " + fileToSend + " ขนาด " + fileSize + " ไบต์");

            // 4. *** ท่าไม้ตาย Zero Copy! ***
            // สั่ง OS ให้ "โอน" จาก fileChannel (ดิสก์)
            // ไปยัง clientSocket (เน็ตเวิร์ก) โดยตรง
            // [อ้างอิงหลักการจากสไลด์ Week10 หน้า 14 และ 19]
            long bytesTransferred = fileChannel.transferTo(0, fileSize, clientSocket); // [cite: 1362]

            System.out.println("เซิร์ฟเวอร์: ส่งไฟล์เสร็จแล้ว! (ส่งไป " + bytesTransferred + " ไบต์)");

            // 5. ปิดทุกอย่าง
            fileChannel.close();
            fileInputStream.close();
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            System.out.println("เซิร์ฟเวอร์: โอ๊ย! เกิดปัญหาค่ะ!");
            e.printStackTrace();
        }
    }
}