import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataPacket implements Packet {

	public short cksum; /* Optional bonus part */
	public short len;
	public int seqno;
	/* Data */
	public byte data[]; /* Not always 500 bytes, can be less */

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeShort(cksum);
		dos.writeShort(len);
		dos.writeInt(seqno);
		for (int i = 0; i < len; i++)
			dos.writeByte(data[i]);

		dos.flush();
		return bos.toByteArray();

	}

	public DataPacket(short l) {
		this.len=l;
		data=new byte[l];
	}
	
	public DataPacket(short seqno, byte []data) {
		this.len=(short) data.length;
		this.cksum=0;
		this.seqno=seqno;
		this.data=data;
	}

	public DataPacket(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream in = new DataInputStream(bis);
		cksum = in.readShort();
		len = in.readShort();
		seqno = in.readInt();
		data = new byte[len];
		for (int i = 0; i < len; i++)
			data[i] = in.readByte();

	}

	public String toString() {
		String txt="Data Packet seq"+seqno+ "  "+ len +"("+ new String(data)+")\n";
		return txt;
	}
	
	
}