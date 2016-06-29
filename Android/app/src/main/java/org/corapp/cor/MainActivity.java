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
 * Filename: MainActivity.java
 * Description: Main activity that handles all navigation functions, including menu item actions.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     26-Oct-15   Create file, added core functionality.
 *                      J.Jones     27-Oct-15   Added new navigation menu items,
 *                                              created sendErrorReport() method to email report.
 *                      J.Jones     28-Oct-15   Added function to initiate video stream.
 *                      J.Jones     30-Oct-15   Added method to open Ministry and blog fragment.
 *                                              Set mToolbar title on drawer click.
 *                      J.Jones     3-Nov-15    Defined sContext variable for access in other classes.
 *                      J.Jones     7-Nov-15    Added method for Contact Us tab.
 *                                              Changed openStream() to open a stream fragment first, therefore
 *                                              an old fragment tab does not override the LiveStream text.
 *                      J.Jones     12-Nov-15   Initialized Maps on main thread.
 *                      J.Jones     16-Nov-15   Implemented viewpager with TabHost for multiple tabs.
 *                      J.Jones     17-Nov-15   Added method to parse all data when app loads.
 *                      J.Jones     22-Nov-15   Updated method of opening tabs, start tasks in new thread.
 *                      J.Jones     27-Nov-15   Enabled app bar menu item actions. (Open/Close Nav, Login/ Logout Button)
 *                                              Added tutorial to aide in navigating the application. New content.
 *                      J.Jones     4-Dec-15    Removed LogFile and implemented LogCat logging.
 *                      J.Jones     29-Dec-15   Check login status on app bootup.
 *                      J.Jones     3-Feb-15    Added ability to ask user for certain permissions. (New Android 6.0 requirement)
 *                                              Updated debugging scheme.
*                       J.Jones     6-May-16    Added method to check if user is logged in, if so, get ministries.
*                       J.Jones     17-Jun-16   Remove Server IP.
 */

