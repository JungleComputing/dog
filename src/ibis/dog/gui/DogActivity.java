package ibis.dog.gui;

import ibis.dog.client.Client;
import ibis.dog.client.ClientActionListener;
import ibis.dog.client.ClientListener;
import ibis.dog.client.ServerData;
import ibis.dog.shared.JPEGImage;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class DogActivity extends DogActivityStandalone {

    private Client mClient;

    private JPEGImage mJPEGImage;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        System.setProperty("ibis.server.address", extras
                .getString("ibis.server.address"));
        System.setProperty("ibis.server.hub.addresses", extras
                .getString("ibis.server.hub.addresses"));
        System
                .setProperty("ibis.pool.name", extras
                        .getString("ibis.pool.name"));
        System.setProperty("ibis.location", extras.getString("ibis.location"));
        System.setProperty("ibis.deploy.job.id", extras
                .getString("ibis.deploy.job.id"));
        System.setProperty("ibis.deploy.job.size", extras
                .getString("ibis.deploy.job.size"));
        mClient = new Client(this, mPreview.getCamera());
        // make a listener for the client, that shows updates of compute servers
        // on the screen.

        final List<Integer> knownServerIDs = new ArrayList<Integer>();
        final Handler statusHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(DogActivity.this, msg.obj.toString(),
                        Toast.LENGTH_LONG).show();
            }
        };
        mClient.registerListener(new ClientListener() {

            public void updateServers(ServerData[] servers) {
                String updateReportHeader = "  -- server update report --";
                String updateReportBody = "";
                // match server list again known list, display changes!

                // check for removed servers
                for (Integer knownServerID : knownServerIDs) {
                    boolean removed = true;
                    String serverName = null;
                    for (ServerData server : servers) {
                        if (knownServerID == server.serverID) {
                            // found!
                            removed = false;
                            serverName = server.getName();
                            break;
                        }
                    }
                    if (removed) {
                        knownServerIDs.remove(knownServerID);
                        updateReportBody += "\nmm-server '" + serverName
                                + "' removed.";
                    }
                }

                // check for new servers
                for (ServerData server : servers) {
                    boolean added = true;
                    for (Integer knownServerID : knownServerIDs) {
                        if (knownServerID == server.serverID) {
                            // found!
                            added = false;
                            break;
                        }
                    }
                    if (added) {
                        knownServerIDs.add(server.serverID);
                        server.setConnected(true);
                        updateReportBody += "\nmm-server '" + server.getName()
                                + "' added.";
                    }
                }
                if (!updateReportBody.equals("")) {
                    statusHandler.sendMessage(Message.obtain(statusHandler, 0,
                            updateReportHeader + updateReportBody));
                    System.out.println("update: " + updateReportBody);
                }
            }
        });
        mClient.start();
    }

    void takePicture(final PictureHandler pictureHandler) {
        mPreview.getCamera().stopPreview();
        Camera.Parameters params = mPreview.getCamera().getParameters();
        System.out.println(params.getPictureSize().width + "x"
                + params.getPictureSize().height);
        params.setPictureSize(
                2048 / (int) Math.pow(2, Math.min(mImageSize, 2)),
                1536 / (int) Math.pow(2, Math.min(mImageSize, 2)));
        System.out.println(params.getPictureSize().width + "x"
                + params.getPictureSize().height);
        mPreview.getCamera().setParameters(params);
        mPreview.getCamera().takePicture(null, null,
                new Camera.PictureCallback() {

                    public void onPictureTaken(byte[] data, Camera camera) {
                        mStartPreviewHandler.sendEmptyMessage(1);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = Math.max(1, (int) Math.pow(2,
                                (mImageSize - 2)));
                        mThumb = BitmapFactory.decodeByteArray(data, 0,
                                data.length, options);
                        System.out.println("thumb: " + mThumb.getWidth() + "x"
                                + mThumb.getHeight() + " for imagesize: "
                                + mImageSize);
                        mJPEGImage = new JPEGImage(camera.getParameters()
                                .getPictureSize().width, camera.getParameters()
                                .getPictureSize().height, data);
                        pictureHandler.pictureTaken();
                    }
                });

    }

    protected void learn(final Handler dialogHandler, final String name,
            final String author, final long createdDate) {
        try {
            mStartOperation = System.currentTimeMillis();
            mClient.learn(name, author, mJPEGImage, new ClientActionListener() {

                public void replyReceived(Cursor cursor) {
                    mCursor = cursor;
                    dialogHandler.sendEmptyMessage(0);
                }

            });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to learn: " + e, Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    protected void recognize(final Handler dialogHandler) {
        try {
            mStartOperation = System.currentTimeMillis();
            mClient.recognize(mJPEGImage, new ClientActionListener() {

                public void replyReceived(Cursor cursor) {
                    mCursor = cursor;
                    dialogHandler.sendEmptyMessage(0);
                }

            });
        } catch (Exception e) {
            Toast
                    .makeText(this, "Failed to recognize: " + e,
                            Toast.LENGTH_LONG);
            e.printStackTrace();
        }

    }

}