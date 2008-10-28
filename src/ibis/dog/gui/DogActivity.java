package ibis.dog.gui;

import ibis.dog.client.Client;
import ibis.dog.client.ClientListener;
import ibis.dog.client.ServerData;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DogActivity extends Activity {
    private static final CharSequence CHANGE_MODE = "Change mode to ";

    private enum Mode {
        LEARN, RECOGNIZE
    };

    private Mode mMode = Mode.LEARN;

    private Menu mOptionsMenu;

    private Client mClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.getRootLogger().addAppender(new AndroidAppender("Dog"));
        Logger.getRootLogger().setLevel(Level.DEBUG);

        Bundle extras = getIntent().getExtras();
        System.setProperty("ibis.server.address", extras
                .getString("ibis.server.address"));
        System.setProperty("ibis.server.hub.addresses", extras
                .getString("ibis.server.hub.addresses"));
        System
                .setProperty("ibis.pool.name", extras
                        .getString("ibis.pool.name"));

        setContentView(R.layout.main);

        final Handler statusHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(DogActivity.this, msg.obj.toString(),
                        Toast.LENGTH_LONG).show();
            }
        };

        final List<Integer> knownServerIDs = new ArrayList<Integer>();

        mClient = new Client();
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
                } else {
                }
            }
        });
        mClient.start();
        Preview preview = new Preview(this);
        preview.setMinimumHeight(144);
        preview.setMinimumWidth(176);
        LinearLayout main = (LinearLayout) findViewById(R.id.top);
        main.addView(preview);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
            if (mMode == Mode.LEARN) {
                doLearn();
            } else if (mMode == Mode.RECOGNIZE) {
                doRecognize();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void doLearn() {
        final LinearLayout learnPopupView = new LinearLayout(this);
        final TextView learnPopupTextView = new TextView(this);
        final EditText learnPopupEditText = new EditText(this);
        learnPopupTextView.setText("This object is a:");
        learnPopupEditText.setHint("object name");
        learnPopupView.addView(learnPopupTextView);
        learnPopupView.addView(learnPopupEditText);

        new AlertDialog.Builder(this).setTitle("Enter object name").setView(
                learnPopupView).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mClient.learn(learnPopupEditText.getText()
                                .toString())) {
                            Toast.makeText(
                                    DogActivity.this,
                                    "I just learned a new object: "
                                            + learnPopupEditText.getText(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    DogActivity.this,
                                    "I failed to learn a new object: "
                                            + learnPopupEditText.getText(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })

        .create().show();
    }

    private void doRecognize() {
        String recognizedObject = mClient.recognize();
        Toast.makeText(
                DogActivity.this,
                recognizedObject == null ? "I don't recognize this object!"
                        : recognizedObject, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        mOptionsMenu.add(CHANGE_MODE.toString()
                + (mMode == Mode.LEARN ? Mode.RECOGNIZE : Mode.LEARN));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().startsWith(CHANGE_MODE.toString())) {
            if (mMode == Mode.LEARN) {
                mMode = Mode.RECOGNIZE;

            } else {
                mMode = Mode.LEARN;
            }
            mOptionsMenu.clear();
            mOptionsMenu.add(CHANGE_MODE.toString()
                    + (mMode == Mode.LEARN ? Mode.RECOGNIZE : Mode.LEARN));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class Preview extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder mHolder;

        Camera mCamera;

        Preview(Context context) {
            super(context);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it
            // where
            // to draw.
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFrameRate(1);
            mCamera.setParameters(parameters);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mClient.gotImage(data, mCamera.getParameters()
                            .getPreviewSize().width, mCamera.getParameters()
                            .getPreviewSize().height);
                }
            });
            mCamera.setPreviewDisplay(holder);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            // Because the CameraDevice object is not a shared resource, it's
            // very
            // important to release it when the activity is paused.
            mCamera.stopPreview();
            mCamera = null;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                int h) {
            // Now that the size is known, set up the camera parameters and
            // begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(w, h);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }

    }
}