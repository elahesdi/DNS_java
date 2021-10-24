import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Response{
    private int ANCount, NSCount, RCode, QDCount,  ARCount;
	private byte[] response , ID;
    private QueryType queryType;
    private boolean noRecords = false;
    private boolean QR, AA, TC, RD, RA;
    private Record[] ansRecords , additionalRecords;
    boolean classIsNot1 = false ;


	public Response(byte[] response, int requestSize, QueryType queryType) {
		this.response = response;
		this.queryType = queryType;

        //check validation of response
        int index = 12;
        while (true) {
            if (this.response[index] == 0)
                break;
            index++;
        }
        byte[] queType = {this.response[index + 1], this.response[index + 2]};
        if (this.byteToType(queType) != this.queryType) {
            throw new RuntimeException("ERROR   Response query type does not match request query type");
        }

        headerParse();
        ansRecords = new Record[ANCount];
        int offSet = requestSize;
        for(int i = 0; i < ANCount; i ++){
        	ansRecords[i] = this.parseResponse(offSet);
        	offSet += ansRecords[i].getByteLength();
        }
        for(int i = 0; i < NSCount; i++){
        	offSet = offSet + parseResponse(offSet).getByteLength();
        }
        additionalRecords = new Record[ARCount];
        for(int i = 0; i < ARCount; i++){
        	additionalRecords[i] = this.parseResponse(offSet);
        	offSet = offSet + additionalRecords[i].getByteLength();
        }
        try {
            this.rCodeErrors();
        } catch(RuntimeException e){
        	noRecords = true;
        }
        if (!this.QR) {
            throw new RuntimeException("ERROR\tInvalid response from server: Message is not a response");
        }
    }

    public void showResponse() {
        System.out.println();
        if (this.ANCount <= 0  || noRecords) {
            System.out.println("response not found ");
            if (ARCount > 0) {
                System.out.println("Additional Section (" + ARCount + " answerRecords");
                for (int i = 0 ;i<additionalRecords.length ; i++){
                    additionalRecords[i].outputRecord();
                }
            }
            return;
        }
        System.out.println("answer Section : " + this.ANCount + " answerRecords");
        for (int i = 0 ; i< ansRecords.length ; i++){
            ansRecords[i].outputRecord();
        }
        System.out.println();
        if (ARCount > 0) {
            System.out.println("Additional Section (" + ARCount + " answerRecords");
            for (int i = 0 ;i<additionalRecords.length ; i++){
                additionalRecords[i].outputRecord();
            }
        }
    }

	public void rCodeErrors() {
            if(this.RCode == 1)
                throw new RuntimeException("Format error");
            else if(this.RCode == 2)
                throw new RuntimeException("Server failure");
            else if(this.RCode == 3)
                throw new RuntimeException();
            else if(this.RCode == 4)
                throw new RuntimeException("Not implemented");
            else if(this.RCode == 5)
                throw new RuntimeException("Refused");
    }

    public void headerParse(){
        this.RD = byteToBit(response[2], 0) == 1;
        this.RA = byteToBit(response[3], 7) == 1;
        this.QR = byteToBit(response[2], 7) == 1;
        this.AA = byteToBit(response[2], 2) == 1;
        this.TC = byteToBit(response[2], 1) == 1;
        this.RCode = response[3] & 0x0F;
        byte[] QDCount = { response[4], response[5] };
        ByteBuffer wrapped = ByteBuffer.wrap(QDCount);
        this.QDCount = wrapped.getShort();
        byte[] ANCount = { response[6], response[7] };
        wrapped = ByteBuffer.wrap(ANCount);
        this.ANCount = wrapped.getShort();
        byte[] NSCount = { response[8], response[9] };
        wrapped = ByteBuffer.wrap(NSCount);
        this.NSCount = wrapped.getShort();
        byte[] ARCount = { response[10], response[11] };
        wrapped = ByteBuffer.wrap(ARCount);
        this.ARCount = wrapped.getShort();
        byte[] ID = new byte[2];
        ID[0] = response[0];
        ID[1] = response[1];
        this.ID = ID;
    }

    public Record parseResponse(int index) {
        Record result = new Record();
        String domain = "";
        int countByte = index;
        rDataEntry domainResult = getDomainbyIndex(countByte);
        countByte = countByte + domainResult.getBytes();
        domain = domainResult.getDomain();
        result.setName(domain);
        byte[] typeOfAns = new byte[2]; //type
        typeOfAns[0] = response[countByte];
        typeOfAns[1] = response[countByte + 1];
        result.setQueryType(byteToType(typeOfAns));
        countByte = countByte + 2;
        byte[] classOfAns = new byte[2]; //class
        classOfAns[0] = response[countByte];
        classOfAns[1] = response[countByte + 1];
        if (classOfAns[1] != 1 && classOfAns[0] != 0) {
            throw new RuntimeException("class is not 1");
        }
            result.setQueryClass(classOfAns);
            countByte = countByte + 2;
            byte[] TTL = {response[countByte], response[countByte + 1], response[countByte + 2], response[countByte + 3]}; //TTL
            ByteBuffer w = ByteBuffer.wrap(TTL);
            result.setTimeToLive(w.getInt());
            countByte = countByte + 4;
            byte[] RDLength = {response[countByte], response[countByte + 1]}; //RDLength
            w = ByteBuffer.wrap(RDLength);
            int rdLength = w.getShort();
            result.setRdLength(rdLength);
            countByte = countByte + 2;
            if (result.getQueryType() == QueryType.CNAME)
                result.setDomain(parseCNAME(countByte));
            if (result.getQueryType() == QueryType.A)
                result.setDomain(parseA(countByte));
            if (result.getQueryType() == QueryType.NS)
                result.setDomain(parseNS(countByte));
            if (result.getQueryType() == QueryType.MX)
                result.setDomain(parseMX(countByte, result));
            result.setByteLength(countByte + rdLength - index);
            return result;

    }
    public String parseMX(int countByte, Record record) {
        byte[] mx= {this.response[countByte], this.response[countByte + 1]};
        ByteBuffer buffer = ByteBuffer.wrap(mx);
        record.setMxPreference(buffer.getShort());
        return getDomainbyIndex(countByte + 2).getDomain();
    }

    public int byteToBit(byte bytee, int pos) {
        return (bytee >> pos) & 1;
    }

    public QueryType byteToType(byte[] queType) {
        if (queType[0] == 0) {
            if (queType[1] == 1) {
                return QueryType.A;
            } else if (queType[1] == 2) {
                return QueryType.NS;
            } else if (queType[1] == 15) {
                return  QueryType.MX;
            } else if (queType[1] == 5) {
                return QueryType.CNAME;
            }else {
                return QueryType.Default;
            }
        } else {
            return QueryType.Default;
        }
    }

    public String parseA(int countByte) {
        String address = "";
        byte[] byteOfAddress= { response[countByte], response[countByte + 1], response[countByte + 2], response[countByte + 3] };
        try {
            InetAddress inetaddress = InetAddress.getByAddress(byteOfAddress);
            address = inetaddress.toString().substring(1);
        } catch (UnknownHostException e) {
          System.out.println(e);
        }
        return address;
    }

    public String parseNS( int countByte) {
		rDataEntry res = getDomainbyIndex(countByte);
		String nameServer = res.getDomain();
    	return nameServer;
    }

    public String parseCNAME( int countByte) {
		rDataEntry res = getDomainbyIndex(countByte);
		String cname = res.getDomain();
    	return cname;
    }

    public rDataEntry getDomainbyIndex(int index){
        int wordLength = response[index];
    	rDataEntry result = new rDataEntry();
    	String domain = "";
    	//start
    	boolean s = true;
    	int count = 0;
    	while(wordLength != 0){
			if (!s){
				domain =domain + ".";
			}
	    	if ((wordLength & 0xC0) == (int) 0xC0) {
	    		byte[] offset = { (byte) (response[index] & 0x3F), response[index + 1] };
	            ByteBuffer w = ByteBuffer.wrap(offset);
	            domain = domain + getDomainbyIndex(w.getShort()).getDomain();
                count = count+ 2;
	            index = index + 2;
	            wordLength = 0;
	    	}else{
	    	    // word from index
                String word = "";
                int wordSize = response[index];
                for(int i =0; i < wordSize; i++){
                    word += (char) response[index + i + 1];
                }

	    		domain += word;
	    		index += wordLength + 1;
	    		count += wordLength+ 1;
                wordLength = response[index];
	    	}
            s = false;
    	}
    	result.setDomain(domain);
    	result.setBytes(count);
    	return result;
    }

    public void setRDFalse(){
	    RD = false ;
    }
    public int getANCount() {
        return ANCount;
    }

    public Record[] getAdditionalRecords() {
        return additionalRecords;
    }
}
 /*class rDataEntry {
    private int bytes;
    private String domain;
    public int getBytes() {
        return bytes;
    }
    public void setBytes(int bytes) {
        this.bytes = bytes;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
}
*/