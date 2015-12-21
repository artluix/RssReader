package artluix.rssreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class PostsDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "postsReader.db";
    private String TABLE_NAME = "posts";

    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_TITLE = "title";
    private static final String COLUMN_NAME_LINK = "link";
    private static final String COLUMN_NAME_DATE = "date";

    public PostsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RSS_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY," + COLUMN_NAME_TITLE + " TEXT,"
                + COLUMN_NAME_LINK + " TEXT," + COLUMN_NAME_DATE + " TEXT" + ")";
        db.execSQL(CREATE_RSS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addPost(PostData postData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TITLE, postData.getTitle());
        values.put(COLUMN_NAME_LINK, postData.getLink());
        values.put(COLUMN_NAME_DATE, postData.getDate());

        if (!isPostExists(db, postData.getLink())) {
            db.insert(TABLE_NAME, null, values);
            db.close();
        }
    }

    public ArrayList<PostData> getAllPosts() {
        ArrayList<PostData> listData = new ArrayList<PostData>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                PostData postData = new PostData();
                postData.setId(cursor.getInt(0));
                postData.setTitle(cursor.getString(1));
                postData.setLink(cursor.getString(2));
                postData.setDate(cursor.getString(3));
                listData.add(postData);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return listData;
    }

    public void deletePost(PostData postData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = " + String.valueOf(postData.getId()), null);
        db.close();
    }

    public void deleteAllPosts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public boolean isPostExists(SQLiteDatabase db, String link) {
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_NAME
            + " WHERE link = '" + link + "'", new String[] {});
        boolean exists = (cursor.getCount() > 0);
        return exists;
    }

}
