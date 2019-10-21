package com.yulia.milich.mplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongLikesDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "likes";
    public static final String ID = "_id";
    public static final String LIKES = "likess";
    public static final String LOVE = "love";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME + " (" + ID + " INT, "
            + LOVE + " INT, "
            + LIKES + " INT);";

    private static final String
            SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public SongLikesDB(Context context) {
        super(context, DATABASE_NAME, null,
                DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public boolean addSong(int id) {
        SQLiteDatabase sqdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SongLikesDB.ID, id);
        cv.put(SongLikesDB.LIKES, 0);
        cv.put(SongLikesDB.LOVE, 0);
        long result = sqdb.insert(SongLikesDB.TABLE_NAME, null, cv);

        if (result == -1) {
            return false;
        } else return true;
    }

    public Cursor getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemByLikes(int likes){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LIKES + " = '" + likes + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void deleteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = '" + id + "'";
        db.execSQL(query);
    }

    public void incLikes(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = getItem(id);
        int likesColIndex = c.getColumnIndex(LIKES);
        int likes = c.getInt((likesColIndex));
        String query = "UPDATE " + TABLE_NAME + " SET " + LIKES + " = '" + (likes + 1) + "' WHERE " + ID + " = '" + id + "'";
        db.execSQL(query);
    }

    public void loveSong(boolean l, int id){
        int ok = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = getItem(id);
        int loveColIndex = c.getColumnIndex(LOVE);
        int love = c.getInt((loveColIndex));
        if (l){
            ok = 1;
        }
        String query = "UPDATE " + TABLE_NAME + " SET " + LOVE + " = '" + ok + "' WHERE " + ID + " = '" + id + "'";
        db.execSQL(query);
    }

    public Cursor getLovedSongs(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LOVE + " = '" + 1 + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public int getLikes(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = getItem(id);
        c.moveToFirst();
        int likesColIndex = c.getColumnIndex(LIKES);
        int likes = c.getInt((likesColIndex));
        return likes;
    }

    public boolean doesSongExists(int nid) {
        boolean exist = false;

        SQLiteDatabase sqdb = this.getWritableDatabase();
        Cursor c = sqdb.query(SongLikesDB.TABLE_NAME, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast() && !exist) {
            int idColIndex = c.getColumnIndex(ID);

            int id = c.getInt(idColIndex);

            if (id == nid) {
                exist = true;
            }
            c.moveToNext();
        }

        c.close();
        return exist;
    }

    public int getMaxLikes(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(LIKES) FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        data.moveToFirst();
        int likesColIndex = data.getColumnIndex(LIKES);
        int likes = data.getInt(likesColIndex);
        return likes;
    }

    //    public Cursor getItem(String name) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LIKES + " = '" + name + "'";
//        Cursor data = db.rawQuery(query, null);
//        return data;
//    }

//    public void updateName(String newName, int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + LIKES + " = '" + newName + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }

//    public void updatePassword(String newPassword, int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + UNLIKES + " = '" + newPassword + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }
//
//    public void updatePassword(String newPassword, String name) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + UNLIKES + " = '" + newPassword + "' WHERE " + LIKES + " = '" + name + "'";
//        db.execSQL(query);
//    }
//
//    public void updateGamesPlayed(String newGamesPlayed, int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + GAMESPLAYED + " = '" + newGamesPlayed + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }
//
//    public void updateGamesWon(String newGamesWon, int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + GAMESWON + " = '" + newGamesWon + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }
//
//    public void updateManager(String newManager, int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + MANAGER + " = '" + newManager + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }
//
//    public void updateID(int id, int newID) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + ID + " = '" + newID + "' WHERE " + ID + " = '" + id + "'";
//        db.execSQL(query);
//    }

//    public void update(int id, int newId, String newName, String newPassword, String newGamesPlayed, String newGamesWon, String newManager) {
//        updateID(id, newId);
//        updateName(newName, id);
//        updatePassword(newPassword, id);
//        updateGamesPlayed(newGamesPlayed, id);
//        updateGamesWon(newGamesWon, id);
//        updateManager(newManager, id);
//    }

//    public boolean isNameTaken(String name) {
//        boolean ok = false;
//        SQLiteDatabase sqdb = this.getWritableDatabase();
////        Cursor c = sqdb.query(DBUsers.TABLE_NAME, null, null, null, null, null, null);
////        c.moveToFirst();
////        while (!c.isAfterLast() && !ok) {
////            int nameColIndex = c.getColumnIndex(LIKES);
////
////            String name1 = c.getString(nameColIndex);
////            if (name1.equals(name))
////                ok = true;
////            c.moveToNext();
////        }
////        c.close();
//        Cursor c = getItem(name);
//        if(c.getCount()>0){
//            ok = true;
//        }
//        c.close();
//        return ok;
//    }
}
