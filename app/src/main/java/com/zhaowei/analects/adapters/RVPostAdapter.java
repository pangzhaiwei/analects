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
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import java.util.ArrayList;

public class RVPostAdapter extends RecyclerView.Adapter<RVPostAdapter.PostViewHolder>{

    private final ArrayList<PostBean> list;

    private final OnItemClickListener onItemClickListener;

    public RVPostAdapter(ArrayList<PostBean> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_post, viewGroup, false);
        PostViewHolder postViewHolder = new PostViewHolder(view);
        return postViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int i) {
        postViewHolder.textViewCreater.setText(list.get(i).getUsername());
        postViewHolder.textViewTitle.setText(list.get(i).getPosttitle());
        postViewHolder.textViewContent.setText(list.get(i).getPostcontent());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", list.get(i).getPosttime()).toString();
        postViewHolder.textViewTime.setText(date);
        final int position = i;
        postViewHolder.cardViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemCardClick(v, position);
            }
        });
        postViewHolder.cardViewPost.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.onItemLongClick(v, position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder{

        CardView cardViewPost;
        TextView textViewCreater;
        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewTime;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewPost = (CardView)itemView.findViewById(R.id.recycler_view_item_post_cardview);
            textViewCreater = (TextView)itemView.findViewById(R.id.tv_item_post_creater);
            textViewTitle = (TextView)itemView.findViewById(R.id.tv_item_post_title);
            textViewContent = (TextView)itemView.findViewById(R.id.tv_item_post_content);
            textViewTime = (TextView)itemView.findViewById(R.id.tv_item_post_date);
        }
    }
}
