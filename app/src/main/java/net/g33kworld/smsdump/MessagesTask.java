package net.g33kworld.smsdump;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

/* package-private */ class MessagesTask extends AsyncTask<String, Integer, String[]> {

    private MessageFragment parent;
    private String data;
    private int count;

    public MessagesTask(MessageFragment parentFragment) {
        super();
        parent = parentFragment;
        parent.getProgressBar().setProgress(0);
        data = "";
        count = 0;
    }

    @Override
    protected String[] doInBackground(String... uri) {
        if(parent == null){
            return null;
        }
        Cursor cursor = parent.getActivity().getApplicationContext().getContentResolver().query(Uri.parse(uri[0]), null, null, null, null);

        if(cursor.moveToFirst()) { // must check the result to prevent exception
            parent.getProgressBar().setMax(cursor.getCount());
            count = cursor.getCount();
            String firstMessage = getMessage(cursor);

            do {
                data += "--------------------\n";
                data += getMessage(cursor);
                publishProgress(cursor.getPosition());
                if(isCancelled()) {
                    return null;
                }
            } while(cursor.moveToNext());

            return new String[] {uri[0], firstMessage, data};
        } else {
            return new String[] {uri[0], null, null};
        }
    }

    @Override
    protected void onProgressUpdate(Integer... position) {
        //Update progress max in case of config changes
        parent.getProgressBar().setMax(count);
        parent.getProgressBar().setProgress(position[0]);
    }

    @Override
    protected void onPostExecute(String[] result) {
        if(result != null && result[1] != null) {
            parent.displayText(result[0] + parent.getResources().getString(R.string.uriFirstMessage) + "\n" + result[1] + "\n");
            parent.messagesLoaded(result[0], data);
        } else if(result != null) {
            parent.displayText(parent.getResources().getString(R.string.uriNoData) + result[0] + "\n");
        } else {
            //Default constructor used; do nothing.
        }
    }

    protected String getMessage(Cursor cursor) {
        String message = "";
        for(int id = 0; id < cursor.getColumnCount(); id++) {
            message += cursor.getColumnName(id) + ":" + cursor.getString(id) + "\n";
        }
        return message;
    }
}