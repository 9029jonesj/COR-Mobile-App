/*
 * Copyright (C) 2015-2016 COR Church in Irvine, CA
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
 * Filename: SignupActivity.java
 * Description: Signup activity that handles all signup actions.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     4-Dec-15    Create file, added core functionality.
 *                      J.Jones     29-Dec-15   Added ability to store login information.
 *                      J.Jones     3-Feb-16    Updated logging information.
 *                      J.Jones     4-Feb-16    Added profile picture upload abilities.
 *                      J.Jones     5-Feb-16    Added ability to check if user is already registered.
 *                      J.Jones     6-Feb-16    Signup successfully.
 *                      J.Jones     12-Feb-16   Added default profile picture save when user does not select image.
 *                      J.Jones     17-Feb-16   Signup email automatically defaults to lowercase.
 *                      J.Jones     17-Jun-16   Remove Server IP.
 */

package org.corapp.cor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignupActivity extends AppCompatActivity {
    private static final int SELECT_FILE = 1;
    private static final int CAMERA_REQUEST = 1888;
    public static boolean userExists = false;
    private Socket mSocket;
    private Bitmap profPicture;

    @Override
    public View findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    public Resources getResources() {
        return super.getResources();
    }

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;
    @InjectView(R.id.profile_image) ImageView _profileImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        // get default profile picture
        AssetManager assetManager = this.getAssets();
        InputStream is;
        try {
            is = assetManager.open("default_profile_picture.jpg");
            profPicture = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) { Log.d("SA.onCreate()", e.getMessage()); }

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { userRegistered(); }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        _profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openImagePick(); }
        });

        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);

            // Socket emitters
            mSocket.on("user exist", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SA.user exist", "Socket Received user exist event.");
                    final boolean exists = (boolean) args[0];
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (exists) {
                                userExists = true;
                                signup();
                            } else {
                                userExists = false;
                                signup();
                            }
                        }
                    });
                }
            });
            // Connect socket to server
            mSocket.connect();

            Log.i("SA.onCreate()", "Socket Connected to " + Constants.IP_ADDRESS);
        } catch (URISyntaxException e) {
            Log.d("SA.onCreate()", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        mSocket.disconnect();
        super.onBackPressed();
    }

    public void userRegistered() {
        JSONObject data = new JSONObject();
        try {
            data.put("email", _emailText.getText().toString().toLowerCase());
        } catch (JSONException e) { Log.d("SA.userRegistered()", e.getMessage()); }

        mSocket.emit("signup", data);
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");

        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException e) { Log.d("SA.signup()", e.getMessage()); }


        final String name = _nameText.getText().toString();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();
        final Drawable profilePicture = _profileImage.getDrawable();

        // TODO: Implement your own signup logic here.
        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            data.put("email", email.toLowerCase());
            data.put("password", password);
            data.put("picture", profPicture);
        } catch (JSONException e) { Log.d("SA.signup()", e.getMessage()); }

        mSocket.emit("create user", data);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess(name, email, password, profilePicture);
                        // onSignupFailed();
                        progressDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Account Created.", Toast.LENGTH_SHORT).show();
                        mSocket.disconnect();
                        finish();
                    }
                }, 3000);
    }


    public void onSignupSuccess(String name, String email, String password, Drawable profilePicture) {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        settings.edit().putBoolean("loggedIn", true).commit();
        storeLogin(name, email, password);
        finish();
    }
    // TODO: Add info to nav header
    private void addNavData(String name, String email, Drawable profilePicture) {
        View navHeader = getLayoutInflater().inflate(R.layout.nav_header_main, null);
        TextView nameField = (TextView) navHeader.findViewById(R.id.userName);
        nameField.setText(name);

        TextView emailField = (TextView) navHeader.findViewById(R.id.emailAddr);
        emailField.setText(email);

        ImageView profPic = (ImageView) navHeader.findViewById(R.id.profPic);
        profPic.setImageDrawable(profilePicture);
    }

    public void onSignupFailed() {
        //Toast.makeText(getBaseContext(), "Signup failed.", Toast.LENGTH_SHORT).show();
        Log.i("SA.onSignupFailed()", "Signup failed.");
        _signupButton.setEnabled(true);
    }

    /** Store login information */
    private void storeLogin(String name, String email, String password) {
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        settings.edit().putString("name", name).commit();
        settings.edit().putString("email", email).commit();
        settings.edit().putString("password", password).commit();
        saveImage(profPicture);
    }

    /** Validate input. */
    private boolean validate() {
        boolean valid = true;
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();


        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {  _nameText.setError(null); }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else { _emailText.setError(null); }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else { _passwordText.setError(null); }

        if(userExists) {
            _emailText.setError("email address already registered");
            valid = false;
        } else { _emailText.setError(null); }

        userExists = false;
        return valid;
    }

    /** Open dialogue to select image */
    private void openImagePick() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select image...");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Program"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /** Resize selected image. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                profPicture = thumbnail;
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    Log.d("SA.onActivityResult()", e.getMessage());
                } catch (IOException e) {
                    Log.d("SA.onActivityResult()", e.getMessage());
                }
                _profileImage.setImageBitmap(profPicture);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                profPicture = bm;
                _profileImage.setImageBitmap(profPicture);
            }
        }
    }

    private void saveImage(Bitmap image) {
        File profilePicture = new File("sdcard/COR Church/", "profile_picture.png");
        if(profilePicture.exists()) { profilePicture.delete(); }
        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(profilePicture);
            image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            Log.d("SA.saveImage()", e.getMessage());
        } catch (IOException e) {
            Log.d("SA.saveImage()", e.getMessage());
        }
    }
}
