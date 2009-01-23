package ibis.dog.gui;

import ibis.dog.client.ObjectRecognition;
import ibis.dog.gui.LearnedObjects.LearnedObject;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.RGB24Image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import jorus.weibull.CxWeibull;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A stand alone version of the 'Dog' application. The Dog application is an
 * image recognition application. It computes mathematical representations of
 * images and is able to compare these representations with representations of
 * earlier learned images.
 * 
 * This stand alone version can only operate on very small images because of
 * hardware limitations. On the G-1 it can use images up to (64 x 48 pixels),
 * where the computation for such an image takes about 2 minutes.
 * 
 * The application can run in learn and recognize mode and can only learn or
 * recognize one object at a time.
 * 
 * @author rkemp
 */
public class DogActivityStandalone extends Activity {
    private static final int LEARNED_OBJECTS = 0;

    private static final int CHANGE_MODE_ID = 1;

    private static final int IMAGE_SIZE_ADD = 2;

    private static final int IMAGE_SIZE_SUB = 3;

    private static final int LEARN_DIALOG = 0;

    private static final int LEARNING_DIALOG = 1;

    private static final int LEARNING_DONE_DIALOG = 2;

    private static final int RECOGNIZING_DIALOG = 3;

    private static final int RECOGNIZING_DONE_DIALOG = 4;

    private enum Mode {
        /**
         * Learning mode.
         */
        LEARN,
        /**
         * Recognition mode.
         */
        RECOGNIZE
    };

    // Set the initial mode to learn
    private Mode mMode = Mode.LEARN;

    private Menu mOptionsMenu;

    protected Preview mPreview;

    // holds a thumb of the current picture that's used either for
    // recognition or for learning.
    Bitmap mThumb;

    // points to the recognized object
    Cursor mCursor;

    protected long mStartOperation;

    int mImageSize;

    // onPrepareDialog is called each time a dialog appears, whereas
    // onCreateDialog is created once per dialog id. So in this method we've to
    // change the dynamic parts of the dialogs.
    protected void onPrepareDialog(int id, Dialog dialog) {
        // update the thumb!
        ImageView thumb = (ImageView) dialog.findViewById(R.id.thumb);
        thumb.setImageBitmap(mThumb);
        switch (id) {
        case RECOGNIZING_DONE_DIALOG:
            ImageView image = (ImageView) dialog
                    .findViewById(R.id.recognizedObject);
            TextView text = (TextView) dialog.findViewById(R.id.text);
            text.setText(mCursor.getString(mCursor
                    .getColumnIndex(LearnedObject.OBJECT_NAME))
                    + "\n"
                    + mCursor.getString(mCursor
                            .getColumnIndex(LearnedObject.AUTHOR))
                    + "\n"
                    + ((System.currentTimeMillis() - mStartOperation) / 1000.0)
                    + " s");
            int row = mCursor.getInt(mCursor.getColumnIndex(LearnedObject._ID));
            Uri thumbnailUri = Uri.withAppendedPath(LearnedObject.CONTENT_URI,
                    "thumbs/" + row);
            image.setImageURI(thumbnailUri);
            break;
        case LEARNING_DONE_DIALOG:
            TextView time = (TextView) dialog.findViewById(R.id.text);
            time.setText("("
                    + ((System.currentTimeMillis() - mStartOperation) / 1000.0)
                    + " s)");
            break;
        }

    }

