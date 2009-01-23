package ibis.dog.client;

import android.database.Cursor;

public interface ClientActionListener {
    public void replyReceived(Cursor cursor);
}
