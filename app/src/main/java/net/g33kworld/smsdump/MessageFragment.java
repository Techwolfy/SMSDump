package net.g33kworld.smsdump;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

//Basic container for retained data
public class MessageFragment extends Fragment {

    private TextView text;
    private ProgressBar progress;
    private ProgressBar upload;
    private String[] messages;
    private MessagesTask[] messagesTasks;
    private UploadTask[] uploadTasks;
    private boolean isLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Retain through config changes
        setRetainInstance(true);

        messages = new String[3];   //{INBOX, SENT, DRAFTS}
        messagesTasks = new MessagesTask[3];
        uploadTasks = new UploadTask[3];
        isLoading = false;
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

    protected void loadMessages(String uri) {
        displayText("Loading \"" + uri + "\"...\n");
        if(uri.equals(SMSDump.INBOX)) {
            if(messagesTasks[0] != null) {
                messagesTasks[0].cancel(true);
            }
            messagesTasks[0] = (MessagesTask)new MessagesTask(this).execute(uri);
        } else if(uri.equals(SMSDump.SENT)) {
            if(messagesTasks[1] != null) {
                messagesTasks[1].cancel(true);
            }
            messagesTasks[1] = (MessagesTask)new MessagesTask(this).execute(uri);
        } else if(uri.equals(SMSDump.DRAFTS)) {
            if(messagesTasks[2] != null) {
                messagesTasks[2].cancel(true);
            }
            messagesTasks[2] = (MessagesTask)new MessagesTask(this).execute(uri);
        }
        isLoading = true;
    }

    protected void messagesLoaded(String uri, String data) {
        if(uri.equals(SMSDump.INBOX)) {
            messages[0] = data;
        } else if(uri.equals(SMSDump.SENT)) {
            messages[1] = data;
        } else if(uri.equals(SMSDump.DRAFTS)) {
            messages[2] = data;
        }
        ((SMSDump)getActivity()).messagesLoaded(uri);
    }

    protected void uploadData(String uri, String uploadLocation) {
        //NOTE: Semi-silently fails if messages aren't loaded (no failure message, but no progress bar either)
        if(uri.equals(SMSDump.INBOX) && messages[0] != null) {
            displayText("Uploading \"" + uri + "\"...\n");
            uploadTasks[0] = (UploadTask)new UploadTask(this, uploadLocation).execute(messages[0]);
        } else if(uri.equals(SMSDump.SENT) && messages[1] != null) {
            displayText("Uploading \"" + uri + "\"...\n");
            uploadTasks[1] = (UploadTask)new UploadTask(this, uploadLocation).execute(messages[1]);
        } else if(uri.equals(SMSDump.DRAFTS) && messages[2] != null) {
            displayText("Uploading \"" + uri + "\"...\n");
            uploadTasks[2] = (UploadTask)new UploadTask(this, uploadLocation).execute(messages[2]);
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