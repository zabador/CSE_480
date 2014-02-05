package edu.oakland.cse480;

import javax.persistence.Entity;

@Entity
public class MyResult {

	private String value;

	public MyResult(String value) {
		this.value = value;
	}

	public String getVaule() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
