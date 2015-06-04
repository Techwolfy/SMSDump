package net.g33kworld.smsdump;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SMSDump extends AppCompatActivity {

    //Constants
    public static final String INBOX = "content://sms/inbox";
    public static final String SENT = "content://sms/sent";
    public static final String DRAFTS = "content://sms/drafts";

    //Views and variables
    private MessageFragment fragment;

    //Preferences
    private boolean autoLoad;
    private boolean autoSave;
    private String saveLocation;
    private boolean autoUpload;
    private String uploadLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Retrieve user preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        checkDefaultPrefs(prefs);
        autoLoad = prefs.getBoolean("autoLoad", true);
        autoSave = prefs.getBoolean("autoSave", false);
        saveLocation = prefs.getString("saveLocation", "SMSDump.txt");
        autoUpload = prefs.getBoolean("autoUpload", false);
        uploadLocation = prefs.getString("uploadLocation", "http://techwolf.tk/sms.php");
        //Theme must be set before super.onCreate() is called
        if(prefs.getBoolean("darkTheme", false)) {
            setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat);
        }

        //Create main View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Get a reference to the message fragment
        fragment = (MessageFragment)getFragmentManager().findFragmentById(R.id.messageFragment);

        //Load messages from inbox and sent stores
        //isLoading() returns true once the fragment has begun loading data for the first time, and is never reset
        if(autoLoad && !fragment.isLoading()) {
            fragment.loadMessages(INBOX, SENT); //TODO: DRAFTS
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar / menu item clicks here.
        int id = item.getItemId();

        if (id == R.id.actionLoad) {
            fragment.loadMessages(INBOX, SENT);                     //TODO: DRAFTS
        } else if (id == R.id.actionSave) {
            fragment.saveMessages(saveLocation, INBOX, SENT);       //TODO: DRAFTS
        } else if(id == R.id.actionEmail) {
            fragment.emailMessages(saveLocation, INBOX, SENT);      //TODO: DRAFTS
        } else if(id == R.id.actionUpload) {
            fragment.uploadMessages(uploadLocation, INBOX, SENT);   //TODO: DRAFTS
        } else if(id == R.id.actionSettings) {
            startActivity(new Intent(this, Settings.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void messagesLoaded(String uri) {
        if(autoSave) {
            fragment.saveMessages(saveLocation, uri);
        }
        if(autoUpload) {
            fragment.uploadMessages(uploadLocation, uri);
        }
    }

    private void checkDefaultPrefs(SharedPreferences prefs) {
        if(prefs.getBoolean("firstRun", true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstRun", false);
            editor.putBoolean("autoLoad", true);
            editor.putBoolean("autoSave", false);
            editor.putString("saveLocation", "SMSDump.txt");
            editor.putBoolean("autoUpload", false);
            editor.putString("uploadLocation", "http://techwolf.tk/sms.php");
            editor.putBoolean("darkTheme", false);
            editor.apply();
        }
    }

    public static int uriToIndex(String uri) {
        if(uri.equals(SMSDump.INBOX)) {
            return 0;
        } else if(uri.equals(SMSDump.SENT)) {
            return 1;
        } else if(uri.equals(SMSDump.DRAFTS)) {
            return 2;
        } else {
            return -1;
        }
    }
}
