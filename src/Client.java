import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class Client {

    public static void main(String[] args) {
        
        String serverAddress = "localhost"; // "localhost" คือเครื่องตัวเอง
        int port = 9999;
        String fileToSave = "downloaded_file.txt"; // <-- ชื่อไฟล์ที่จะเซฟ

        System.out.println("ไคลเอนต์: กำลังเชื่อมต่อไปยัง " + serverAddress + ":" + port);

        try {
            // 1. เปิดช่องทางเพื่อเชื่อมต่อ (วิ่งไปเคาะประตู)
            SocketChannel serverSocket = SocketChannel.open();
            serverSocket.connect(new InetSocketAddress(serverAddress, port));
            System.out.println("ไคลเอนต์: เชื่อมต่อสำเร็จ!");

            // 2. สร้างไฟล์เปล่า ๆ ไว้รอรับ (เตรียมที่วางของ)
            FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
            FileChannel fileChannel = fileOutputStream.getChannel();

            System.out.println("ไคลเอนต์: กำลังดาวน์โหลดไฟล์... เซฟเป็น " + fileToSave);

            // 3. *** ท่าไม้ตาย Zero Copy! ***
            // สั่ง OS ให้ "รับ" จาก serverSocket (เน็ตเวิร์ก)
            // มาใส่ fileChannel (ดิสก์) โดยตรง
            // [อ้างอิงหลักการจากสไลด์ Week10 หน้า 19]
            // (แบบง่าย ๆ คือ รับมาเรื่อย ๆ จนกว่าเขาจะตัดสาย)
            long bytesTransferred = fileChannel.transferFrom(serverSocket, 0, Long.MAX_VALUE); // [cite: 1362]

            System.out.println("ไคลเอนต์: ดาวน์โหลดเสร็จแล้ว! (ได้มา " + bytesTransferred + " ไบต์)");

            // 4. ปิดทุกอย่าง
            fileChannel.close();
            fileOutputStream.close();
            serverSocket.close();

        } catch (IOException e) {
            System.out.println("ไคลเอนต์: โอ๊ย! เกิดปัญหาค่ะ!");
            e.printStackTrace();
        }
    }
}