package com.thangamfrm.simpleapiautomation;

public class APIAutomationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public APIAutomationException() {
	    super();
	}

	public APIAutomationException(String message) {
		super(message);
	}

	public APIAutomationException(String message, Throwable e) {
		super(message, e);
	}
}
