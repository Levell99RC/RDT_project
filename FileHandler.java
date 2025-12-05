
import java.io.IOException;
import java.io.RandomAccessFile;

class FileHandler {

	final int chunkSize = 500;

	String filetosend;
	RandomAccessFile file;
	int no_of_chunks = 0;

	public FileHandler(String filename) throws IOException {
		filetosend = new String(filename);
		file = new RandomAccessFile(filetosend, "r");
		no_of_chunks = ((int) file.length() / chunkSize) + 1;
	}

	byte[] readChunk(int chunk) {
		try {
			if( file.length()< chunk*chunkSize) return null;
			
			file.seek(chunk * chunkSize);
			int size=(int)Math.min(file.length()-chunk*chunkSize, chunkSize);
			byte buf[] = new byte[size];
			file.read(buf);
			
			return buf;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
