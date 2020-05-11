package com.zhaowei.analects.fragments;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.activities.AudioPlayerService;
import com.zhaowei.analects.activities.MusicPlayActivity;
import com.zhaowei.analects.adapters.RVMusicAdapter;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.beans.PlayingNode;
import com.zhaowei.analects.utils.OnItemClickListener;
import com.zhaowei.analects.utils.ServiceUtil;
import com.zhaowei.analects.view.MultiFloatingActionButton;
import com.zhaowei.analects.view.TagFabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    private NetMusicConnect conn;
    private AudioPlayerService.MyBinder audioControl;
    private MultiFloatingActionButton multiFloatingActionButton;

    private RecyclerView recyclerView;

    private RVMusicAdapter rvMusicAdapter;

    private final List<String> musics = new ArrayList<>();

    private final List<MusicInfo> music_list = new ArrayList<>();

    private static final int SYNC_OK = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SYNC_OK) {
                rvMusicAdapter.notifyDataSetChanged();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    };

    public MusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_network);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        multiFloatingActionButton = (MultiFloatingActionButton) view.findViewById(R.id.floating_music);
        multiFloatingActionButton.setVisibility(View.INVISIBLE);
        multiFloatingActionButton.setOnFabItemClickListener(new MultiFloatingActionButton.OnFabItemClickListener() {
            @Override
            public void onFabItemClick(TagFabLayout view, int pos) {
                switch (pos){
                    case 2:
                        LinearSmoothScroller smoothScrollerTop = new LinearSmoothScroller(getContext()) {
                            @Override
                            protected int getVerticalSnapPreference() {
                                return LinearSmoothScroller.SNAP_TO_START;
                            }
                        };
                        smoothScrollerTop.setTargetPosition(0);
                        recyclerView.getLayoutManager().startSmoothScroll(smoothScrollerTop);
                        break;
                    case 3:
                        if (ServiceUtil.isServiceRunning(getContext(), "com.zhaowei.analects.activities.AudioPlayerService")) {
                            Intent bindIntent = new Intent(getContext(), AudioPlayerService.class);
                            bindIntent.putExtra("WHERE", 1);
                            conn = new NetMusicConnect();
                            getActivity().getApplicationContext().bindService(bindIntent, conn, BIND_AUTO_CREATE);
                        }
                        break;
                }
            }
        });

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);

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
                intent.putExtra("LIST_KIND", 0);
                intent.putExtra("WHERE", 0);
                startActivity(intent);
            }

            @Override
            public void onItemCardClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        recyclerView.setAdapter(rvMusicAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshNetWrokData();
                    }
                }, 500);
            }
        });

        refreshNetWrokData();

    }

    private class NetMusicConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audioControl = (AudioPlayerService.MyBinder) service;
            PlayingNode playingNode = audioControl.getListPosition();
            if (playingNode.getListKind() != 0) {
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

    /*
     * 从网络更新数据
     * */
    private void refreshNetWrokData() {
        musics.clear();
        music_list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/query.jsp")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("idlist");
                            String name = jsonObject.getString("name");
                            musics.add(name);
                            String path = jsonObject.getString("path");
                            int part = jsonObject.getInt("part");
                            String netpath = "http://148.70.155.194:8080/downloads/" + path + ".mp3";
                            MusicInfo musicInfo = new MusicInfo(id, name, netpath, part);
                            music_list.add(musicInfo);
                        }
                        Message message = new Message();
                        message.what = SYNC_OK;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
