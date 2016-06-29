/*
 * Copyright (C) 2015 COR Church in Irvine, CA
 *
 * Licensed under the COR License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Filename: TabFragment.java
 * Description: Initializes TabHost and ViewPager for sliding tab fragments.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     23-Nov-15    Create file, added core functionality.
 *
 */

package org.corapp.cor;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;

import java.util.List;
import java.util.Vector;

public class TabFragment extends Fragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String[] tabNames;
    private String[] tabLinks;
    private TabHost tabHost;
    private ViewPager viewPager;
    private TabFragmentPagerAdapter myViewPagerAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment newInstance(String[] param1, String[] param2) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_PARAM1, param1);
        args.putStringArray(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TabFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabNames = getArguments().getStringArray(ARG_PARAM1);
            tabLinks = getArguments().getStringArray(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        initializeTabHost(rootView);
        initializeViewPager(rootView);
        return rootView;
    }

    @Override
    public void onTabChanged(String tabId) {
        int pos = this.tabHost.getCurrentTab();
        this.viewPager.setCurrentItem(pos);

        HorizontalScrollView hScrollView = (HorizontalScrollView) getActivity().findViewById(R.id.hScrollView);
        View tabView = tabHost.getCurrentTabView();
        int scrollPos = tabView.getLeft()
                - (hScrollView.getWidth() - tabView.getWidth()) / 2;
        hScrollView.smoothScrollTo(scrollPos, 0);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }

    @Override
    public void onPageSelected(int position) {
        this.tabHost.setCurrentTab(position);
    }

    // Fake Content for TabHost
    class FakeContent implements TabHost.TabContentFactory {
        private final Context mContext;

        public FakeContent(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumHeight(0);
            v.setMinimumWidth(0);
            return v;
        }
    }

    private void initializeViewPager(View v) {
        List<Fragment> fragments = new Vector<>();

        for(int i = 0; i < tabNames.length; i++) {
            fragments.add(WebViewFragment.newInstance(tabLinks[i], new StackOfUrls()));
        }
        this.myViewPagerAdapter = new TabFragmentPagerAdapter(
                getFragmentManager(), fragments);
        this.viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        this.viewPager.setAdapter(this.myViewPagerAdapter);
        this.viewPager.setOnPageChangeListener(this);
        onStop();
        onStart();
    }

    private void initializeTabHost(View v) {
        tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        tabHost.setup();

        for(int i = 0; i < tabNames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator(tabNames[i]);
            tabSpec.setContent(new FakeContent(v.getContext()));
            tabHost.addTab(tabSpec);
        }
        tabHost.setOnTabChangedListener(this);
    }
}
