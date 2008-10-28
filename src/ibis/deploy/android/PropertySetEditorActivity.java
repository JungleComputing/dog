package ibis.deploy.android;

import ibis.deploy.library.PropertyCategory;
import ibis.deploy.library.PropertySet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PropertySetEditorActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_10);
        TableLayout table = (TableLayout) findViewById(R.id.table);

        Bundle extras = getIntent().getExtras();
        final PropertySet propertySet = (PropertySet) extras
                .getParcelable("propertySet");
        PropertySet defaultPropertySet = null;
        if (extras.containsKey("defaultPropertySet")) {
            defaultPropertySet = (PropertySet) extras
                    .getParcelable("defaultPropertySet");
        }
        if (propertySet != null) {
            for (PropertyCategory category : propertySet.getCategories()) {
                // make a new heading for the category
                TableRow heading = new TableRow(this);
                heading.setBackgroundColor(Color.DKGRAY);
                heading.setLayoutParams(new TableRow.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                TextView categoryHeading = new TextView(this);
                categoryHeading.setText(category.getName());
                categoryHeading.setGravity(Gravity.CENTER_HORIZONTAL);
                categoryHeading.setTextSize(25);
                heading.addView(categoryHeading, new TableRow.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                table.addView(heading, new TableLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                for (String key : category.getData().keySet()) {
                    // now add the key value pairs for this category
                    TableRow property = new TableRow(this);
                    property
                            .setLayoutParams(new TableRow.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    TextView propertyKey = new TextView(this);
                    propertyKey.setText(key);
                    property.addView(propertyKey,
                            new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    EditText propertyValue = new EditText(this);
                    propertyValue.addTextChangedListener(new MyTextWatcher(
                            category, key));

                    if (category.getData().get(key) == null
                            && defaultPropertySet != null
                            && defaultPropertySet.getCategory(
                                    category.getName()).getData().get(key) != null) {
                        propertyValue.setHint(defaultPropertySet.getCategory(
                                category.getName()).getData().get(key));
                    }
                    propertyValue.setText(category.getData().get(key));
                    property.addView(propertyValue,
                            new TableRow.LayoutParams(LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                    table.addView(property,
                            new TableLayout.LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                }
            }
        }
        Button confirmButton = (Button) findViewById(R.id.Button01);
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle extras = getIntent().getExtras();
                extras.remove("propertySet");
                extras.putParcelable("propertySet", propertySet);
                Intent mIntent = new Intent();
                mIntent.putExtras(extras);
                setResult(RESULT_OK, mIntent);
                finish();
            }

        });
    }

    private class MyTextWatcher implements TextWatcher {
        PropertyCategory category;

        String key;

        public MyTextWatcher(PropertyCategory category, String key) {
            this.category = category;
            this.key = key;
        }

        public void afterTextChanged(Editable s) {
            category.getData().put(key, s.toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {

        }
    }
}
