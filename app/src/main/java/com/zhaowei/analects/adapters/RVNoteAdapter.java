package com.zhaowei.analects.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.NoteBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import java.util.ArrayList;

public class RVNoteAdapter extends RecyclerView.Adapter<RVNoteAdapter.NoteViewHolder> {

    private final ArrayList<NoteBean> list;

    private final OnItemClickListener onItemClickListener;

    public RVNoteAdapter(ArrayList<NoteBean> list, OnItemClickListener onItemClickListener) {
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewDate;
        ImageView imageViewMore;
        CardView cardViewNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = (TextView)itemView.findViewById(R.id.tv_item_note_title);
            textViewContent = (TextView)itemView.findViewById(R.id.tv_item_note_content);
            textViewDate = (TextView)itemView.findViewById(R.id.tv_item_note_date);
            imageViewMore = (ImageView)itemView.findViewById(R.id.image_view_note_item_more);
            cardViewNote = (CardView)itemView.findViewById(R.id.recycler_view_item_note_cardview);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_note, viewGroup, false);
        NoteViewHolder viewHolder = new NoteViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i) {
        noteViewHolder.textViewTitle.setText(list.get(i).getNotetitle());
        noteViewHolder.textViewContent.setText(list.get(i).getNotecontent());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", list.get(i).getNotetime()).toString();
        noteViewHolder.textViewDate.setText(date);
        final int position = i;
        noteViewHolder.imageViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
        noteViewHolder.cardViewNote.setOnClickListener(new View.OnClickListener() {
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

}
