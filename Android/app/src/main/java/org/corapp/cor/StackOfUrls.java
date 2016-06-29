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
 * Filename: StackofUrls.java
 * Description: Allows the storage of URLs for back button press. (How all modern browsers implement back button presses.) Stack implemented using a Linked List.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     2-Nov-15    Create file, added core functionality.
 *                      J.Jones     24-Nov-15   Added peak function, implemented Parcelable to pass as
 *                                              parameter to WebViewFragment.
 */

package org.corapp.cor;

import android.os.Parcel;
import android.os.Parcelable;

public class StackOfUrls implements Parcelable {

    private Node mFirst;
    private int mCount;

    public StackOfUrls(){
        mFirst = null;
        mCount = 0;
    }

    public void push(String url) {
        Node oldmFirst = mFirst;
        mFirst = new Node(url);
        mFirst.next = oldmFirst;
        mCount++;
    }

    public String pop() {
        String url = mFirst.item;
        if(mFirst == null) {
            throw new NullPointerException();
        } else {
            mFirst = mFirst.next;
            mCount--;
        }
        return url;
    }

    public String peek() {
        String url = mFirst.item;
        return url;
    }

    public boolean isEmpty() {
        if(mCount == 0) { return true; }
        return false;
    }

    private class Node implements Parcelable {
        Node next;
        String item;

        Node(String url) {
            item = url;
            next = null;
        }

        protected Node(Parcel in) {
            next = (Node) in.readValue(Node.class.getClassLoader());
            item = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(next);
            dest.writeString(item);
        }

        @SuppressWarnings("unused")
        public final Parcelable.Creator<Node> CREATOR = new Parcelable.Creator<Node>() {
            @Override
            public Node createFromParcel(Parcel in) {
                return new Node(in);
            }

            @Override
            public Node[] newArray(int size) {
                return new Node[size];
            }
        };
    }

    protected StackOfUrls(Parcel in) {
        mFirst = (Node) in.readValue(Node.class.getClassLoader());
        mCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mFirst);
        dest.writeInt(mCount);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<StackOfUrls> CREATOR = new Parcelable.Creator<StackOfUrls>() {
        @Override
        public StackOfUrls createFromParcel(Parcel in) {
            return new StackOfUrls(in);
        }

        @Override
        public StackOfUrls[] newArray(int size) {
            return new StackOfUrls[size];
        }
    };
}