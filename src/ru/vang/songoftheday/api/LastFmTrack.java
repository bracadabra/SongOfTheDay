package ru.vang.songoftheday.api;

public class LastFmTrack extends Track {
	private String mMbid;

	public LastFmTrack() {
		// create empty track
	}

	public LastFmTrack(final long _id, final String artist, final String title, final String mbid) {
		super(_id, artist, title);
		mMbid = mbid;
	}

	public String getMbid() {
		return mMbid;
	}

	public void setMbid(String mMbid) {
		this.mMbid = mMbid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mMbid == null) ? 0 : mMbid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LastFmTrack other = (LastFmTrack) obj;
		if (mMbid == null) {
			if (other.mMbid != null) {
				return false;
			}
		} else if (!mMbid.equals(other.mMbid)) {
			return false;
		}
		return true;
	}

	public LastFmTrack copy() {
		return new LastFmTrack(mId, mArtist, mTitle, mMbid);
	}

}
