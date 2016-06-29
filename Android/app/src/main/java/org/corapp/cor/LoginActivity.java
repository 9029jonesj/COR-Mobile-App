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
 * Filename: LoginActivity.java
 * Description: Login activity that handles all login actions.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     4-Dec-15    Create file, added core functionality.
 *                      J.Jones     29-Dec-15   Added ability to store login information.
 *                      J.Jones     3-Feb-16    Updated logging information.
 *                      J.Jones     4-Feb-16    Added MD5 check for passwords. (Security)
 *                      J.Jones     5-Feb-16    Added login capability.
 *                      J.Jones     17-Feb-16   Fixed login, removed MD5.
 *                      J.Jones     20-Feb-16   Added 'Forgot Password?' button.
 *                      J.Jones     4-May-16    Add email to pref file.
 *                      J.Jones     17-Jun-16   Remove Server IP.
 */

package org.corapp.cor;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_PSW = 0;
    private Socket mSocket;
    private SharedPreferences settings;


    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;
    @InjectView(R.id.link_forgotpsw) TextView _forgotpswLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validate()) {
                    checkUser();
                }
            }
        });

        _forgotpswLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Password activity
                Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
                startActivityForResult(intent, REQUEST_PSW);
                mSocket.disconnect();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                mSocket.disconnect();
            }
        });

        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);

            // Socket emitters
            mSocket.on("check password", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    final boolean correct = (boolean) args[0];
                    Log.d("SOCKET ON: ", "CHECK PASSWORD");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (correct) {
                                login();
                            } else {
                                Toast.makeText(getBaseContext(), "Incorrect password, try again.", Toast.LENGTH_SHORT).show();
                                _loginButton.setEnabled(true);
                            }
                        }
                    });
                }
            }).on("not found", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SOCKET ON: ", "NOT FOUND");
                    final String msg = (String) args[0];
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show(); }
                    });
                }
            }).on("groups", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    String data = args[0].toString();
                    data = data.replace("[", "");
                    data = data.replace("]", "");
                    data = data.replace("\"", "");
                    MainActivity.groups = data.split(",");

                }
            });
            // Connect socket to server
            mSocket.connect();

            // Get email from SharedPrefs
            JSONObject data = new JSONObject();
            try {
                data.put("email", settings.getString("email", "").toLowerCase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("get groups", data);

            Log.i("LA.onCreate()", "Socket Connected to " + Constants.IP_ADDRESS);
        } catch (URISyntaxException e) { Log.d("LA.onCreate()", e.getMessage()); }
    }

    private void checkUser() {
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        JSONObject data = new JSONObject();
        try {
            data.put("email", email.toLowerCase());
            data.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("login", data);
        Log.d("SOCKET EMIT: ", "LOGIN");
    }

    @Override
    public void onBackPressed() {
        mSocket.disconnect();
        super.onBackPressed();
    }

    public void login() {
        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException e) { Log.d("LA.login()", e.getMessage()); }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                        mSocket.disconnect();
                        finish();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        } else if (requestCode == REQUEST_PSW) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        settings.edit().putBoolean("loggedIn", true).commit();
        settings.edit().putString("email", _emailText.getText().toString());
        Toast.makeText(getBaseContext(), "Login Successful.", Toast.LENGTH_SHORT).show();

    }

    /** Validate input. */
    public boolean validate() {
        boolean valid = true;
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }
}

