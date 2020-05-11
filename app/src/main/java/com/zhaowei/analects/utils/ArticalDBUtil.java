package com.zhaowei.analects.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhaowei.analects.beans.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ArticalDBUtil {

    private Context context;

    private static final String DB_PATH = "/data/data/com.zhaowei.analects/databases/";

    private static final String DB_NAME = "Artical.db";

    public ArticalDBUtil(Context context) {
        this.context = context;
    }

    //将assets目录下的Content数据库复制到/data/data对应的数据库目录下
    public void copyDB()throws IOException {
        String outFileName = DB_PATH + DB_NAME;
        File file = new File(DB_PATH);
        if (!file.mkdirs()) {
            file.mkdirs();
        }
        if (new File(outFileName).exists()) {
            // 数据库已经存在，无需复制
            return;
        }
        InputStream myInput = context.getAssets().open(DB_NAME);
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];	int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        } 	myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public ArrayList<Paragraph> queryByWord(String keyWord){
        ArrayList<Paragraph> list = new ArrayList<Paragraph>();
        String path = context.getDatabasePath("Artical.db").getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
        Cursor cursor = db.rawQuery("select * from sentence where classical like ?", new String[]{"%"+keyWord+"%"});
        while(cursor.moveToNext()){
            String classical = cursor.getString(cursor.getColumnIndex("classical"));
            String modern = cursor.getString(cursor.getColumnIndex("modern"));
            Paragraph paragraph = new Paragraph(classical, modern);
            list.add(paragraph);
        }
        db.close();
        cursor.close();
        return list;
    }
}

