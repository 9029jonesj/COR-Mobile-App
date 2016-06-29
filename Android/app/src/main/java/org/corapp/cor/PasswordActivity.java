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
 * Filename: PasswordActivity.java
 * Description: Send user password when it is forgotten.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     20-Feb-16   Basic functionality.
 *                      J.Jones     17-Jun-16   Remove Server IP.
 */

package org.corapp.cor;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PasswordActivity extends AppCompatActivity {

    private Socket mSocket;

    @Override
    public View findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    public Resources getResources() {
        return super.getResources();
    }

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.btn_submit) Button _submitButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        ButterKnife.inject(this);

        _submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(validate()) { checkUser(); }
            }
        });

        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);

            // Socket emitters
            mSocket.on("not found", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SOCKET ON: ", "NOT FOUND");
                    final String msg = (String) args[0];
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).on("user exists", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SOCKET ON: ", "USER EXISTS");
                    Log.d("SA.user exist", "Socket Received user exist event.");
                    final boolean exists = (boolean) args[0];
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (exists) {
                                send();
                            }
                        }
                    });
                }
            });
            // Connect socket to server
            mSocket.connect();

            Log.i("LA.onCreate()", "Socket Connected to " + Constants.IP_ADDRESS);
        } catch (URISyntaxException e) { Log.d("PA.onCreate()", e.getMessage()); }
    }

    private void checkUser() {
        String email = _emailText.getText().toString();

        JSONObject data = new JSONObject();
        try {
            data.put("email", email.toLowerCase());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("check user", data);
        Log.d("SOCKET EMIT: ", "check user");
    }

    public void send() {
        _submitButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(PasswordActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending...");

        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException e) { Log.d("LA.login()", e.getMessage()); }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onSentSuccess();
                        progressDialog.dismiss();
                        mSocket.disconnect();
                        //finish();
                    }
                }, 3000);
    }

    public void onSentSuccess() {
        _submitButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Toast.makeText(getBaseContext(), "Password sent.", Toast.LENGTH_SHORT).show();
        // Go back to Login Activity
        finish();
    }

    @Override
    public void onBackPressed() {
        mSocket.disconnect();
        super.onBackPressed();
    }

    public boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;
    }
}

