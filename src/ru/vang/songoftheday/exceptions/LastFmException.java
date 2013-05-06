package ru.vang.songoftheday.exceptions;

import ru.vang.songoftheday.api.LastFmErrors;

public class LastFmException extends RuntimeException {
	private static final long serialVersionUID = 5727366877094589582L;
	private LastFmErrors mError;

	public LastFmException() {
		super();
	}

	public LastFmException(final String msg) {
		super(msg);
	}

	public LastFmException(final LastFmErrors error) {
		super();
		mError = error;
	}

	public LastFmErrors getLastFmError() {
		return mError;
	}
}
