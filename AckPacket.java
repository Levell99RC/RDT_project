
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AckPacket implements Packet {

	public short cksum; /* Optional bonus part */
	public short len;
	public int ackno;

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeShort(cksum);
		dos.writeShort(len);
		dos.writeInt(ackno);
		dos.flush();
		return bos.toByteArray();
	}

	public AckPacket() {
	}

	public AckPacket(int acknum) {
		cksum = 0;
		len = 0;
		ackno = acknum;
	}

	public AckPacket(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream in = new DataInputStream(bis);
		cksum = in.readShort();
		len = in.readShort();
		ackno = in.readInt();
	}
	public String toString() {
		String txt="Ack Packet no"+ackno+ "  "+ len +"\n";
		return txt;
	}
	
}
