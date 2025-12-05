import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientSender {

    private static DatagramSocket socket;

    // Build a packet: [SEQ][DATA...]
    private static byte[] makePacket(int seq, byte[] data, int length) {
        byte[] packet = new byte[length + 1];
        packet[0] = (byte) seq;
        System.arraycopy(data, 0, packet, 1, length);
        return packet;
    }

    public static void main(String[] args) throws Exception {

        // Create socket bound to client's specified port
        socket = new DatagramSocket(8080);
        //Server socket
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int serverPort = 1230;
        socket.setSoTimeout(3000); // 3 sec timeout
        int chunkSize = 500; // copying chunkSize from FileHandler.java

        byte[] buf = new byte[8];

        // TODO: Send init DataPacket containing name of file
        String fileName = "test.txt";
        DataPacket initFileName = new DataPacket((short) 0, fileName.getBytes());
        DatagramPacket initPacket = new DatagramPacket(initFileName.toBytes(), initFileName.toBytes().length, serverAddress, serverPort);
        AckPacket fileSize = new AckPacket(0);
        DatagramPacket sizePacket = new DatagramPacket(fileSize.toBytes(), fileSize.toBytes().length);
        boolean waiting = true;
        do {
            try {
                socket.send(initPacket);
                System.out.println("Client: Sending initial packet with File Name");
                socket.receive(sizePacket);
                System.out.println("Client: Successfully received Total Size Packet");
                waiting = false;
            } catch (Exception e){
                System.out.println("Client: Timeout");
            }
        } while(waiting);
        fileSize = new AckPacket(sizePacket.getData());
        int totalChunks = fileSize.ackno;
        System.out.println("Client: Total Chunks " + totalChunks);
        socket.send(new DatagramPacket(sizePacket.getData(), sizePacket.getData().length, serverAddress, serverPort));
        RandomAccessFile newFile = new RandomAccessFile("copy_of_" + fileName, "rw");
        // newFile.setLength(chunkSize * totalChunks);
        byte[] byteBuf = new byte[totalChunks * chunkSize];


        // Commenting out Jon's code here for the moment. Don't think the below is necessary for our implementation
        // final int CHUNK_SIZE = 512;
        // byte[] buffer = new byte[CHUNK_SIZE];
        // int seq = 0; // sequence number: 0 or 1
        // FileInputStream fis = new FileInputStream(fileName);
        // int bytesRead;

        int pointer = 0;
        DataPacket incData = new DataPacket((short)chunkSize);
        // byte[] dataBuf = new byte[chunkSize * totalChunks];
        byte[] packetBuf = new byte[incData.toBytes().length];
        DatagramPacket incPacket = new DatagramPacket(packetBuf, incData.toBytes().length);
        while (pointer < totalChunks) {
            // loop for gather DataPackets

            waiting = true;
            do {
                try {
                    socket.receive(incPacket);
                    System.out.println("Client: Data Packet Received");
                    waiting = false;
                } catch (Exception e) {
                    System.out.println("Client: Timeout");
                }
            } while(waiting);
            incData = new DataPacket(incPacket.getData());
            byte[] dataBuf = incData.data;
            System.out.println(new String(dataBuf));
            newFile.write(dataBuf, 0, incData.len);
            System.out.println(incData.len);
            AckPacket ack = new AckPacket(pointer);
            DatagramPacket ackPacket = new DatagramPacket(ack.toBytes(), ack.toBytes().length, serverAddress, serverPort);
            socket.send(ackPacket);
            System.out.println("Client: Ack Sent");
            pointer++;

            // Commenting out Jon's code. Think there was a misunderstanding of the responsibilites of the Client class
            // Going to redo it all over.
            // boolean ackReceived = false;
            // while (!ackReceived) {

            //     DatagramPacket dp = new DatagramPacket(packet, packet.length, serverAddress, serverPort);
            //     socket.send(dp);
            //     System.out.println("Client: Sent packet seq=" + seq + " (" + bytesRead + " bytes)");

            //     try {
            //         // Waiting for ACK
            //         byte[] ackBuf = new byte[20];
            //         DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
            //         socket.receive(ackPacket);

            //         String ackStr = new String(ackBuf, 0, ackPacket.getLength()).trim();

            //         if (ackStr.equals("ACK" + seq)) {
            //             System.out.println("Client: Received correct " + ackStr);
            //             ackReceived = true;
            //             seq = 1 - seq; // toggle sequence
            //         } else {
            //             System.out.println(" Client: Incorrect ACK (" + ackStr + "), resending...");
            //         }

            //     } catch (SocketTimeoutException e) {
            //         System.out.println(" Client: Timeout! Resending packet...");
            //     }
            // }
        }

        System.out.println("Client: Finished");
        newFile.close();
        socket.close();
        // Send END signal
        // byte[] endMsg = "END".getBytes();
        // DatagramPacket endPacket = new DatagramPacket(endMsg, endMsg.length, serverAddress, serverPort);
        // socket.send(endPacket);
        // System.out.println("Client: Transmission complete.");
        // socket.close();
    }
}

