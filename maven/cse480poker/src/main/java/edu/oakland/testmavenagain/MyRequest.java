package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyRequest {
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
