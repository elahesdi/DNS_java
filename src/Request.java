import java.nio.ByteBuffer;
import java.util.Random;


public class Request {

	private String domain;
	private QueryType type;
	public Request(String domain, QueryType type){
		this.domain = domain;
		this.type = type;
	}

	public byte[] getRequest(){
		int qNameLength = getQNameLength();
		ByteBuffer request = ByteBuffer.allocate(12 + 5 + qNameLength);
		request.put(createRequestHeader());
		request.put(createQuestionHeader(qNameLength));
		return request.array();
	}

	private byte[] createRequestHeader(){
		ByteBuffer header = ByteBuffer.allocate(12);
		byte[] randomID = new byte[2];
		new Random().nextBytes(randomID);
		header.put(randomID);
		header.put((byte)0x01);
		header.put((byte)0x00);
		header.put((byte)0x00);
		header.put((byte)0x01);
		return header.array();
	}

	private int getQNameLength(){
		int byteLength = 0;
		String[] items = domain.split("\\.");
		for(int i=0; i < items.length; i ++){
			byteLength += items[i].length() + 1;
		}
		return byteLength;
	}

	private byte[] createQuestionHeader(int qNameLength){
		ByteBuffer question = ByteBuffer.allocate(qNameLength+5);
		String[] items = domain.split("\\.");
		for(int i=0; i < items.length; i ++){
			question.put((byte) items[i].length());
			for (int j = 0; j < items[i].length(); j++){
				question.put((byte) ((int) items[i].charAt(j)));

			}
		}

		question.put((byte) 0x00);
		String s = "000" + hexFromQueryType(type);
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		question.put(data);
		question.put((byte) 0x00);
		question.put((byte) 0x0001);

		return question.array();
	}

	private char hexFromQueryType(QueryType type){
		if (type == QueryType.A) {
			return '1';
		} else if (type == QueryType.NS) {
			return '2';
		} else {
			return 'F';
		}
	}

}
