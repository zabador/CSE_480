package edu.oakland.testmavenagain;

import javax.persistence.Entity;

@Entity
public class MyResult {
	private String value;

	public MyResult(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