package org.corapp.cor;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.*;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.maps.MapsInitializer;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final int REQUEST_CODE_MAIL = 1000;
    public static Context sContext;
    public static Intent sActivity;
    private ImageView mBkgndImg;
    private Toolbar mToolbar;
    private FragmentManager fragmentManager = getFragmentManager();
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout drawer;
    private ShowcaseView showCase;
    private Target homeButtonTarget, actionButtonTarget;
    int counter = 0;
    public static final String PREFS_NAME = "CORPrefsFile";
    private static SharedPreferences settings;
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static String[] groups;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContext = this.getApplicationContext();
        sActivity = this.getParentActivityIntent();

        checkPermissions();
        loadMainUIInteractions();
        if(checkFirstTime() == true) {
            setupTutorial();
        }
        new ParseInfoTask().execute();

        if(settings.getBoolean("loggedIn", true)) {
            groups = getGroups();
        }


        Log.i("MA.onCreate()", "Application started successfully.");
    }

    /** Check if external storage write permissions are enabled. */
    private void checkPermissions() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else { createLogFile(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Create Log File, which requires write permission.
                    createLogFile();
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly." +
                            '\n' + "Please consider granting it this permission.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(counter > 0) {
            showCase.hide();
        } else {
            showCase.setTarget(actionButtonTarget);
            showCase.setStyle(R.style.CustomShowcaseTheme2);
            showCase.setContentText("Press this button to Login/Logout of your User Profile.");
        }
        counter++;
    }

    /** If it is the first time the app is open, run tutorial */
    private boolean checkFirstTime() {
        // The app is being launched for first time, do something
        if (settings.getBoolean("firstTime", true)) {
            // Record the fact that the app has been started at least once
            settings.edit().putBoolean("firstTime", false).commit();
            return true;
        } else { return false; }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else { super.onBackPressed(); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setupTutorial() {
        homeButtonTarget = new Target() {
            @Override
            public Point getPoint() {
                // Get approximate position of overflow action icon's center
                int actionBarSize = getSupportActionBar().getHeight();
                int x = 0;
                int y = actionBarSize / 2;
                return new Point(x, y);
            }
        };

        actionButtonTarget = new Target() {
            @Override
            public Point getPoint() {
                // Get approximate position of overflow action icon's center
                int actionBarSize = getSupportActionBar().getHeight();
                int x = getResources().getDisplayMetrics().widthPixels - actionBarSize / 2;
                int y = actionBarSize / 2;
                return new Point(x, y);
            }
        };

        showCase = new ShowcaseView.Builder(this)
                .setTarget(homeButtonTarget)
                .setStyle(R.style.CustomShowcaseTheme2)
                .setContentText("Press this button to open the Navigation Panel." + '\n' + '\n' + "The Navigation Panel can also be accessed by swiping from the edge of the screen to the right.")
                .setOnClickListener(this)
                .build();
        showCase.show();
    }

    /** Handle action bar item clicks here. The action bar will
     *  automatically handle clicks on the Home/Up button, so long
     *  as you specify a parent activity in AndroidManifest.xml.  */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_login:
                if(item.getTitle().equals("Login")) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    removeLogin();
                }
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Remove all the users information. */
    private void removeLogin() {
        settings.edit().putBoolean("loggedIn", false).commit();
        settings.edit().remove("name");
        settings.edit().remove("email");
        settings.edit().remove("password");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check Login Status
        if(settings.getBoolean("loggedIn", true)) {
            menu.findItem(R.id.action_login).setTitle("Logout");
        }
        else {
            menu.findItem(R.id.action_login).setTitle("Login");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /** Navigate to selected menu item. */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mBkgndImg.setVisibility(View.INVISIBLE);

        switch (id) {
            case R.id.nav_about:
                mToolbar.setTitle(item.getTitle());
                openAbout();
                break;

            case R.id.nav_news:
                mToolbar.setTitle(item.getTitle());
                openNewsAndEvents();
                break;

            case R.id.nav_stream:
                mToolbar.setTitle(item.getTitle());
                streamService();
                break;

            case R.id.nav_calendar:
                // TODO: add calendar functionality
                //mToolbar.setTitle(item.getTitle());
                Toast.makeText(getApplicationContext(), "Calendar coming soon!",
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_ministries:
                mToolbar.setTitle(item.getTitle());
                openMinistries();
                break;

            case R.id.nav_giving:
                mToolbar.setTitle(item.getTitle());
                openGiving();
                break;

            case R.id.nav_store:
                mToolbar.setTitle(item.getTitle());
                openStore();
                break;

            case R.id.nav_directory:
                mToolbar.setTitle(item.getTitle());
                openDirectory();
                break;

            case R.id.nav_blog:
                mToolbar.setTitle(item.getTitle());
                openBlog();
                break;

            case R.id.nav_contact:
                mToolbar.setTitle(item.getTitle());
                openContactUs();
                break;

            case R.id.nav_chat:
                mBkgndImg.setVisibility(View.VISIBLE);
                if(settings.getBoolean("loggedIn", true)) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please login.",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_report:
                sendErrorReport();
                break;

            case R.id.nav_settings:
                // TODO: add settings functionality
                Toast.makeText(getApplicationContext(), "Settings coming soon!",
                        Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Send error report via email and attach LOG File. */
    private void sendErrorReport() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "CORChurchIrvine@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "Addresses");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please provide a description of the error: ");
        // Attach LOG File
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("sdcard/COR Church/logcat.txt")));
        try {
            startActivityForResult(Intent.createChooser(emailIntent, "Send Bug Report..."), REQUEST_CODE_MAIL);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            Log.d("MA.sendErrorReport()", "Send Error Report did not execute correctly. No email clients are installed. ERROR: " + ex.getMessage());
        }
    }

    /** Get result from sending error report, log message */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_MAIL){
            if(resultCode == RESULT_OK){
                Log.i("MA.onActivityResult()", "Error Report Sent.");
            } else if(resultCode == RESULT_CANCELED) {
                Log.i("MA.onActivityResult()", "Error Report Cancelled.");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Start new activity to initiate video stream */
    private void streamService() {
        // TODO: Only launch streaming between time frame
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment stream = new StreamFragment();
                fragmentManager.beginTransaction().replace(R.id.content, stream).commit();
            }
        }).start();
    }

    /** Opens Ministry Fragment */
    private void openMinistries() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment ministries = new MinistriesFragment();
                fragmentManager.beginTransaction().replace(R.id.content, ministries).commit();
            }
        }).start();
    }

    /** Opens Contact Fragment */
    private void openContactUs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment contactUs = new ContactFragment();
                fragmentManager.beginTransaction().replace(R.id.content, contactUs).commit();
            }
        }).start();
    }

    /** Open Blog in new WebView Fragment */
    private void openBlog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment blog = WebViewFragment.newInstance("http://www.corchurch.org/blog", new StackOfUrls());
                fragmentManager.beginTransaction().replace(R.id.content, blog).commit();
            }
        }).start();
    }

    /** Open Store in new WebView Fragment */
    private void openStore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment store = WebViewFragment.newInstance("http://www.corchurch.org/sermon-mp3", new StackOfUrls());
                fragmentManager.beginTransaction().replace(R.id.content, store).commit();
            }
        }).start();
    }

    /** Open Directory in new WebView Fragment */
    private void openDirectory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment biz = TabFragment.newInstance(getResources().getStringArray(R.array.directory_array), getResources().getStringArray(R.array.directory_array_links));
                fragmentManager.beginTransaction().replace(R.id.content, biz).commit();
            }
        }).start();
    }

    /** Open Giving in new WebView Fragment */
    private void openGiving() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment give = WebViewFragment.newInstance("https://corchurch.worldsecuresystems.com/giving", new StackOfUrls());
                fragmentManager.beginTransaction().replace(R.id.content, give).commit();
            }
        }).start();
    }

    /** Open About Us fragment with TabHost, ViewPager, and WebView components */
    private void openAbout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment about = TabFragment.newInstance(getResources().getStringArray(R.array.about_array), getResources().getStringArray(R.array.about_array_links));
                fragmentManager.beginTransaction().replace(R.id.content, about).commit();
            }
        }).start();
    }

    /** Open News and Events fragment with TabHost, ViewPager, and WebView components */
    private void openNewsAndEvents() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fragment newsEvents = TabFragment.newInstance(getResources().getStringArray(R.array.newsevents_array), getResources().getStringArray(R.array.newsevents_array_links));
                fragmentManager.beginTransaction().replace(R.id.content, newsEvents).commit();
            }
        }).start();
    }

    /** Loads all Main UI Interactions (ie. Nav Drawer, List items, etc) */
    private void loadMainUIInteractions() {
        // Toolbar Init
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Welcome");
        setSupportActionBar(mToolbar);

        // Nav Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(mToggle);
        mToggle.syncState();

        // Nav View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Bkgnd Img
        mBkgndImg = (ImageView) findViewById(R.id.cor_logo);
        mBkgndImg.setImageResource(R.drawable.cor_background_img);

        // Maps
        MapsInitializer.initialize(this.getApplicationContext());

        // Shared Prefs
        settings = getSharedPreferences(PREFS_NAME, 0);

        // Check Login status
        if(!settings.contains("loggedIn")) {
            settings.edit().putBoolean("loggedIn", false).commit();
        }
    }

    /** Returns the system information of the device (ie. Android Version, Device Manufacturer and Model). */
    private String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        DateFormat mDateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        Date mToday = new Date();
        String date = mDateFormatter.format(mToday);
        info.append("************************************************" + '\n');
        info.append("Codename: " + Build.VERSION.CODENAME + '\n');                  // Codename
        info.append("Incremental: " + Build.VERSION.INCREMENTAL + '\n');            // Increment
        info.append("Release: " + Build.VERSION.RELEASE + '\n');                    // Release
        info.append("Model: " + Build.MANUFACTURER + " " + Build.MODEL + '\n');     // Menu & Model
        info.append("Date: " + date + '\n' + "************************************************" + '\n' + '\n');
        return info.toString();
    }

    /** Create logcat log file for debugging later */
    private void createLogFile() {
        // Initializes log file, if file already exists, delete it and create a new one.
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "COR Church");
        if (!folder.exists()) { folder.mkdir(); }
        File logFile = new File("sdcard/COR Church/", "logcat.txt");
        if(logFile.exists()) { logFile.delete(); }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            // Create info line at beginning of file if it is not already present
            buf.append(getSystemInfo());
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = Environment.getExternalStorageDirectory() + "/COR Church/logcat.txt";
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-v", "time", "-f", filePath});
        } catch(IOException e) {
            Log.d("MA.createLogFile()", "Logcat error. " + e.getMessage());
        }
    }

    /** Check if user is logged in, get groups */
    public static String[] getGroups() {
        Socket mSocket;
        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);

            // Socket emitters
            mSocket.on("groups", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    String data = args[0].toString();
                    data = data.replace("[", "");
                    data = data.replace("]", "");
                    data = data.replace("\"", "");
                    groups = data.split(",");

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

            Log.i("MA.checkLoggedIn()", "Socket Connected to " + Constants.IP_ADDRESS);
        } catch (URISyntaxException e) { Log.d("MA.checkLoggedIn()", e.getMessage()); }

        return groups;
    }
}