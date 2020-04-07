package com.my.socialstress.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizDbHelper  extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String TABLE_MOOD = "mood_quiz";
    public static final String TABLE_DASS = "dass_quiz";
    public static final String TABLE_TEST = "test_quiz";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERID = "userid";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COUNT = "count";
    private HashMap hp;

    public QuizDbHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("create table " + TABLE_MOOD +
                        "(id integer primary key, "+COLUMN_USERID+" text,"+COLUMN_ANSWER+" integer,"+COLUMN_DATE+" text, "+COLUMN_COUNT+" integer)"
        );
        db.execSQL("create table " + TABLE_DASS +
                "(id integer primary key, "+COLUMN_USERID+" text,"+COLUMN_ANSWER+" integer,"+COLUMN_DATE+" text)"
        );
        db.execSQL("create table " + TABLE_TEST +
                "(id integer primary key, "+COLUMN_USERID+" text,"+COLUMN_ANSWER+" integer,"+COLUMN_DATE+" text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_MOOD);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_DASS);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_TEST);
        onCreate(db);
    }

    public boolean insertMood (String userid, int answer, String date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_COUNT, count);
        db.insert(TABLE_MOOD, null, contentValues);
        return true;
    }
    public boolean insertDass (String userid, int answer, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        db.insert(TABLE_DASS, null, contentValues);
        return true;
    }
    public boolean insertTest (String userid, int answer, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        db.insert(TABLE_TEST, null, contentValues);
        return true;
    }

    public Cursor getData(int id, String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+table_name+" where id="+id+"", null );
        return res;
    }

    public int numberOfRows(String table_name){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, table_name);
        return numRows;
    }

    public boolean updateMood (Integer id, String userid, int answer, String date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_COUNT, count);
        db.update(TABLE_MOOD, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }
    public boolean updateDass (Integer id, String userid, int answer, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        db.update(TABLE_DASS, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }
    public boolean updateTest (Integer id, String userid, int answer, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_DATE, date);
        db.update(TABLE_TEST, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteMood (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MOOD,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
    public Integer deleteDass (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DASS,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
    public Integer deleteTest (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TEST,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllMoods() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_MOOD, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_USERID)));
            res.moveToNext();
        }
        return array_list;
    }
    public ArrayList<String> getAllDass() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_DASS, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_USERID)));
            res.moveToNext();
        }
        return array_list;
    }
    public ArrayList<String> getAllTests() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_TEST, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_USERID)));
            res.moveToNext();
        }
        return array_list;
    }

    public List<Question> getAll_Moods() {
        List<Question> contactList = new ArrayList<Question>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MOOD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Question contact = new Question();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setUserid(cursor.getString(1));
                contact.setAnswer(cursor.getInt(2));
                contact.setDate(cursor.getString(3));
                contact.setCount(cursor.getInt(4));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }
    public List<Question> getAll_Dass() {
        List<Question> contactList = new ArrayList<Question>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DASS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Question contact = new Question();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setUserid(cursor.getString(1));
                contact.setAnswer(cursor.getInt(2));
                contact.setDate(cursor.getString(3));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }
    public List<Question> getAll_Test() {
        List<Question> contactList = new ArrayList<Question>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TEST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Question contact = new Question();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setUserid(cursor.getString(1));
                contact.setAnswer(cursor.getInt(2));
                contact.setDate(cursor.getString(3));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }
}