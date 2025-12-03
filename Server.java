import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {
    // Receives AckPacket from Sender (through Pipe)
    // Sends DataPacket in return (through Pipe)
    // Waits for Ack OR Timeout
    // Ack's are 8 bytes long. Data is 12-512
    // FileHandler 

    /*
        Current solution:
        -hardcoded IP address, portNum, and fileName
        -Sent to dest WITHOUT pipe
        TODO:
        - Utilize Pipe
        - thread: pipe > server > client
        - client initiates with DataPacket with file name and ackno = 0
        - how to determine file is done sending
     */
    public static void main(String []args){

        // Ensures new Socket can be made
		try (DatagramSocket socket=new DatagramSocket(1230)) {
            // Waiting for initial with the FileName
            byte[] startBuf = new byte[512];
            DatagramPacket initPacket = new DatagramPacket(startBuf, 512);
            try {
                socket.receive(initPacket);
            } catch (Exception e) {
                System.out.println("Did not receive initial datagram correctly.");
            }
            startBuf = initPacket.getData();
            DataPacket initData = new DataPacket(startBuf);
            String fileName = new String(initData.data);
            System.out.println(fileName);

            // Begin sending the file
            socket.setSoTimeout(3000);
            InetAddress address = InetAddress.getByName("127.0.0.1");
            FileHandler file = new FileHandler(fileName);
            int numChunk = 0;
            short seqNum = 1;
            byte[] buf = new byte[8];
            while (numChunk < file.no_of_chunks) {
                byte[] data = file.readChunk(numChunk);
                DataPacket newDataPacket = new DataPacket(seqNum, data);
                byte[] packetBytes = newDataPacket.toBytes();
                DatagramPacket sentDatagram = new DatagramPacket(packetBytes, packetBytes.length, address, 6000);
                // Timeout if no Ack received
                DatagramPacket recDatagram = new DatagramPacket(buf, 8);
                boolean loop = true;
                do {
                    try {
                        socket.send(sentDatagram);
                        socket.receive(recDatagram);
                        buf = recDatagram.getData();
                        AckPacket ack = new AckPacket(buf);
                        if (ack.ackno == seqNum) {
                            seqNum++;
                            numChunk++;
                            loop = false;
                        }
                    } catch (Exception e) {   
                    }
                } while (loop);
            }
            
            // Sending final Packet signalling file is done sending
            AckPacket finalPacket = new AckPacket();
            DatagramPacket finalDatagram = new DatagramPacket(finalPacket.toBytes(), 8);
            boolean loop = true;
            do {
                try {
                    socket.send(finalDatagram);
                    DatagramPacket finalCheck = new DatagramPacket(buf, 8);
                    socket.receive(initPacket);
                    loop = false;
                } catch (Exception e) {
                    
                }
            } while (loop);
            System.out.println("File finished sending.");
		} catch(Exception e){
			    e.printStackTrace();
		}
	}
}
