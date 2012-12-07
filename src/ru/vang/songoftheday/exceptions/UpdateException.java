package ru.vang.songoftheday.exceptions;

public class UpdateException extends RuntimeException {
	private static final long serialVersionUID = 5727366877094589582L;
	
	public UpdateException () {
		super();
	}
	
	public UpdateException(final String msg) {
		super(msg);		
	}
}
