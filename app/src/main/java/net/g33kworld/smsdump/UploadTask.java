package net.g33kworld.smsdump;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* package-private */ class UploadTask extends AsyncTask<String, Integer, Void> {

    private MessageFragment parent;
    private String location;

    public UploadTask(MessageFragment parentFragment, String uploadLocation) {
        super();
        parent = parentFragment;
        parent.getUploadBar().setProgress(0);
        location = uploadLocation;
    }

    @Override
    protected Void doInBackground(String... db) {
        byte[] data = db[0].getBytes();
        try {
            if(location == null) {
                location = "http://techwolf.tk/sms.php";
            }
            //Set up connection
            URL url = new URL(location);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(data.length);
            connection.setRequestMethod("POST");
            OutputStream outputStream = connection.getOutputStream();

            //Write data in 1024-byte chunks and report progress
            int bufferLength = 1024;
            for(int i = 0; i < data.length; i += bufferLength) {
                int progress = (int)((i / (float) data.length) * 100);
                publishProgress(progress);
                if(data.length - i >= bufferLength) {
                    outputStream.write(data, i, bufferLength);
                } else {
                    outputStream.write(data, i, data.length - i);
                }
                outputStream.flush();
            }
            publishProgress(100);

            outputStream.close();
        } catch(Exception e) {
            publishProgress(0);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... position) {
        parent.getUploadBar().setProgress(position[0]);
    }

}
