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
 * Filename: WebTask.java
 * Description: Remove header, footer, and column from web pages.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     23-Nov-15   Create file, added core functionality.
 *                      J.Jones     24-Nov-15   Parsed columns (Prayer request, etc.)
 *                      J.Jones     3-Feb-16    Inserted logging information.
 *
 */

package org.corapp.cor;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebTask extends AsyncTask<Object, Void, Void> {
    private Document doc;
    private String mUrl;
    private WebView mWebView;
    private StackOfUrls mUrls;

    @Override
    protected Void doInBackground(Object... params) {
        mUrl = (String) params[0];
        mWebView = (WebView) params[1];
        mUrls = (StackOfUrls) params[2];

        try {
            doc = Jsoup.connect(mUrl).get();
            Elements headTags = doc.select("header");
            headTags.remove();
            Elements footerTags = doc.select("footer");
            footerTags.remove();
            Elements columnTag = doc.getElementsByClass("large-3");
            columnTag.remove();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("WT.doInBackground()", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        mUrls.push(mUrl);
        try {
            mWebView.loadDataWithBaseURL(mUrl, doc.toString(), "text/html", "utf-8", "");
        } catch (NullPointerException e) {

        }
    }
}