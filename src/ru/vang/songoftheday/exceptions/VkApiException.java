package ru.vang.songoftheday.exceptions;

public class VkApiException extends UpdateException {	
	private static final long serialVersionUID = -2584853781419948198L;
	
	private transient int mCode;
	private transient String mUserMessage;

	public VkApiException() {
		super();
	}

	public VkApiException(final String message) {
		super();
		mUserMessage = message;
	}
	
	public VkApiException(final int code, final String message) {
		super();
		mCode = code;
		mUserMessage = message;
	}
	
	public int getCode() {
		return mCode;
	}
	
	public void setCode(final int code) {
		mCode = code;
	}
	
	public String getUserMessage() {
		return mUserMessage;
	}
	
	public void setUserMessage(final String message) {
		mUserMessage = message;
	}

}
