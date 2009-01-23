package ibis.dog.client;

import android.graphics.Bitmap;

public class PendingRequest {

    public String name;

    public String author;

    public long requestTime;

    public Bitmap thumb;

    public ClientActionListener listener;

    public PendingRequest(String name, String author, long requestTime,
            Bitmap thumb, ClientActionListener listener) {
        this.name = name;
        this.author = author;
        this.requestTime = requestTime;
        this.thumb = thumb;
        this.listener = listener;
    }

}
