package com.zhaowei.analects.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.activities.ArticalInfoActivity;
import com.zhaowei.analects.activities.MusicPlayActivity;
import com.zhaowei.analects.activities.NoteInfoActivity;
import com.zhaowei.analects.activities.PostInfoActivity;
import com.zhaowei.analects.activities.UpdateNoteActivity;
import com.zhaowei.analects.adapters.RVMusicAdapter;
import com.zhaowei.analects.adapters.RVNoteAdapter;
import com.zhaowei.analects.adapters.RVPostAdapter;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.beans.NoteBean;
import com.zhaowei.analects.beans.Paragraph;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.utils.ArticalDBUtil;
import com.zhaowei.analects.utils.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private AppCompatSpinner spinner;

    private int currentPosition = 0;

    private SearchView mSearchView;

    private ListView mListView;

    private ArticalAdapter articalAdapter;

    private ArrayList<Paragraph> paragraphs;

    private final List<String> musics = new ArrayList<>();
    private final List<MusicInfo> music_list = new ArrayList<>();

    private static final int SYNC_OK = 1;

    private RecyclerView recyclerViewMusic;
    private RVMusicAdapter rvMusicAdapter;

    private final ArrayList<PostBean> post_list = new ArrayList<>();
    private RecyclerView recyclerViewPost;
    private RVPostAdapter rvPostAdapter;

    private final ArrayList<NoteBean> note_list = new ArrayList<>();
    private RecyclerView recyclerViewNote;
    private RVNoteAdapter rvNoteAdapter;

    private SharedPreferences sharedPreferences;
    private int userid;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == SYNC_OK) {
                switch (currentPosition) {
                    case 1:
                        rvMusicAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        rvPostAdapter.notifyDataSetChanged();
                        break;
                    case 3:
                        rvNoteAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }else if (msg.what == 500){
                rvNoteAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "删除成功！", Toast.LENGTH_SHORT).show();
            }

        }
    };

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        userid = sharedPreferences.getInt("userid", -1);

        spinner = (AppCompatSpinner) view.findViewById(R.id.spinner_search);
        mSearchView = (SearchView) view.findViewById(R.id.search_view_artical);
        mListView = (ListView) view.findViewById(R.id.lv_search_artical);

        recyclerViewMusic = (RecyclerView) view.findViewById(R.id.recycler_view_search_music);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewMusic.setLayoutManager(layoutManager);
        rvMusicAdapter = new RVMusicAdapter(musics, getContext(), new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //进行跳转播放操作
                Intent intent = new Intent(getContext(), MusicPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("MUSICS", (Serializable) music_list);
                intent.putExtras(bundle);
                intent.putExtra("POSITION", position);
                intent.putExtra("LIST_KIND", 2);
                intent.putExtra("WHERE", 0);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemCardClick(View view, int position) {

            }
        });
        recyclerViewMusic.setAdapter(rvMusicAdapter);

        recyclerViewPost = (RecyclerView) view.findViewById(R.id.recycler_view_search_post);
        LinearLayoutManager layoutManagerPost = new LinearLayoutManager(getContext());
        recyclerViewPost.setLayoutManager(layoutManagerPost);
        rvPostAdapter = new RVPostAdapter(post_list, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemCardClick(View view, int position) {

                //进行跳转
                Intent intentpost = new Intent(getContext(), PostInfoActivity.class);
                intentpost.putExtra("CLICKEDPOST", post_list.get(position));
                startActivity(intentpost);

            }
        });
        recyclerViewPost.setAdapter(rvPostAdapter);

        recyclerViewNote = (RecyclerView)view.findViewById(R.id.recycler_view_search_note);
        LinearLayoutManager linearLayoutManagerNote = new LinearLayoutManager(getContext());
        recyclerViewNote.setLayoutManager(linearLayoutManagerNote);
        rvNoteAdapter = new RVNoteAdapter(note_list, new OnItemClickListener() {
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
                                int noteid = note_list.get(position).getNoteid();
                                final String delnote = noteid + "";
                                deleteNote(delnote);
                                note_list.remove(position);
                                return false;
                            case R.id.note_list_item_update:
                                Intent intent = new Intent(getContext(), UpdateNoteActivity.class);
                                intent.putExtra("NOTE_UPDATE", note_list.get(position));
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
                Intent intentnote = new Intent(getContext(), NoteInfoActivity.class);
                intentnote.putExtra("NOTE_OBJECT", note_list.get(position));
                startActivity(intentnote);
            }
        });
        recyclerViewNote.setAdapter(rvNoteAdapter);

        mSearchView.setQueryHint("请输入搜索内容");
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(true);

        final String[] spinnerItems = new String[]{"原文", "音频", "帖子", "笔记"};
        //简单的string数组适配器：样式res，数组
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, spinnerItems);
        //下拉的样式res
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner.setAdapter(spinnerAdapter);

        articalAdapter = new ArticalAdapter();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ArticalInfoActivity.class);
                intent.putExtra("ARTICAL_INFO", paragraphs.get(position));
                startActivity(intent);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                currentPosition = position;

                //默认全部不可见
                recyclerViewNote.setVisibility(View.GONE);
                recyclerViewPost.setVisibility(View.GONE);
                recyclerViewMusic.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                switch (currentPosition) {
                    case 0:
                        if (TextUtils.isEmpty(s)) {
                            mListView.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "请输入关键字！", Toast.LENGTH_SHORT).show();
                        } else {
                            mListView.setVisibility(View.VISIBLE);
                            ArticalDBUtil articalDBUtil = new ArticalDBUtil(getContext());
                            paragraphs = articalDBUtil.queryByWord(s);
                            articalAdapter.notifyDataSetChanged();
                            mListView.setAdapter(articalAdapter);
                        }
                        break;
                    case 1:
                        if (TextUtils.isEmpty(s)) {
                            recyclerViewMusic.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "请输入关键字！", Toast.LENGTH_SHORT).show();
                        } else {
                            musics.clear();
                            music_list.clear();
                            rvMusicAdapter.notifyDataSetChanged();
                            recyclerViewMusic.setVisibility(View.VISIBLE);
                            final String str = s;
                            searchAudio(str);

                        }
                        break;
                    case 2:
                        if (TextUtils.isEmpty(s)) {
                            recyclerViewPost.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "请输入关键字！", Toast.LENGTH_SHORT).show();
                        } else {
                            post_list.clear();
                            rvPostAdapter.notifyDataSetChanged();
                            recyclerViewPost.setVisibility(View.VISIBLE);
                            final String str = s;
                            searchPost(str);

                        }
                        break;
                    case 3:
                        if (TextUtils.isEmpty(s)) {
                            recyclerViewNote.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "请输入关键字！", Toast.LENGTH_SHORT).show();
                        } else {
                            rvNoteAdapter.notifyDataSetChanged();
                            recyclerViewNote.setVisibility(View.VISIBLE);
                            searchNote(s);

                        }
                        break;
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                switch (currentPosition) {
                    case 0:
                        if (TextUtils.isEmpty(s)) {
                            mListView.setVisibility(View.GONE);
                        } else {
                            mListView.setVisibility(View.VISIBLE);
                            ArticalDBUtil articalDBUtil = new ArticalDBUtil(getContext());
                            paragraphs = articalDBUtil.queryByWord(s);
                            articalAdapter.notifyDataSetChanged();
                            mListView.setAdapter(articalAdapter);
                        }
                        break;
                    case 1:
                        //避免过于频繁的请求，nothing to do
                        break;
                    case 2:
                        //避免过于频繁的请求，nothing to do
                        break;
                    case 3:
                        //避免过于频繁的请求，nothing to do
                        break;
                }

                return true;
            }
        });

    }

    private class ArticalAdapter extends BaseAdapter {

        class ViewHodler {
            TextView mTextViewArtical;
        }

        @Override
        public int getCount() {
            return paragraphs.size();
        }

        @Override
        public Object getItem(int position) {
            return paragraphs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler hodler;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_item_artical, null);
                hodler = new ViewHodler();
                hodler.mTextViewArtical = (TextView) convertView.findViewById(R.id.tv_search_artical_item);
                convertView.setTag(hodler);
            } else {
                hodler = (ViewHodler) convertView.getTag();
            }
            hodler.mTextViewArtical.setText(paragraphs.get(position).getClassical().toString());
            return convertView;
        }
    }

    //搜索音频
    private void searchAudio(final String str) {
        //在这里进行联网搜索操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("keyword", str)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/search.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("idlist");
                            String name = jsonObject.getString("name");
                            int part = jsonObject.getInt("part");
                            musics.add(name);
                            String path = jsonObject.getString("path");
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

    //搜索动态
    private void searchPost(final String str) {
        //在这里进行联网搜索操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("keyword", str)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/searchPost.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int postid = jsonObject.getInt("postid");
                            String posttitle = jsonObject.getString("posttitle");
                            String postcontent = jsonObject.getString("postcontent");
                            int userid = jsonObject.getInt("userid");
                            String username = jsonObject.getString("username");
                            long posttime = jsonObject.getLong("posttime");
                            PostBean postBean = new PostBean(postid, posttitle, postcontent, userid, username, posttime);
                            post_list.add(postBean);
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

    //搜索笔记
    private void searchNote(String keyword){
        note_list.clear();
        final String queryuser = userid + "";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("keyword", keyword)//添加键值对
                            .add("userid", queryuser)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/searchNote.jsp")
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
                            note_list.add(note);
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
