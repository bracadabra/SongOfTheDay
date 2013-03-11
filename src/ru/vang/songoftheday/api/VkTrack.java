package ru.vang.songoftheday.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class VkTrack extends Track {
	private static final String TAG = VkTrack.class.getSimpleName();
	private static final String ARTIST = "artist";
	private static final String TITLE = "title";
	private static final String URL = "url";
	private static final String AID = "aid";
	private static final String OWNER_ID = "owner_id";
	private static final int SEED = 31;
	
	private transient String mOwnerId;
	private transient String mUrl;
	private transient String mPath;	
	
	public VkTrack(final String aid, final String ownerId, final String artist, final String title, final String url) {
		super(aid, artist, title);		
		mOwnerId = ownerId;		
		mUrl = url;
	}
	
	public String getOwnerId() {
		return mOwnerId;
	}
	
	public void setOwnerId(final String ownerId) {
		mOwnerId = ownerId;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public void setUrl(final String url) {
		mUrl = url;
	}
	
	public String getPath() {
		return mPath;
	}
	
	public void setPath(final String path) {
		mPath = path;
	}
	
	public static VkTrack valueOf(final JSONObject track) {
		VkTrack vkTrack = null;
		try {
			final String aid = track.getString(AID);
			final String ownerId = track.getString(OWNER_ID);
			final String artist = track.getString(ARTIST);
			final String title = track.getString(TITLE);
			final String url = track.getString(URL);
			
			vkTrack = new VkTrack(aid, ownerId, artist, title, url); 
		} catch (JSONException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		
		return vkTrack;
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + mPath;
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
		
		final VkTrack other = (VkTrack) object;
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
		
		if (mOwnerId != other.mOwnerId) {
			return false;
		}
		
		if (mUrl == null) {
			if(other.mUrl != null) {
				return false;
			}
		} else if (!mUrl.equals(other.mUrl)) {
			return false;
		}
		
		
		if (mPath == null) {
			if (other.mPath != null) {
				return false;
			}
		} else if (!mPath.equals(other.mPath)) {
			return false;
		}

		return true;		
	}
	
	@Override
	public int hashCode() {		
		int result = 1;
		result = SEED * result + ((mTitle == null) ? 0 : mTitle.hashCode());
		result = SEED * result + ((mArtist == null) ? 0 : mArtist.hashCode());
		result = SEED * result + String.valueOf(mId).hashCode();
		result = SEED * result + ((mPath == null) ? 0 : mPath.hashCode());
		result = SEED * result + ((mUrl == null) ? 0 : mPath.hashCode());
		result = SEED * result + String.valueOf(mOwnerId).hashCode();

		return result;
	}
}
