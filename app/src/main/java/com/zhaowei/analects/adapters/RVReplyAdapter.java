package com.zhaowei.analects.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.ReplyPostBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import java.util.ArrayList;

public class RVReplyAdapter extends RecyclerView.Adapter<RVReplyAdapter.ReplyViewHolder> {

    private final ArrayList<ReplyPostBean> list;

    private final OnItemClickListener onItemClickListener;

    public RVReplyAdapter(ArrayList<ReplyPostBean> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_reply, viewGroup, false);
        ReplyViewHolder replyViewHolder = new ReplyViewHolder(view);
        return replyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder replyViewHolder, int i) {
        replyViewHolder.textViewFromuser.setText(list.get(i).getFromusername());
        replyViewHolder.textViewContent.setText(list.get(i).getReplycontent());
        replyViewHolder.textViewTouser.setText(list.get(i).getTousername());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", list.get(i).getReplytime()).toString();
        replyViewHolder.textViewTime.setText(date);
        final int position = i;
        replyViewHolder.cardViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemCardClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder{

        CardView cardViewPost;
        TextView textViewFromuser;
        TextView textViewTouser;
        TextView textViewContent;
        TextView textViewTime;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewPost = (CardView)itemView.findViewById(R.id.cardview_reply);
            textViewFromuser = (TextView)itemView.findViewById(R.id.recycler_view_item_reply_from);
            textViewTouser = (TextView)itemView.findViewById(R.id.recycler_view_item_reply_to);
            textViewContent = (TextView)itemView.findViewById(R.id.recycler_view_item_reply_content);
            textViewTime = (TextView)itemView.findViewById(R.id.recycler_view_item_reply_time);
        }
    }

}
