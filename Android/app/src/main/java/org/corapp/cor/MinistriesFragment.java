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
 * Filename: MinistriesFragment.java
 * Description: Displays all mMinistries via cards.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     3-Nov-15    Create file, added core functionality.
 *                                              Load JSON File, init adapter, parse JSON file.
 *                      J.Jones     4-Nov-15    Set Recycler View to have fixed size.
 *                      J.Jones     17-Nov-15   Removed the loading/parsing of the JSON file.
 *                                              Placed in AsyncTask to load when app starts.
 *                      J.Jones     4-Dec-15    Added logcat logging.
 *                      J.Jones     3-Feb-16    Updated logging information.
 *
 */

package org.corapp.cor;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MinistriesFragment extends Fragment {
    private RecyclerView mRecView;

    // TODO: Implement Alphabet scroll
    public MinistriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ministries, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecView = (RecyclerView)view.findViewById(R.id.min_rec_view);
        mRecView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.sContext);
        mRecView.setLayoutManager(layoutManager);
        initializeAdapter();
        Log.i("MF.onViewCreated()", "Ministries Fragment view created.");
    }

    /** Initialize Recycler View Adapter. */
    private void initializeAdapter(){
        RecViewAdapterMin adapter = new RecViewAdapterMin(ParseInfoTask.mMinistries);
        mRecView.setAdapter(adapter);
    }
}