    // onPrepareDialog is called each time a dialog appears, whereas
    // onCreateDialog is created once per dialog id. So in this method we've to
    // initialize the static parts of the dialogs.
    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);
        switch (id) {
        case LEARN_DIALOG:
            final View learnView = factory.inflate(R.layout.learndialog, null);
            final ImageButton button = (ImageButton) learnView
                    .findViewById(R.id.learnButton);
            final EditText editText = (EditText) learnView
                    .findViewById(R.id.learnEditText);
            button.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    DogActivityStandalone.this.dismissDialog(LEARN_DIALOG);
                    DogActivityStandalone.this.showDialog(LEARNING_DIALOG);
                    final String name = editText.getText().toString();
                    // TODO: get a more decent author name... (maybe gmail
                    // username?)
                    final String author = "Mii";
                    final long createdDate = System.currentTimeMillis();

                    final Handler dialogHandler = new Handler() {

                        public void handleMessage(Message msg) {
                            DogActivityStandalone.this
                                    .dismissDialog(LEARNING_DIALOG);
                            DogActivityStandalone.this
                                    .showDialog(LEARNING_DONE_DIALOG);
                        }

                    };

                    learn(dialogHandler, name, author, createdDate);

                }
            });
            return new AlertDialog.Builder(DogActivityStandalone.this)
                    .setTitle(R.string.learn).setView(learnView).create();
        case LEARNING_DIALOG:
            final View learningView = factory.inflate(R.layout.learningdialog,
                    null);
            return new AlertDialog.Builder(DogActivityStandalone.this)
                    .setTitle(R.string.learning).setView(learningView).create();

        case LEARNING_DONE_DIALOG:
            final View learningDoneView = factory.inflate(
                    R.layout.learningdonedialog, null);
            final ImageButton learningDoneButton = (ImageButton) learningDoneView
                    .findViewById(R.id.closeButton);
            learningDoneButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    DogActivityStandalone.this
                            .dismissDialog(LEARNING_DONE_DIALOG);
                }
            });
            return new AlertDialog.Builder(DogActivityStandalone.this)
                    .setTitle(R.string.learning_done).setView(learningDoneView)
                    .create();
        case RECOGNIZING_DIALOG:
            final View recognizingView = factory.inflate(
                    R.layout.recognizingdialog, null);
            return new AlertDialog.Builder(DogActivityStandalone.this)
                    .setTitle(R.string.recognizing).setView(recognizingView)
                    .create();
        case RECOGNIZING_DONE_DIALOG:
            final View recognizingDoneView = factory.inflate(
                    R.layout.recognizingdonedialog, null);
            final ImageButton recognizingDoneButton = (ImageButton) recognizingDoneView
                    .findViewById(R.id.closeButton);
            recognizingDoneButton
                    .setOnClickListener(new View.OnClickListener() {

                        public void onClick(View view) {
                            DogActivityStandalone.this
                                    .dismissDialog(RECOGNIZING_DONE_DIALOG);
                        }
                    });
            return new AlertDialog.Builder(DogActivityStandalone.this)
                    .setTitle(R.string.recognition_done).setView(
                            recognizingDoneView).create();

        }

        return null;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageSize = 4;

        setContentView(R.layout.main);
        mPreview = new Preview(DogActivityStandalone.this);
        final LinearLayout main = (LinearLayout) findViewById(R.id.top);
        main.addView(mPreview);

        CxWeibull.initialize(128, 96);

        main.removeView(findViewById(R.id.splashscreen));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_CAMERA:
            if (mMode == Mode.LEARN) {
                doLearn();
            } else if (mMode == Mode.RECOGNIZE) {
                doRecognize();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_CAMERA:
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    void takePicture(final PictureHandler pictureHandler) {
        mPreview.getCamera().stopPreview();
        Camera.Parameters params = mPreview.getCamera().getParameters();
        params.setPictureSize(
                2048 / (int) Math.pow(2, Math.min(mImageSize, 2)),
                1536 / (int) Math.pow(2, Math.min(mImageSize, 2)));
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
                                + mThumb.getHeight());
                        pictureHandler.pictureTaken();

                    }
                });
    }

    FeatureVector getFeatureVectorFromBitmap(Bitmap thumb) {
        mStartOperation = System.currentTimeMillis();
        RGB24Image result = new RGB24Image(thumb.getWidth(), thumb.getHeight());
        for (int h = 0; h < result.height; h++) {
            for (int w = 0; w < result.width; w++) {
                // pixel = ARGB?
                int argbPixel = thumb.getPixel(w, h);
                result.pixels[h * result.width * 3 + w * 3] = (byte) ((argbPixel & 0x00FF0000) >> 16);
                result.pixels[h * result.width * 3 + w * 3 + 1] = (byte) ((argbPixel & 0x0000FF00) >> 8);
                result.pixels[h * result.width * 3 + w * 3 + 2] = (byte) ((argbPixel & 0x0000FF00));
            }
        }
        return getFeatureVectorFromRGB24(result);
    }

    private FeatureVector getFeatureVectorFromRGB24(RGB24Image image) {
        FeatureVector v = new FeatureVector(CxWeibull.getNrInvars(), CxWeibull
                .getNrRfields());
        CxWeibull
                .doRecognize(image.width, image.height, image.pixels, v.vector);
        System.out.println("Feature Vector: " + Arrays.toString(v.vector));

        return v;
    }

    Handler mStartPreviewHandler = new Handler() {

        public void handleMessage(Message msg) {
            mPreview.getCamera().startPreview();
        }

    };

    private void doRecognize() {
        takePicture(new PictureHandler() {

            public void pictureTaken() {
                DogActivityStandalone.this.showDialog(RECOGNIZING_DIALOG);
                final Handler dialogHandler = new Handler() {

                    public void handleMessage(Message msg) {
                        DogActivityStandalone.this
                                .dismissDialog(RECOGNIZING_DIALOG);
                        DogActivityStandalone.this
                                .showDialog(RECOGNIZING_DONE_DIALOG);
                    }

                };

                recognize(dialogHandler);

            }

        });
    }

    protected void recognize(final Handler dialogHandler) {
        new Thread() {

            public void run() {
                // convert image to feature vector
                FeatureVector v = getFeatureVectorFromBitmap(mThumb);

                // loop over existing feature vectors
                try {
                    mCursor = new ObjectRecognition().recognize(v,
                            DogActivityStandalone.this);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialogHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void doLearn() {

        takePicture(new PictureHandler() {

            public void pictureTaken() {
                DogActivityStandalone.this.showDialog(LEARN_DIALOG);
            }

        });

    }

    protected void learn(final Handler dialogHandler, final String name,
            final String author, final long createdDate) {
        new Thread() {
            public void run() {
                // convert image to feature vector
                FeatureVector v = getFeatureVectorFromBitmap(mThumb);

                // store feature vector
                ContentValues values = new ContentValues();
                values.put(LearnedObject.OBJECT_NAME, name);
                values.put(LearnedObject.AUTHOR, author);
                values.put(LearnedObject.CREATED_DATE, createdDate);
                Uri uri = DogActivityStandalone.this.getContentResolver()
                        .insert(LearnedObject.CONTENT_URI, values);
                OutputStream thumbOutStream = null;
                try {
                    thumbOutStream = DogActivityStandalone.this
                            .getContentResolver().openOutputStream(
                                    LearnedObject.getThumbUri(uri));

                    mThumb.compress(Bitmap.CompressFormat.JPEG, 100,
                            thumbOutStream);
                    thumbOutStream.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        thumbOutStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ObjectOutputStream featureVectorOutStream = null;
                try {
                    featureVectorOutStream = new ObjectOutputStream(
                            DogActivityStandalone.this.getContentResolver()
                                    .openOutputStream(
                                            LearnedObject
                                                    .getFeatureVectorUri(uri)));
                    featureVectorOutStream.writeObject(((FeatureVector) v));
                    featureVectorOutStream.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        featureVectorOutStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dialogHandler.sendEmptyMessage(0);
            }
        }.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        mOptionsMenu.add(0, LEARNED_OBJECTS, 0, R.string.learned_objects)
                .setIcon(R.drawable.folder);
        // mOptionsMenu.add(0, IMAGE_SIZE_ADD, 0, "+");
        // mOptionsMenu.add(0, IMAGE_SIZE_SUB, 0, "-");
        mOptionsMenu.add(0, CHANGE_MODE_ID, 0, R.string.change_mode).setIcon(
                R.drawable.switch_mode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CHANGE_MODE_ID:
            ImageView view = (ImageView) findViewById(R.id.mode_icon);
            if (mMode == Mode.LEARN) {
                mMode = Mode.RECOGNIZE;
                view.setImageResource(R.drawable.recognize);
            } else {
                mMode = Mode.LEARN;
                view.setImageResource(R.drawable.learn);
            }
            return true;
        case LEARNED_OBJECTS:
            startActivity(new Intent(this, LearnedObjectsList.class));
            return true;
        case IMAGE_SIZE_ADD:
            mImageSize--;
            return true;
        case IMAGE_SIZE_SUB:
            mImageSize++;
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
            mCamera = Camera.open();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it
            // where to draw.
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            mCamera.setPreviewDisplay(holder);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            // Because the CameraDevice object is not a shared resource, it's
            // very important to release it when the activity is paused.
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

        public Camera getCamera() {
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            return mCamera;
        }

    }
}