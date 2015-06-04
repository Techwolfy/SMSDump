package net.g33kworld.smsdump;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

//Basic container for retained data
public class MessageFragment extends Fragment {

    private TextView text;
    private ProgressBar progress;
    private ProgressBar upload;
    private String[] messages;
    private MessagesTask[] messagesTasks;
    private UploadTask uploadTask;
    private boolean isLoading;
    private boolean[] loaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Retain through config changes
        setRetainInstance(true);

        messages = new String[3];   //{INBOX, SENT, DRAFTS}
        messagesTasks = new MessagesTask[3];
        uploadTask = null;
        isLoading = false;
        loaded = new boolean[] {false, false, false};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View v;
        if(PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).getBoolean("darkTheme", false)) {
            //Switch to dark theme
            LayoutInflater themeInflater = inflater.cloneInContext(new ContextThemeWrapper(getActivity(), android.support.v7.appcompat.R.style.Theme_AppCompat));
            v = themeInflater.inflate(R.layout.message_fragment, container, false);
        } else {
            //Use default theme
            v = inflater.inflate(R.layout.message_fragment, container, false);
        }

        //Retrieve and initialize TextView and ProgressBar objects
        text = (TextView)v.findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());
        progress = (ProgressBar)v.findViewById(R.id.progress);
        upload = (ProgressBar)v.findViewById(R.id.upload);

        return v;
    }

    protected boolean isLoading() {
        return isLoading;
    }

    protected void loadMessages(String... uris) {
        displayText(getResources().getString(R.string.loadingMessages) + "\n\n");
        for(String uri : uris) {
            if (SMSDump.uriToIndex(uri) == -1) {
                Toast.makeText(this.getActivity(), R.string.errorUri + uri, Toast.LENGTH_SHORT).show();
                continue;
            } else {
                if (messagesTasks[SMSDump.uriToIndex(uri)] != null) {
                    messagesTasks[SMSDump.uriToIndex(uri)].cancel(true);
                }
                messagesTasks[SMSDump.uriToIndex(uri)] = (MessagesTask)new MessagesTask(this).execute(uri);
            }
            isLoading = true;
        }
    }

    protected void messagesLoaded(String uri, String data) {
        if(SMSDump.uriToIndex(uri) == -1) {
            Toast.makeText(this.getActivity(), R.string.errorUri + uri, Toast.LENGTH_SHORT).show();
            return;
        } else {
            messages[SMSDump.uriToIndex(uri)] = data;
            loaded[SMSDump.uriToIndex(uri)] = true;
            ((SMSDump)getActivity()).messagesLoaded(uri);
        }
    }

    protected String collateMessages(String... uris) {
        String collate = "";
        for(String uri : uris) {
            if(SMSDump.uriToIndex(uri) == -1) {
                Toast.makeText(this.getActivity(), R.string.errorUri + uri, Toast.LENGTH_SHORT).show();
                return null;
            } else if(!loaded[SMSDump.uriToIndex(uri)]) {
                Toast.makeText(this.getActivity(), R.string.errorNotLoaded, Toast.LENGTH_SHORT).show();
                return null;
            } else {
                collate += messages[SMSDump.uriToIndex(uri)];
                collate += "\n";
            }
        }
        return collate;
    }

    protected File saveMessages(String saveLocation, String... uris) {
        String data = collateMessages(uris);
        if(data != null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            displayText(getResources().getString(R.string.savingMessages) + "\n");
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), saveLocation);
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.write(data.getBytes());
                out.flush();
                out.close();
                displayText(getResources().getString(R.string.saveComplete) + "\n");
            } catch(FileNotFoundException e) {
                Toast.makeText(this.getActivity(), getResources().getString(R.string.errorFileOpen), Toast.LENGTH_SHORT).show();
                return null;
            } catch(IOException e) {
                Toast.makeText(this.getActivity(), getResources().getString(R.string.errorFileWrite), Toast.LENGTH_SHORT).show();
                return null;
            }
            return file;
        }
        return null;
    }

    protected void emailMessages(String saveLocation, String... uris) {
        File file = saveMessages(saveLocation, uris);
        if(file == null) {
            return;
        }
        String emailBody = getResources().getString(R.string.emailBody);
        emailBody += DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date());
        emailBody += ".\n";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.emailTitle));
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.setType("message/rfc822");
        startActivity(intent);
    }

    protected void uploadMessages(String uploadLocation, String... uris) {
        String data = collateMessages(uris);
        if(data != null) {
            displayText(getResources().getString(R.string.uploadingMessages) + "\n");
            uploadTask = (UploadTask)new UploadTask(this, uploadLocation).execute(data);
        }
    }

    protected ProgressBar getProgressBar() {
        return progress;
    }

    public ProgressBar getUploadBar() {
        return upload;
    }

    public void displayText(String data) {
        text.append(data);
    }
}