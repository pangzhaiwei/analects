package com.zhaowei.analects.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AudioDBHelper extends SQLiteOpenHelper {

    String sql = "create table Audios(_id integer primary key autoincrement," +
            "name varchar(255)," +
            "path varchar(255)," +
            "part integer )";

    public AudioDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
