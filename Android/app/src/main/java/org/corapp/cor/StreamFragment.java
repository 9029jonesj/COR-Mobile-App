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
 * Filename: StreamFragment.java
 * Description: Creates ability to show streaming times text.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     28-Oct-15   Create file, added core functionality.
 *                      J.Jones     3-Feb-16    Updated logging information.
 */

package org.corapp.cor;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StreamFragment extends Fragment {

    private TextView streamText;
    private final String mStreamTimeText = "LIVE Worship Service:" + '\n' + "Sunday - 8am, 10:30am (PST)" +
            '\n' + "Re-streaming:" + '\n' + "Sunday - Tuesday 9am (PST)";

    public StreamFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        streamText = (TextView)view.findViewById(R.id.stream_times);
        editStreamText(streamText);
        streamService();
        Log.i("SF.onViewCreated()", "Stream view created.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stream, container, false);
    }

    /** Edits Stream Time text to bold certain parts */
    private void editStreamText(TextView tv) {
        SpannableString spanString = new SpannableString(mStreamTimeText);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, 21, 0);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 49, 62, 0);
        tv.setText(spanString);
    }

    /** Start new activity to initiate video stream */
    private void streamService() {
        Intent intent = new Intent(MainActivity.sContext, StreamActivity.class);
        startActivity(intent);
    }

}
