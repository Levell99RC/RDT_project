import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class ClientSender {

    private static DatagramSocket socket;

    // Build a packet: [SEQ][DATA...]
    private static byte[] makePacket(int seq, byte[] data) {
        byte[] packet = new byte[data.length + 1];
        packet[0] = (byte) seq;
        System.arraycopy(data, 0, packet, 1, data.length);
        return packet;
    }

    public static void main(String[] args) throws Exception {

        // Create socket bound to client's specified port
        socket = new DatagramSocket("8080");
        socket.setSoTimeout(3000); // 3 sec timeout

        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

        byte[] fileBytes = Files.readAllBytes(new File("DataPacket").toPath());

        final int CHUNK_SIZE = 512;
        int seq = 0; // sequence number: 0 or 1

        int offset = 0;
        while (offset < fileBytes.length) {

            int length = Math.min(CHUNK_SIZE, fileBytes.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(fileBytes, 0 + offset, chunk, 0, length);

            byte[] packet = makePacket(seq, chunk);

            boolean ackReceived = false;

            while (!ackReceived) {

                DatagramPacket dp = new DatagramPacket(packet, packet.length, serverAddress, "8080");
                socket.send(dp);
                System.out.println("Sent packet seq=" + seq + " (" + length + " bytes)");

                try {
                    // Waiting for ACK
                    byte[] ackBuf = new byte[20];
                    DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
                    socket.receive(ackPacket);

                    String ackStr = new String(ackBuf, 0, ackPacket.getLength()).trim();

                    if (ackStr.equals("ACK" + seq)) {
                        System.out.println("Received correct " + ackStr);
                        ackReceived = true;
                        seq = 1 - seq; // toggle sequence
                        offset += length;
                    } else {
                        System.out.println("Incorrect ACK (" + ackStr + "), resending...");
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout! Resending packet...");
                }
            }
        }

        // Send END signal
        byte[] endMsg = "END".getBytes();
        DatagramPacket endPacket = new DatagramPacket(endMsg, endMsg.length, serverAddress, "8080");
        socket.send(endPacket);

        System.out.println("Transmission complete.");
        socket.close();
    }
}
