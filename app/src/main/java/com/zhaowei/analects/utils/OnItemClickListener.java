package com.zhaowei.analects.utils;

import android.view.View;

public interface OnItemClickListener {

    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);

    void onItemCardClick(View view, int position);

}
