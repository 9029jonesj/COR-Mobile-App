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
 * Filename: Constants.java
 * Description: File used to store important/frequently used constants.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     17-Jun-16   Create file, added core functionality.
 */

package org.corapp.cor;

public final class Constants {
    public static final String IP_ADDRESS = "http://76.167.173.122:80";
    public static final String STREAM_URL = "http://ws.nhicdn.net/worshipstream/_definst_/mp4:ChristOurRedeemerAMEChurch.stream/playlist.m3u8";

    /** The caller should be prevented from constructing objects of
     this class, by declaring this private constructor. */
    private Constants() {}
}
