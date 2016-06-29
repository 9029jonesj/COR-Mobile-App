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
 * Filename: StreamActivity.java
 * Description: Video stream activity initiates and streams video.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     28-Oct-15   Create file, added core functionality, and streaming error handling.
 *                      J.Jones     30-Oct-15   Added ProgressDialog to show user that the video is loading.
 *                      J.Jones     10-Nov-15   Fixed error handling on startStream(). Originally defined each error, now
 *                                              reference it by it's MediaPlayer class.
 *                      J.Jones     4-Dec-15    Added logcat logging.
 *                      J.Jones     3-Feb-16    Updated logging information.
 *                      J.Jones     17-Jun-16   Remove stream URL.
 */

package org.corapp.cor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

public class StreamActivity extends Activity {

    private ProgressDialog mProgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        startStream();
    }

    /** Start video stream using URL for video. (Excluded code for Media Player. Wanted to get rid of all play/pause and seek controls.) */
    private void startStream() {
        final VideoView videoView = (VideoView) findViewById(R.id.stream_view);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String error1, error2;
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        error1 = "Unspecified media player error.";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        error1 = "Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.";
                        break;
                    default:
                        error1 = "";
                        break;
                }
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        error2 = "File or network related operation errors.";
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        error2 = "Bitstream is not conforming to the related coding standard or file spec.";
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        error2 = "Some operation takes too long to complete, usually more than 3-5 seconds.";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        error2 = "Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.";
                        break;
                    default:
                        error2 = "";
                        break;
                }
                Log.d("SA.startStream()", error1 + " " + error2);
                return false;
            }
        });
        videoView.setMediaController(null);
        videoView.setVideoURI(Uri.parse(Constants.STREAM_URL));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mProgDialog.dismiss();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("SA.startStream()", "Video did not start.");
                onBackPressed();
                Toast.makeText(getApplicationContext(), "Error! Try again later.",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });
        try {
            mProgDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data...", true);
            videoView.start();
        } catch (Exception e) {
            Log.d("SA.startStream()", "Video did not start. Error: " + e.getMessage());
        }
    }
}
