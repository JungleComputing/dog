package ibis.dog.gui;

import ibis.dog.gui.LearnedObjects.LearnedObject;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class LearnedObjectEditor extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(LearnedObject.CONTENT_URI);
        }

        setContentView(R.layout.object_edit);

        Bundle extras = getIntent().getExtras();
        ImageView image = (ImageView) findViewById(R.id.editImageView);
        Uri thumbnailUri = Uri.withAppendedPath(getIntent().getData(),
                "thumbs/" + extras.getInt("id"));
        image.setImageURI(thumbnailUri);

        final EditText nameEditText = (EditText) findViewById(R.id.nameEditText);
        nameEditText.setHint(extras.getCharSequence("name"));

        final EditText authorEditText = (EditText) findViewById(R.id.authorEditText);
        authorEditText.setHint(extras.getCharSequence("author"));

        Button editButton = (Button) findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle extras = getIntent().getExtras();
                if (nameEditText.getText().toString().equals("")
                        || nameEditText.getText() == null) {
                    extras.putString("name", nameEditText.getHint().toString());
                } else {
                    extras.putString("name", nameEditText.getText().toString());
                }
                if (authorEditText.getText().toString().equals("")
                        || authorEditText.getText() == null) {
                    extras.putString("author", authorEditText.getHint()
                            .toString());
                } else {
                    extras.putString("author", authorEditText.getText()
                            .toString());
                }
                Intent mIntent = new Intent();
                mIntent.putExtras(extras);
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
