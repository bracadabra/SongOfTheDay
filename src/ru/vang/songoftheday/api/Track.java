package ru.vang.songoftheday.api;

public class Track {
	protected transient String mId;
	protected transient String mTitle;
	protected transient String mArtist;
	
	public Track() {	
	};

	public Track(final String _id, final String artist, final String title) {
		mId = _id;
		mArtist = artist;
		mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String title) {
		mTitle = title;
	}

	public String getArtist() {
		return mArtist;
	}

	public void setArtist(final String artist) {
		mArtist = artist;
	}

	public String getId() {
		return mId;
	}

	public void setId(final String _id) {
		mId = _id;
	}
	
	public Track copy() {
		return new Track(mId, mArtist, mTitle);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mArtist == null) ? 0 : mArtist.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result + ((mTitle == null) ? 0 : mTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Track other = (Track) obj;
		if (mArtist == null) {
			if (other.mArtist != null) {
				return false;
			}
		} else if (!mArtist.equals(other.mArtist)) {
			return false;
		}
		if (mId == null) {
			if (other.mId != null) {
				return false;
			}
		} else if (!mId.equals(other.mId)) {
			return false;
		}
		if (mTitle == null) {
			if (other.mTitle != null) {
				return false;
			}
		} else if (!mTitle.equals(other.mTitle)) {
			return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return mTitle == null && mArtist == null;
	}

	public String toString() {
		return mId + " " + mArtist + " " + mTitle;
	}
}
