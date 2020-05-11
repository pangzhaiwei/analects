package com.zhaowei.analects.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.zhaowei.analects.activities.NoteEditorActivity;
import com.zhaowei.analects.activities.NoteInfoActivity;
import com.zhaowei.analects.activities.UpdateNoteActivity;
import com.zhaowei.analects.adapters.RVNoteAdapter;
import com.zhaowei.analects.beans.NoteBean;
import com.zhaowei.analects.utils.OnItemClickListener;
import com.zhaowei.analects.view.MultiFloatingActionButton;
import com.zhaowei.analects.view.TagFabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteFragment extends Fragment {

    private RecyclerView recyclerView;

    private MultiFloatingActionButton multiFloatingActionButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private final ArrayList<NoteBean> list = new ArrayList<>();

    private RVNoteAdapter rvNoteAdapter;

    private SharedPreferences sharedPreferences;
    private int userid;

    private final int NOTE_QUERY_OK = 300;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOTE_QUERY_OK:
                    rvNoteAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case 500:
                    if (userid != -1){
                        final String queryid = userid + "";
                        refreshData(queryid);
                    }
                    Toast.makeText(getContext(), "删除成功！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public NoteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        userid = sharedPreferences.getInt("userid", -1);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_note);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_note);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        multiFloatingActionButton = (MultiFloatingActionButton) view.findViewById(R.id.floating_note);
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
                        Intent intent = new Intent(getActivity(), NoteEditorActivity.class);
                        NoteFragment.this.startActivityForResult(intent, 1);
                        break;
                }
            }
        });

        rvNoteAdapter = new RVNoteAdapter(list, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int p) {
                final int position = p;
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.getMenuInflater().inflate(R.menu.note_list_item, popupMenu.getMenu());

                //弹出式菜单的菜单项点击事件
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.note_list_item_delete:
                                //通过list里的对象取出对应的id
                                int noteid = list.get(position).getNoteid();
                                final String delnote = noteid + "";
                                deleteNote(delnote);
                                return false;
                            case R.id.note_list_item_update:
                                Intent intent = new Intent(getContext(), UpdateNoteActivity.class);
                                intent.putExtra("NOTE_UPDATE", list.get(position));
                                startActivityForResult(intent, 1);
                                return false;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemCardClick(View view, int position) {
                Intent intent = new Intent(getContext(), NoteInfoActivity.class);
                intent.putExtra("NOTE_OBJECT", list.get(position));
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(rvNoteAdapter);

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (userid != -1) {
                            final String queryid = userid + "";
                            refreshData(queryid);
                        }
                    }
                }, 500);
            }
        });

        if (userid != -1) {
            final String queryid = userid + "";
            refreshData(queryid);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            //更新列表
            if (userid != -1) {
                final String queryid = userid + "";
                refreshData(queryid);
            }
        }
    }

    private void refreshData(final String queryid) {
        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("userid", queryid)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/queryNote.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int noteid = jsonObject.getInt("noteid");
                            String notetitile = jsonObject.getString("notetitle");
                            String notecontent = jsonObject.getString("notecontent");
                            long notetime = jsonObject.getLong("notetime");
                            int userid = jsonObject.getInt("userid");
                            String username = jsonObject.getString("username");
                            NoteBean note = new NoteBean(noteid, notetitile, notecontent, notetime, userid, username);
                            list.add(note);
                        }
                        Message message = new Message();
                        message.what = NOTE_QUERY_OK;
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

    private void deleteNote(final String noteid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("noteid", noteid)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/deleteNote.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string().trim();
                    try {
                        //获取返回值,500为成功
                        int back = Integer.valueOf(responseData);
                        Message message = new Message();
                        message.what = back;
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
