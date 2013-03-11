package ru.vang.songoftheday.model;

import ru.vang.songoftheday.api.Track;
import ru.vang.songoftheday.api.VkTrack;

public class WidgetUpdateInfo {

	private String mOriginalTitle;
	private String mOriginalArtist;
	private VkTrack mVkTrack;
	private Track mLastFmTrack;
	private Status mStatus = Status.SUCCESS;
	private boolean mHasVkAccount;

	public String getOriginalTitle() {
		return mOriginalTitle;
	}

	public void setOriginalTitle(String originalTitle) {
		this.mOriginalTitle = originalTitle;
	}

	public String getOriginalArtist() {
		return mOriginalArtist;
	}

	public void setOriginalArtist(String originalArtist) {
		this.mOriginalArtist = originalArtist;
	}

	public VkTrack getVkTrack() {
		return mVkTrack;
	}

	public void setVkTrack(VkTrack vkTrack) {
		this.mVkTrack = vkTrack;
	}

	public void setPath(final String path) {
		mVkTrack.setPath(path);
	}

	public void setStatus(final Status status) {
		mStatus = status;
	}

	public Status getStatus() {
		return mStatus;
	}

	public boolean isCancelled() {
		return mStatus == Status.CANCELLED;
	}

	public void setTrack(final Track track) {
		mLastFmTrack = track;
	}

	public Track getTrack() {
		return mLastFmTrack;
	}

	public String getTitle() {
		return mLastFmTrack.getTitle();
	}

	public String getArtist() {
		return mLastFmTrack.getArtist();
	}

	public void setHasVkAccount(final boolean hasVkAccount) {
		mHasVkAccount = hasVkAccount;
	}

	public boolean hasVkAccount() {
		return mHasVkAccount;
	}

	public boolean isOriginalEmpty() {
		return mOriginalTitle == null || mOriginalArtist == null;
	}

	public static enum Status {
		SUCCESS, CANCELLED
	}
}
