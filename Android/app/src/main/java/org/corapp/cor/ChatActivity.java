/*
 * Copyright (C) 2016 COR Church in Irvine, CA
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
 * Filename: ChatActivity.java
 * Description: Chat activity that handles all chat navigation functions, including adding/removing chat rooms.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     4-Jan-16    Create file, added core functionality.
 *                      J.Jones     4-May-16    Getting user groups from DB.
 *                      J.Jones     5-May-16    ListView layout.
 *                      J.Jones     6-May-16    Auto populate list view with ministries from DB.
 *                      J.Jones     21-May-16   Add button adds group to database under users account.
 *                      J.Jones     6-Jun-16    ListView items are now clickable and open Message Activity.
 *                      J.Jones     17-Jun-16   Remove Server IP.
 */

package org.corapp.cor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private FloatingActionButton fab;

    private static final String TAG_NAME = "name";
    private static final String TAG_MINISTRIES = "ministries";
    private JSONArray mMinistryObjs;
    static List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    static SimpleAdapter adapter;
    static ListView lv;
    private SharedPreferences settings;
    private Socket mSocket;
    private static PlaceholderFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);

        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);
        } catch (URISyntaxException e) { Log.d("CA.onCreate()", e.getMessage()); }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_light_comm_chat);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Collect ministry names from website
                String json = loadJSONFromAsset();
                final CharSequence ministries[] = parseMinistries(json);

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Add a Ministry");
                builder.setIcon(R.mipmap.ic_dark_menu_ministries);
                builder.setItems(ministries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Add ministry to viewer, refresh viewer
                        JSONObject data = new JSONObject();
                        try {
                            data.put("email", settings.getString("email", "").toLowerCase());
                            data.put("group", ministries[which]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        boolean found = false;
                        for(String str : MainActivity.groups) {
                            if(str.equals(ministries[which])) {
                                Toast.makeText(getBaseContext(), "You are already apart of this ministry.", Toast.LENGTH_SHORT).show();
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            mSocket.emit("add group", data);
                        }
                        resetList();
                    }
                });
                builder.show();
            }
        });
        adapter = new SimpleAdapter(this, data,
                R.layout.min_list_item,
                new String[] {"name", "msg", "time"},
                new int[] {R.id.min_name,
                        R.id.msg,
                        R.id.msg_time});
    }

    public void resetList() {
//        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
//        tr.replace(R.id.container, fragment);
//        tr.commit();
        //lv.invalidate();
        mViewPager.refreshDrawableState();
    }

    /** Load JSON file from asset folder and place all contents into a string for parsing */
    private String loadJSONFromAsset() {
        String json;
        try {
            // TODO: Save JSON file on server
            InputStream is =  MainActivity.sContext.getAssets().open("info.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /** Parse each ministry based on predefined TAGS */
    private CharSequence[] parseMinistries(String json) {
        List<String> ministry = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            mMinistryObjs = jsonObj.getJSONArray(TAG_MINISTRIES);

            for(int i = 0; i < mMinistryObjs.length(); i++) {
                JSONObject obj = mMinistryObjs.getJSONObject(i);
                String name = obj.getString(TAG_NAME);
                ministry.add(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ministry.toArray(new CharSequence[ministry.size()]);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

            lv = (ListView)rootView.findViewById(R.id.ministriesListView);

            // Update list each time view is created
            data.clear();
            for(String str : MainActivity.getGroups()) {
                Map<String, String> datum = new HashMap<String, String>(2);
                datum.put("name", str);
                datum.put("msg", "Pastor Mark: Test Message");
                datum.put("time", "Wed. 6:15pm");
                data.add(datum);
            }

            lv.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                    Intent intent = new Intent(getActivity(), MessageActivity.class);
                    HashMap<String, String> temp = (HashMap<String, String>) lv.getItemAtPosition(i);
                    String title = temp.get("name");
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            });

            lv.setAdapter(adapter);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "My Ministries";
            }
            return null;
        }
    }
}
