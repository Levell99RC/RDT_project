import java.io.IOException;

public interface Packet {
	public byte[] toBytes() throws IOException;
	
}
