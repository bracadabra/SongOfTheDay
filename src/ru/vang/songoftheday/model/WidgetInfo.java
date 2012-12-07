package ru.vang.songoftheday.model;

import ru.vang.songoftheday.api.VkTrack;

//for future use
public class WidgetInfo {

	private String mOriginalTitle;
	private String mOriginalArtist;
	private VkTrack mVkTrack;

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
}
