	
public class Record {
	 private int timeToLive, rdLength, mxPreference;
	 private String name, domain;
	 private byte[] queryClass;
	 private QueryType queryType;
	 private int byteLength;


	 public void outputRecord() {
       if (queryType == QueryType.A)
                this.outputA();
		else if (queryType == QueryType.NS)
                this.outputNS();
		else if (queryType == QueryType.MX)
                this.outputMX();
		else if (queryType == QueryType.CNAME)
				this.outputCName();

	}

	public String getDomain() {
		return domain;
	}

	private void outputA() {
        System.out.println("IP:\t" + this.domain + "\t timeToLive:" + this.timeToLive );
    }

    private void outputNS() {
    	System.out.println("NS\t" + this.domain + "\t timeToLive:" + this.timeToLive );
    }

    private void outputMX() {
    	System.out.println("MX\t" + this.domain + "\t" + mxPreference + "\t timeToLive:" + this.timeToLive );
    }
    
    private void outputCName() {
		System.out.println("CNAME\t" + this.domain + "\t" + this.timeToLive );
    }
	
    public int getByteLength() {
		return byteLength;
	}
	
	public void setByteLength(int byteLength) {
		this.byteLength = byteLength;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setRdLength(int rdLength) {
		this.rdLength = rdLength;
	}

	public void setMxPreference(int mxPreference) {
		this.mxPreference = mxPreference;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setQueryClass(byte[] queryClass) {
		this.queryClass = queryClass;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}




}
