package ru.vang.songoftheday.api;

public class Track {
	protected transient long mId;
	protected transient String mTitle;
	protected transient String mArtist;
	private static final int SEED = 31;

	public Track() {
		// Create empty track
	};

	public Track(final long _id, final String artist, final String title) {
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

	public long getId() {
		return mId;
	}

	public void setId(final long _id) {
		mId = _id;
	}
	
	public Track copy() {
		return new Track(mId, mArtist, mTitle);
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = SEED * result + ((mTitle == null) ? 0 : mTitle.hashCode());
		result = SEED * result + ((mArtist == null) ? 0 : mArtist.hashCode());
		result = SEED * result + String.valueOf(mId).hashCode();

		return result;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (object == null) {
			return false;
		}

		if (getClass() != object.getClass()) {
			return false;
		}

		final Track other = (Track) object;
		if (mTitle == null) {
			if (other.mTitle != null) {
				return false;
			}
		} else if (!mTitle.equals(other.mTitle)) {
			return false;
		}

		if (mArtist == null) {
			if (other.mArtist != null) {
				return false;
			}
		} else if (!mArtist.equals(other.mArtist)) {
			return false;
		}

		if (mId != other.mId) {
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
