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
 * Filename: ParseInfoTask.java
 * Description: Parses all data from JSON file.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     16-Nov-15   Create file, added core functionality.
 *                      J.Jones     4-Dec-15    Added logcat logging.
 *                      J.Jones     3-Feb-16    Updated logging information.
 *
 */

package org.corapp.cor;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ParseInfoTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG_MINISTRIES = "ministries";
    private static final String TAG_NAME = "name";
    private static final String TAG_LEADER = "leader";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_CONTACT_INFO = "contact info";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_FAX = "fax";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_EMAIL_CONTACTS = "email contacts";
    private static final String TAG_TITLE = "title";
    private static final String TAG_ADDRESS = "street address";
    private static final String TAG_CITY_ZIP = "city zip";
    private static final String TAG_COR_CENTRAL = "cor central";
    protected static List<ContactInfo> mContactInfo;
    protected static List<EmailContact> mEmailContact;
    protected static List<Address> mAddress;
    protected static List<Ministry> mMinistries;
    private JSONArray mMinistryObjs, mInfoObj, mEmailObj, mAddrObj;

    @Override
    protected Void doInBackground(Void... params) {
        String json = loadJSONFromAsset();
        parseMinistries(json);
        parseContact(json);
        return null;
    }

    /** Load JSON file from asset folder and place all contents into a string for parsing. */
    public String loadJSONFromAsset() {
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

    /** Parse each ministry based on predefined TAGS. */
    private void parseMinistries(String json) {
        mMinistries = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            mMinistryObjs = jsonObj.getJSONArray(TAG_MINISTRIES);

            for(int i = 0; i < mMinistryObjs.length(); i++) {
                JSONObject obj = mMinistryObjs.getJSONObject(i);
                String name = obj.getString(TAG_NAME);
                String leader = obj.getString(TAG_LEADER);
                String contact = obj.getString(TAG_CONTACT);
                String description = obj.getString(TAG_DESCRIPTION);

                mMinistries.add(new Ministry(name, leader, contact, description));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("PIT.parseMinistries", "Ministries info parsed successfully.");
    }

    /** Parse each contact info based on predefined TAGS. */
    private void parseContact(String json) {
        mContactInfo = new ArrayList<>();
        try {
            // Contact Information
            JSONObject jsonObj = new JSONObject(json);
            mInfoObj = jsonObj.getJSONArray(TAG_CONTACT_INFO);
            JSONObject obj = mInfoObj.getJSONObject(0);
            String phone = obj.getString(TAG_PHONE);
            String fax = obj.getString(TAG_FAX);
            String email = obj.getString(TAG_EMAIL);
            mContactInfo.add(new ContactInfo(phone, fax, email));

            // Email Information
            mEmailContact = new ArrayList<>();
            JSONObject jsonObj2 = new JSONObject(json);
            mEmailObj = jsonObj2.getJSONArray(TAG_EMAIL_CONTACTS);

            for(int i = 0; i < mEmailObj.length(); i++) {
                obj = mEmailObj.getJSONObject(i);
                String name = obj.getString(TAG_NAME);
                String title = obj.getString(TAG_TITLE);
                email = obj.getString(TAG_EMAIL);

                mEmailContact.add(new EmailContact(name, title, email));
            }

            // Map Information
            mAddress = new ArrayList<>();
            JSONObject jsonObj3 = new JSONObject(json);
            mAddrObj = jsonObj3.getJSONArray(TAG_COR_CENTRAL);
            obj = mAddrObj.getJSONObject(0);
            String name = obj.getString(TAG_NAME);
            String addr = obj.getString(TAG_ADDRESS);
            String cityZip = obj.getString(TAG_CITY_ZIP);

            mAddress.add(new Address(name, addr, cityZip));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("PIT.parseContact()", "Contact info parsed successfully.");
    }
}
