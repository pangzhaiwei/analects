package com.zhaowei.analects.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.Paragraph;

public class ArticalInfoActivity extends BaseActivity {

    private Toolbar toolbar;

    private TextView mTextViewClassical;

    private TextView mTextViewModern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artical_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar_artical_info);
        setSupportActionBar(toolbar);

        mTextViewClassical = (TextView)findViewById(R.id.tv_search_result_info_classical);
        mTextViewModern = (TextView)findViewById(R.id.tv_search_result_info_modern);

        Intent intent = getIntent();
        Paragraph paragraph = (Paragraph) intent.getSerializableExtra("ARTICAL_INFO");
        String classical = "原文：\n" + "\u3000\u3000" + paragraph.getClassical() + "\n";
        String modern = "译文：\n" + "\u3000\u3000" + paragraph.getModern();
        mTextViewClassical.setText(classical);
        mTextViewModern.setText(modern);

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

                //获取待分享的内容
                Intent intent = getIntent();
                Paragraph paragraph = (Paragraph) intent.getSerializableExtra("ARTICAL_INFO");
                String classical = "原文：\n" + "\u3000\u3000" + paragraph.getClassical() + "\n";
                String modern = "译文：\n" + "\u3000\u3000" + paragraph.getModern();

                //分享
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, classical + modern);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "分享到"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
