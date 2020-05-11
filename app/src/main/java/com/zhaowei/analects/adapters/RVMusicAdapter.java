package com.zhaowei.analects.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.utils.OnItemClickListener;

import java.util.List;

public class RVMusicAdapter extends RecyclerView.Adapter<RVMusicAdapter.ViewHolder> {

    private final List<String> mArray;

    private final Context context;

    private final OnItemClickListener onItemClickListener;

    public RVMusicAdapter(List<String> list, Context context, OnItemClickListener onItemClickListener) {
        this.mArray = list;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;

        TextView itemName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.recycler_view_item_cardview);
            itemName = (TextView) view.findViewById(R.id.recycler_view_item_cardview_tv);
        }

    }

    @NonNull
    @Override
    public RVMusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_item_music, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RVMusicAdapter.ViewHolder viewHolder, int i) {
        viewHolder.itemName.setText(mArray.get(i));
        final int position = i;
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
        viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.onItemLongClick(v, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArray == null ? 0 : mArray.size();
    }
}
