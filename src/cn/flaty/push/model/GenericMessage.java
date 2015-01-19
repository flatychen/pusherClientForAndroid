package cn.flaty.push.model;

public class GenericMessage {
	
	
	public static int text = 100;
	
	private int commond;
	
	private String message;

	public int getCommond() {
		return commond;
	}

	
	public GenericMessage(String message) {
		super();
		commond = Integer.parseInt(new String(message.substring(0,4)));
		message = message.substring(4, message.length());
	}


	public void setCommond(int commond) {
		this.commond = commond;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
