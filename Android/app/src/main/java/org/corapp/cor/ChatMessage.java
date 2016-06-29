/*
 * Copyright (C) 2015-16 COR Church in Irvine, CA
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
 * Filename: ChatMessage.java
 * Description: Store information of a chat message for placement into Chat  view holder.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     12-Feb-16   Create file, added core functionality.
 */

package org.corapp.cor;

import android.graphics.Bitmap;

public class ChatMessage {
    private String name;
    private String msg;
    private long timestamp;
    private Bitmap profPic;

    public ChatMessage(String user, String text, long time, Bitmap img) {
        name = user;
        msg = text;
        timestamp = time;
        profPic = img;
    }
}
