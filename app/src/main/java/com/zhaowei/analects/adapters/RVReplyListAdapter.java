package com.zhaowei.analects.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.ReplyBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import java.util.ArrayList;

public class RVReplyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<ReplyBean> list;
    private OnItemClickListener onItemClickListener;

    private View mHeaderView;

    private int ITEM_TYPE_NORMAL = 0;
    private int ITEM_TYPE_HEADER = 1;

    public RVReplyListAdapter(ArrayList<ReplyBean> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_TYPE_HEADER) {
            return new ViewHolder(mHeaderView);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                            R.layout.list_item_reply,
                            viewGroup,
                            false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int type = getItemViewType(position);
        if (type == ITEM_TYPE_HEADER) {
            return;
        }
        int realPos = getRealItemPosition(position);
        ((ViewHolder) viewHolder).textViewFrom.setText(list.get(realPos).getFromusername());
        ((ViewHolder) viewHolder).textViewTo.setText(list.get(realPos).getTousername());
        ((ViewHolder) viewHolder).textViewContent.setText(list.get(realPos).getReplycontent());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", list.get(realPos).getReplytime()).toString();
        ((ViewHolder) viewHolder).textViewTime.setText(date);
        ((ViewHolder) viewHolder).linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, realPos);
            }
        });
        ((ViewHolder) viewHolder).linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.onItemLongClick(v, realPos);
                return false;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (null != mHeaderView && position == 0) {
            return ITEM_TYPE_HEADER;
        }
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        int itemCount = list.size();
        if (null != mHeaderView) {
            itemCount++;
        }
        return itemCount;
    }

    private int getRealItemPosition(int position) {
        if (null != mHeaderView) {
            return position - 1;
        }
        return position;
    }

    public void addHeaderView(View view) {
        mHeaderView = view;
        notifyItemInserted(0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textViewFrom;
        TextView textViewTo;
        TextView textViewContent;
        TextView textViewTime;

        ViewHolder(View v) {
            super(v);
            linearLayout = (LinearLayout)v.findViewById(R.id.ll_recycler_view_reply_postinfo);
            textViewFrom = (TextView)v.findViewById(R.id.tv_post_info_item_reply_from);
            textViewTo = (TextView)v.findViewById(R.id.tv_post_info_item_reply_to);
            textViewContent = (TextView)v.findViewById(R.id.tv_post_info_reply_content);
            textViewTime = (TextView)v.findViewById(R.id.tv_post_info_reply_time);
        }
    }
}
