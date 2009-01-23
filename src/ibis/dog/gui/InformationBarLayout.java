package ibis.dog.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InformationBarLayout extends LinearLayout {

    private ImageView mImage;

    private LinearLayout mContentCenter;

    private LinearLayout mContentRight;

    public InformationBarLayout(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        mImage = new ImageView(context);
        mImage.setAdjustViewBounds(true);
        mImage.setMaxHeight(50);
        mImage.setMaxWidth(100);
        addView(mImage);

        mContentCenter = new LinearLayout(context);
        addView(mContentCenter);

        mContentRight = new LinearLayout(context);
        addView(mContentRight);
    }

    public void setToLearn(Bitmap thumb, EditText editText,
            View.OnClickListener listener) {
        mImage.setImageBitmap(thumb);
        mContentCenter.removeAllViews();
        editText.setHint("Enter Object Name");
        mContentCenter.addView(editText);
        mContentRight.removeAllViews();
        Button learnButton = new Button(getContext());
        learnButton.setText("Learn");
        learnButton.setOnClickListener(listener);
        mContentRight.addView(learnButton);
    }

    public void setToLearning(Bitmap thumb) {
        mImage.setImageBitmap(thumb);
        mContentCenter.removeAllViews();
        TextView textView = new TextView(getContext());
        textView.setText("Learning ...");
        mContentCenter.addView(textView);
        mContentRight.removeAllViews();
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        mContentRight.addView(progressBar);
    }

    public void setToLearningDone(Bitmap thumb, long learnTime,
            View.OnClickListener listener) {
        mImage.setImageBitmap(thumb);
        mContentCenter.removeAllViews();
        TextView textView = new TextView(getContext());
        textView.setText("Done (" + learnTime / 1000.0 + " sec)");
        mContentCenter.addView(textView);
        mContentRight.removeAllViews();
        Button closeButton = new Button(getContext());
        closeButton.setText("Close");
        closeButton.setOnClickListener(listener);
        mContentRight.addView(closeButton);
    }
}
