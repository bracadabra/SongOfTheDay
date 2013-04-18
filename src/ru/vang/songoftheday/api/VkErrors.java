package ru.vang.songoftheday.api;

import ru.vang.songoftheday.R;

public enum VkErrors {
	UNKNOWN_ERROR(1, R.string.unknown_error),
	APPLICATION_DISABLED(2,	R.string.application_disabled),
	INCORRECT_SIGNATURE(4, R.string.incorrect_signature),
	AUTHORIZATION_FAILED(5,	R.string.authorization_failed),
	TOO_MANY_REQUESTS(6, R.string.too_many_requests),
	PARAMETER_MISSING(100, R.string.parameter_missing),
	ACCESS_DENIED(201, R.string.access_denied);

	private int mCode;
	private int mMessageId;

	VkErrors(final int code, final int messageId) {
		mCode = code;
		mMessageId = messageId;
	}

	public int getCode() {
		return mCode;
	}

	public int getMessageId() {
		return mMessageId;
	}

	public static VkErrors getValueByCode(final int code) {
		switch (code) {
		case 1:
			return UNKNOWN_ERROR;
		case 2:
			return APPLICATION_DISABLED;
		case 4:
			return INCORRECT_SIGNATURE;
		case 5:
			return AUTHORIZATION_FAILED;
		case 6:
			return TOO_MANY_REQUESTS;
		case 100:
			return PARAMETER_MISSING;
		case 201:
			return ACCESS_DENIED;
		default:
			return UNKNOWN_ERROR;
		}
	}
}
