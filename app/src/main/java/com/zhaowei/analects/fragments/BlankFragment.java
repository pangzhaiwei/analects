package com.zhaowei.analects.fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.activities.AudioPlayerService;
import com.zhaowei.analects.activities.MusicPlayActivity;
import com.zhaowei.analects.adapters.RVMusicAdapter;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.beans.PlayingNode;
import com.zhaowei.analects.databases.AudioDBHelper;
import com.zhaowei.analects.utils.OnItemClickListener;
import com.zhaowei.analects.utils.ServiceUtil;
import com.zhaowei.analects.view.MultiFloatingActionButton;
import com.zhaowei.analects.view.TagFabLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;

    private LocalMusicConnect conn;
    private AudioPlayerService.MyBinder audioControl;
    private MultiFloatingActionButton multiFloatingActionButton;

    private RVMusicAdapter rvMusicAdapter;

    private AudioDBHelper audioDBHelper;

    private final List<String> musics = new ArrayList<>();

    private final List<MusicInfo> music_list = new ArrayList<>();

    private final int QUERY_OK = 200;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case QUERY_OK:
                    rvMusicAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        multiFloatingActionButton = (MultiFloatingActionButton) view.findViewById(R.id.floating_blank);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view_blank);

        multiFloatingActionButton.setVisibility(View.INVISIBLE);

        swipeRefreshLayout.setColorSchemeColors(Color.RED,Color.BLUE,Color.GREEN);

        audioDBHelper = new AudioDBHelper(getContext(), "NowAudios.db", null, 1);

        refreshData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        rvMusicAdapter = new RVMusicAdapter(musics, getContext(), new OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                multiFloatingActionButton.setVisibility(View.VISIBLE);

                Intent intent = new Intent(getContext(), MusicPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("MUSICS", (Serializable) music_list);
                intent.putExtras(bundle);
                intent.putExtra("POSITION", position);
                intent.putExtra("LIST_KIND", 1);
                intent.putExtra("WHERE", 0);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                PopupMenu popupMenu = new PopupMenu(getActivity(),view);
                popupMenu.getMenuInflater().inflate(R.menu.delete,popupMenu.getMenu());

                //弹出式菜单的菜单项点击事件
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.recycler_view_item_delete) {
                            boolean flag = deleteFile(music_list.get(position).getPath());
                            if (flag){
                                deleteRecord(music_list.get(position).getName());
                                refreshData();
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onItemCardClick(View view, int position) {

            }
        });
        recyclerView.setAdapter(rvMusicAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                }, 500);
            }
        });

        multiFloatingActionButton.setOnFabItemClickListener(new MultiFloatingActionButton.OnFabItemClickListener() {
            @Override
            public void onFabItemClick(TagFabLayout view, int pos) {
                switch (pos){
                    case 2:
                        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                            @Override
                            protected int getVerticalSnapPreference() {
                                return LinearSmoothScroller.SNAP_TO_START;
                            }
                        };
                        smoothScroller.setTargetPosition(0);
                        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                        break;
                    case 3:
                        if (ServiceUtil.isServiceRunning(getContext(), "com.zhaowei.analects.activities.AudioPlayerService")) {
                            Intent bindIntent = new Intent(getContext(), AudioPlayerService.class);
                            bindIntent.putExtra("WHERE", 1);
                            conn = new LocalMusicConnect();
                            getActivity().getApplicationContext().bindService(bindIntent, conn, BIND_AUTO_CREATE);
                        }
                        break;
                }
            }
        });

    }

    private class LocalMusicConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audioControl = (AudioPlayerService.MyBinder) service;
            PlayingNode playingNode = audioControl.getListPosition();
            if (playingNode.getListKind() != 1) {
                getActivity().getApplicationContext().unbindService(conn);
                Toast.makeText(getContext(), "不是这个列表！", Toast.LENGTH_SHORT).show();
                return;
            }
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {

                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(playingNode.getPosition());
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            getActivity().getApplicationContext().unbindService(conn);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    //刷新数据
    private void refreshData(){
        musics.clear();
        music_list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = audioDBHelper.getReadableDatabase();
                Cursor cursor = db.query("Audios", null, null, null, null, null, null);
                if (cursor.moveToFirst()){
                    do{
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String path = cursor.getString(cursor.getColumnIndex("path"));
                        int part = cursor.getInt(cursor.getColumnIndex("part"));
                        musics.add(name);
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setName(name);
                        musicInfo.setPath(path);
                        musicInfo.setPart(part);
                        music_list.add(musicInfo);
                    }while (cursor.moveToNext());
                }
                cursor.close();
                Message message = new Message();
                message.what = QUERY_OK;
                handler.sendMessage(message);
            }
        }).start();

    }

    //删除指定文件
    private boolean  deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    //删除指定数据库记录
    private void deleteRecord(String name){
        SQLiteDatabase db = audioDBHelper.getWritableDatabase();
        db.execSQL("delete from Audios where name like ?", new String[]{name});
    }

}
