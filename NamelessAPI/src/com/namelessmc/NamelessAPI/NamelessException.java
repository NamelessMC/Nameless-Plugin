package com.namelessmc.NamelessAPI;

public class NamelessException extends Exception {

	private static final long serialVersionUID = 6127505087276545949L;

	private String message;

	public NamelessException(String message) {
		this.message = message;
	}

	public NamelessException(Exception cause) {
		this.message = cause.getMessage();
		this.initCause(cause);
	}

	public String getMessage() {
		return message;
	}

}
