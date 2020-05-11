package com.zhaowei.analects.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaowei.analects.R;
import com.zhaowei.analects.adapters.MyFragmentPagerAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class IndexFragment extends Fragment {

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private MusicFragment musicFragment;

    private BlankFragment blankFragment;

    private ArrayList<String> tab_title_list = new ArrayList<>();//存放标签页标题

    private ArrayList<Fragment> fragment_list = new ArrayList<>();//存放ViewPager下的Fragment

    private MyFragmentPagerAdapter adapter;//适配器

    public IndexFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = (TabLayout)view.findViewById(R.id.tablayout);
        viewPager = (ViewPager)view.findViewById(R.id.view_pager_index);

        tab_title_list.add("在线音频");
        tab_title_list.add("本地音频");
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(1)));
        musicFragment = new MusicFragment();
        blankFragment = new BlankFragment();
        fragment_list.add(musicFragment);
        fragment_list.add(blankFragment);
        adapter = new MyFragmentPagerAdapter(getChildFragmentManager(), tab_title_list, fragment_list);
        viewPager.setAdapter(adapter);//给ViewPager设置适配器
        tabLayout.setupWithViewPager(viewPager);//将TabLayout与Viewpager联动起来
        tabLayout.setTabsFromPagerAdapter(adapter);//给TabLayout设置适配器

    }

}
