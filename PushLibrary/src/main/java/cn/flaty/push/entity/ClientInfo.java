package cn.flaty.push.entity;


public class ClientInfo {
	
	public static String APPKEY = "APPKEY";
	
	private String appKey;

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	private String did;

	private int appVer;

	private String os;

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	public int getAppVer() {
		return appVer;
	}

	public void setAppVer(int appVer) {
		this.appVer = appVer;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}


	@Override
	public String toString() {
		return "ClientInfo [did=" + did + ", appVer=" + appVer + ", os=" + os
				+ "]";
	}

	public String tobabambaJson(){
		StringBuilder  sb = new StringBuilder();
		sb.append("{").
		append("'did':").append(did).
		append("'appVer':").append(appVer).
		append("'os':").append(os);
		sb.append("}");
		return sb.toString();
	}
	
	
	
}
