package net.g33kworld.smsdump;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.message_fragment, container, false);

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
        if(uri.equals(SMSDump.INBOX)) {
            messagesTasks[0] = (MessagesTask) new MessagesTask(this).execute(uri);
        } else if(uri.equals(SMSDump.SENT)) {
            messagesTasks[1] = (MessagesTask) new MessagesTask(this).execute(uri);
        } else if(uri.equals(SMSDump.DRAFTS)) {
            messagesTasks[2] = (MessagesTask) new MessagesTask(this).execute(uri);
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
        displayText("Loading \"" + uri + "\"...\n");
        if(uri.equals(SMSDump.INBOX)) {
            uploadTasks[0] = (UploadTask)new UploadTask(this, uploadLocation).execute(messages[0]);
        } else if(uri.equals(SMSDump.SENT)) {
            uploadTasks[1] = (UploadTask)new UploadTask(this, uploadLocation).execute(messages[1]);
        } else if(uri.equals(SMSDump.DRAFTS)) {
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