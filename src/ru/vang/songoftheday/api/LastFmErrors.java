package ru.vang.songoftheday.api;

import ru.vang.songoftheday.R;

public enum LastFmErrors {
	INVALID_SERVICE(2, R.string.invalid_service),
	INVALID_METHOD(3, R.string.invalid_method),
	AUTHENTIFICATION_FAILED(4, R.string.last_fm_authentication_failed),
	INVALID_FORMAT(5, R.string.invalid_format),
	INVALID_PARAMETERS(6, R.string.invalid_parameters),
	INVALID_RESOURCE(7, R.string.invalid_resource),
	OPERATION_FAILED(8, R.string.operation_failed),
	INVALID_SESSION_KEY(9, R.string.invalid_session_key),
	INVALID_API_KEY(10, R.string.invalid_api_key),
	SERVICE_OFFLINE(11, R.string.service_offline),
	INVALID_METHOD_SIGNATURE(13, R.string.invalid_method_signature),
	TEMPORARY_ERROR(16, R.string.temporary_error),
	SUSPENDED_API_KEY(26, R.string.suspended_api_key),
	RATE_LIMIT_EXCEEDED(29, R.string.rate_limit_exceeded);

	private final int mCode;
	private final int mMessageId;

	LastFmErrors(final int code, final int messsageId) {
		mCode = code;
		mMessageId = messsageId;
	}

	public int getCode() {
		return mCode;
	}

	public int getMessageId() {
		return mMessageId;
	}

	public static LastFmErrors getValueByCode(final int code) {
		switch (code) {
		case 2:
			return INVALID_SERVICE;
		case 3:
			return INVALID_METHOD;
		case 4:
			return AUTHENTIFICATION_FAILED;
		case 5:
			return INVALID_FORMAT;
		case 6:
			return INVALID_PARAMETERS;
		case 7:
			return INVALID_RESOURCE;
		case 8:
			return OPERATION_FAILED;
		case 9:
			return INVALID_SESSION_KEY;
		case 10:
			return INVALID_API_KEY;
		case 11:
			return SERVICE_OFFLINE;
		case 13:
			return INVALID_METHOD_SIGNATURE;
		case 16:
			return TEMPORARY_ERROR;
		case 26:
			return SUSPENDED_API_KEY;
		case 29:
			return RATE_LIMIT_EXCEEDED;
		default:
			return OPERATION_FAILED;
		}
	}
}