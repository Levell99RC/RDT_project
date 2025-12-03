import java.io.IOException;

public class Main {

	public static void main(String[] args) {

		try {
			Sender s=new Sender("words1.txt");
			/*s.sendChunk((short) 0);
			s.sendChunk((short) 1);
			s.sendChunk((short) 2);
			
			*
			*/
			s.sendChunk((short) 3);
			s.sendChunk((short) 4);
			s.sendChunk((short) 5);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
