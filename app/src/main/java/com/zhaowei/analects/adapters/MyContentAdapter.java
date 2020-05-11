package com.zhaowei.analects.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhaowei.analects.R;

public class MyContentAdapter extends BaseAdapter {

    private String[] lvs;

    private Context context;

    private int color;

    public MyContentAdapter() {

    }

    public MyContentAdapter(Context context, String[] lvs, int color) {
        this.lvs = lvs;
        this.context = context;
        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getCount() {
        return lvs.length;
    }

    @Override
    public Object getItem(int position) {
        return lvs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyContentViewHolder holder;

        if (convertView == null) {
            holder = new MyContentViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_text, null);
            holder.textView = (TextView) convertView.findViewById(R.id.tv_item_text_content);
            convertView.setTag(holder);
        }
        holder = (MyContentViewHolder) convertView.getTag();

        holder.textView.setText(lvs[position]);
        holder.textView.setTextColor(color);

        return convertView;
    }

    private class MyContentViewHolder{
        public TextView textView;
    }

}
