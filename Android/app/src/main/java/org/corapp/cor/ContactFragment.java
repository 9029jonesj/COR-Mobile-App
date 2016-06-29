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
 * Filename: ContactFragment.java
 * Description: Creates ability to load contact us fragment.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     10-Nov-15   Create file, added core functionality; AsyncTask to load contacts from JSON.
 *                                              AsyncTask to initialize adapter.
 *                      J.Jones     17-Nov-15   Removed AsyncTask and loading/parsing of JSON file. Now info is loaded on app start.
 *                      J.Jones     4-Dec-15    Added logcat logging.
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

public class ContactFragment extends Fragment {
    private RecyclerView mRecView;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecView = (RecyclerView)view.findViewById(R.id.contact_rec_view);
        mRecView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.sContext);
        mRecView.setLayoutManager(layoutManager);
        initializeAdapter();
        Log.i("CF.onViewCreated()", "Contact Fragment view created.");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Initialize Recycler View Adapter */
    private void initializeAdapter(){
        RecViewAdapterContactUs adapter = new RecViewAdapterContactUs(ParseInfoTask.mContactInfo, ParseInfoTask.mEmailContact, ParseInfoTask.mAddress);
        mRecView.setAdapter(adapter);
    }
}
