package com.zhaowei.analects.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.NoteBean;

public class NoteInfoActivity extends BaseActivity {

    private Toolbar toolbar;

    private TextView mTextViewTitle;

    private TextView mTextViewCreateTime;

    private TextView mTextViewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar_note_info);
        setSupportActionBar(toolbar);

        mTextViewTitle = (TextView)findViewById(R.id.tv_note_info_title);
        mTextViewCreateTime = (TextView)findViewById(R.id.tv_note_info_createTime);
        mTextViewContent = (TextView)findViewById(R.id.tv_note_info_content);

        Intent intent = getIntent();
        NoteBean noteobject = (NoteBean)intent.getSerializableExtra("NOTE_OBJECT");

        mTextViewTitle.setText(noteobject.getNotetitle().toString());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", noteobject.getNotetime()).toString();
        mTextViewCreateTime.setText(date);
        mTextViewContent.setText(noteobject.getNotecontent().toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.text_shared:

                //获取笔记内容
                Intent intent = getIntent();
                NoteBean noteobject = (NoteBean)intent.getSerializableExtra("NOTE_OBJECT");

                //分享
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, noteobject.getNotecontent().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "分享到"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
