package com.base.util.json;

public class JSONParseErrorException extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5407894191655252611L;

	public JSONParseErrorException() {
		super();
	}
	
	public JSONParseErrorException(String str,int location) {
		super("json pasre error :"+str+" location :"+location);
	}
	
	public JSONParseErrorException(String message) {
		super(message);
	}
}
