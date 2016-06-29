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
 * Filename: WebViewFragment.java
 * Description: Displays all web pages..
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     22-Nov-15   Create file, added core functionality.
 *                      J.Jones     27-Nov-15   Open links with images in external browser.
 *
 */

package org.corapp.cor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private StackOfUrls mParam2;
    protected static WebView mWebView;

    public WebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebViewFragment newInstance(String param1, StackOfUrls param2) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putParcelable(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getParcelable(ARG_PARAM2);
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_web_view, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.webViewF);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    // Use stack to keep track of URLs since each URL click opens a new AsyncTask -- mWebView.goBack();
                    previousPage(v);
                    return true;
                }
                return false;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            /** When link/url is clicked, load without header and footer */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // If link is a CORChurch website link then open using BlogTask AsyncTask
                if (url.contains("corchurch") && !url.contains("sunday-synop") && !url.contains("cor-newsletter-1")) {
                    // TODO: Get Error handling for Page Not Found (ie: see "www.corchurch.org/blog/coming-soon")
                    new WebTask().execute(url, view, mParam2);
                } else { // Else, open in external browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ARG_PARAM1, mParam1);
        savedInstanceState.putParcelable(ARG_PARAM2, mParam2);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new WebTask().execute(mParam1, mWebView, mParam2);
    }
    /** Load previous page from stack */
    // TODO: Fix back button. Returning from external links causes issues (Skips links).
    private void previousPage(View v) {
        String url;
        try {
            url = mParam2.pop();
        } catch (NullPointerException e) { return; }

        if (url.contains("corchurch")) {
            // TODO: Get Error handling for Page Not Found (ie: see "www.corchurch.org/blog/coming-soon")
            new WebTask().execute(url, v, mParam2);
        }
    }
}