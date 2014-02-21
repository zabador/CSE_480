package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyRequest {
	private String message;
	private String GCMmessage;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getGCMMessage() {
		return GCMmessage;
	}

	public void setGCMMessage(String GCMmessage) {
		this.GCMmessage = GCMmessage;
	}
}
