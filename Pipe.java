package stop_wait;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Pipe {

	InetAddress client_ip;
	InetAddress server_ip;
	int client_port;
	int server_port;
	String filename;
	int port_no;
	double prob;
	long seed;

	DatagramSocket client_socket;
	DatagramSocket server_socket;

	Pipe() throws FileNotFoundException, UnknownHostException {

		JsonReader jsonReader = Json.createReader(new FileReader("pipe.json"));
		JsonObject obj = jsonReader.readObject();

		String client_ip_tmp = obj.getString("client_ip");
		String server_ip_tmp = obj.getString("server_ip");
		client_port = obj.getInt("client_port");
		server_port = obj.getInt("server_port");
		port_no = obj.getInt("port_no");

		prob = obj.getJsonNumber("prob").doubleValue();
		seed = obj.getJsonNumber("seed").longValue();

		jsonReader.close();
		client_ip = InetAddress.getByName(client_ip_tmp);
		server_ip = InetAddress.getByName(server_ip_tmp);
	}

	void run() throws IOException {

		DatagramSocket socket = new DatagramSocket(port_no);
		Random r = new Random(seed);

		byte[] buf = new byte[1000];
		for (;;) {
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			socket.receive(p);
			// System.out.println("Received a packet");

			InetAddress dst = p.getAddress();
			int port = p.getPort();

			if (dst.equals(client_ip) && port == client_port) {

				// forward the packet to the server
				p.setAddress(server_ip);
				p.setPort(server_port);

				dst = p.getAddress();
				port = p.getPort();

				System.out.println("received from client," + dst + "  " + port + "  " + "   forward to server");
				socket.send(p);
			} else if (dst.equals(server_ip) && port == server_port) {

				double pr = r.nextDouble();
				System.out.println(">>>" + pr + "  " + prob);
				// forward the packet to the server
				if (pr > prob) {
					p.setAddress(client_ip);
					p.setPort(client_port);
				
					dst = p.getAddress();
					port = p.getPort();
					// System.out.println(dst + " " + port);

					System.out.println("received from server," + dst + "  " + port + "  " + "   forward to client");

					socket.send(p);
				} else {
					System.out.println("received from server, dropped");

				}
			} else {
				System.out.println("--------------");
			}
		}

	}

	public static void main(String[] args) throws IOException {
		if (args.length != 0) {
			System.out.println("Usage: java Pipe\n The program using configuration file pipe.json");

			return;
		}

		Pipe p = new Pipe();
		p.run();
	}
}
