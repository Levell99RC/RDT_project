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
            InetAddress address = InetAddress.getByName("127.0.0.1");
            System.out.println("Server: Socket Creation successful");
            int clientPort = 8080;

            // Waiting for initial DataPacket with the FileName
            byte[] startBuf = new byte[512];
            DatagramPacket initPacket = new DatagramPacket(startBuf, 512);
            try {
                socket.receive(initPacket);
            } catch (Exception e) {
                System.out.println("Server: Did not receive initial datagram correctly.");
            }

            socket.setSoTimeout(3000);
            startBuf = initPacket.getData();
            DataPacket initData = new DataPacket(startBuf);
            String fileName = new String(initData.data);
            System.out.println("Server: File name :" + fileName);

            // Send AckPacket with total # of chunks to send
            FileHandler file = new FileHandler(fileName);
            int totalChunks = file.no_of_chunks;
            System.out.println("Server: Total Chunks to send: " + totalChunks);
            AckPacket sizePacket = new AckPacket(totalChunks);
            byte[] ackBuf = sizePacket.toBytes();
            DatagramPacket sentAck = new DatagramPacket(ackBuf, ackBuf.length, address, clientPort);
            DatagramPacket recAck = new DatagramPacket(ackBuf, ackBuf.length, address, clientPort);
            boolean wait = true;
            do {
                socket.send(sentAck);
                System.out.println("Server: File Chunk Size sent.");
                try {
                    socket.receive(recAck);
                    System.out.println("Server: Client Size Ack Received");
                    wait = false;
                } catch (Exception e) {
                    System.out.println("Server: Timeout");
                }
            } while(wait);

            // Begin sending the file
            System.out.println("Server: Begin sending file chunks.");
            short numChunk = 0; // seqNum   
            byte[] buf = new byte[8];
            while (numChunk < totalChunks) {
                byte[] data = file.readChunk(numChunk);
                DataPacket newDataPacket = new DataPacket(numChunk, data);
                byte[] packetBytes = newDataPacket.toBytes();
                DatagramPacket sentDatagram = new DatagramPacket(packetBytes, packetBytes.length, address, clientPort);
                // Timeout if no Ack received
                DatagramPacket recDatagram = new DatagramPacket(buf, 8);
                boolean loop = true;
                do {
                    try {
                        socket.send(sentDatagram);
                        System.out.println("Server: Sent packet " + (numChunk + 1) + " out of " + (totalChunks));
                        socket.receive(recDatagram);

                        // If Server sucessfully receives final AckPacket, it closes the connection.
                        // if (numChunk == totalChunks - 1) {
                        //     break;
                        // }
                        buf = recDatagram.getData();
                        AckPacket ack = new AckPacket(buf);
                        if (ack.ackno == numChunk) {
                            numChunk++;
                            loop = false;
                        }
                    } catch (Exception e) {
                        System.out.println("Server: Timeout");  
                    }
                } while (loop);
            }
            
            // Sending final Packet signalling file is done sending
            // AckPacket finalPacket = new AckPacket();
            // DatagramPacket finalDatagram = new DatagramPacket(finalPacket.toBytes(), 8);
            // boolean loop = true;
            // do {
            //     try {
            //         socket.send(finalDatagram);
            //         DatagramPacket finalCheck = new DatagramPacket(buf, 8);
            //         socket.receive(initPacket);
            //         loop = false;
            //     } catch (Exception e) {
                    
            //     }
            // } while (loop);
            System.out.println("File finished sending.");
            socket.close();
		} catch(Exception e){
			    e.printStackTrace();
		}
	}
}
