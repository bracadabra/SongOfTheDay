package ru.vang.songoftheday.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.vang.songoftheday.util.Logger;

public class SongOfTheDayDbHelper extends SQLiteOpenHelper {

    private static final String TAG = SongOfTheDayDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "songoftheday.db";

    private static final String TABLE_NAME = "similarsongs";

    private static final String COLUMN_NAME_ID = "_id";

    private static final String COLUMN_NAME_MBID = "mbid";

    private static final int COLUMNS_COUNT = 1;

    public SongOfTheDayDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY," + COLUMN_NAME_MBID + " TEXT NOT NULL" + ");");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertMbid(final String mbid) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            return;
        }

        final ContentValues cv = new ContentValues(COLUMNS_COUNT);
        cv.put(COLUMN_NAME_MBID, mbid);
        db.insert(TABLE_NAME, null, cv);
        db.close();
        Logger.debug(TAG, "Row with mbid " + mbid + " was inserted");
    }

    public boolean ifMbidExists(final String mbid) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            return false;
        }

        final Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME_MBID + "=?",
                new String[]{mbid}, null, null, null);
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        db.close();

        return exists;
    }

}
