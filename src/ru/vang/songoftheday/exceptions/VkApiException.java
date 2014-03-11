package ru.vang.songoftheday.exceptions;

import ru.vang.songoftheday.api.VkErrors;

public class VkApiException extends RuntimeException {

    private static final long serialVersionUID = -2584853781419948198L;

    private VkErrors mError;

    public VkApiException() {
        super();
    }

    public VkApiException(final String message) {
        super(message);
    }

    public VkApiException(final VkErrors error) {
        mError = error;
    }

    public VkErrors getVkError() {
        return mError;
    }
}
