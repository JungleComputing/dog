package ibis.deploy.library.gui;

import ibis.deploy.library.IbisPool;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class IbisPoolSelectionComponent implements SelectionComponent {

    private TableLayout mLayout;

    private final CheckBox mNewPoolCheckBox;

    private final CheckBox mClosedWorldCheckBox;

    private final EditText mSelectPoolNameEditText;

    private final EditText mSelectPoolSizeEditText;

    private final Spinner mSelectPoolNameSpinner;

    public IbisPoolSelectionComponent(Context context) {
        mLayout = new TableLayout(context);
        mLayout.setStretchAllColumns(true);
        // set border/title?

        final TextView newPoolTextView = new TextView(context);
        mNewPoolCheckBox = new CheckBox(context);
        mNewPoolCheckBox.setChecked(true);
        final TextView selectPoolNameTextView = new TextView(context);
        final TextView closedWorldTextView = new TextView(context);
        mClosedWorldCheckBox = new CheckBox(context);
        final TextView selectPoolSizeTextView = new TextView(context);
        mSelectPoolNameEditText = new EditText(context);
        mSelectPoolSizeEditText = new EditText(context);
        mSelectPoolNameSpinner = new Spinner(context);

        final TableRow newPoolRow = new TableRow(context);
        final TableRow selectPoolNameRow = new TableRow(context);
        // TODO make this something else (LinearLayout?)
        final LinearLayout closedWorldContainerRow = new LinearLayout(context);
        closedWorldContainerRow.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout closedWorldRow = new LinearLayout(context);
        final LinearLayout selectPoolSizeRow = new LinearLayout(context);

        newPoolRow.addView(newPoolTextView);
        newPoolRow.addView(mNewPoolCheckBox);

        selectPoolNameRow.addView(selectPoolNameTextView);
        selectPoolNameRow.addView(mSelectPoolNameEditText);

        closedWorldRow.addView(closedWorldTextView);
        closedWorldRow.addView(mClosedWorldCheckBox);

        selectPoolSizeRow.addView(selectPoolSizeTextView);
        selectPoolSizeRow.addView(mSelectPoolSizeEditText);

        closedWorldContainerRow.addView(closedWorldRow, new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        mLayout.addView(newPoolRow);
        mLayout.addView(selectPoolNameRow);
        mLayout.addView(closedWorldContainerRow);

        newPoolTextView.setText("new ibis pool");
        selectPoolNameTextView.setText("pool name: ");
        selectPoolSizeTextView.setText("pool size: ");
        closedWorldTextView.setText("closed world");
        mSelectPoolNameEditText.setHint("pool-0");
        mSelectPoolSizeEditText.setHint("1");
        ArrayAdapter<IbisPool> spinnerAdapter = new ArrayAdapter<IbisPool>(
                context, android.R.layout.simple_spinner_item,
                new ArrayList<IbisPool>());
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setNotifyOnChange(true);
        mSelectPoolNameSpinner.setAdapter(spinnerAdapter);

        mNewPoolCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton button,
                            boolean checked) {
                        if (checked) {
                            // button is checked, it should contain the edit
                            // text!
                            selectPoolNameRow
                                    .removeView(mSelectPoolNameSpinner);
                            selectPoolNameRow.addView(mSelectPoolNameEditText);
                            mLayout.addView(closedWorldContainerRow);
                        } else {
                            selectPoolNameRow
                                    .removeView(mSelectPoolNameEditText);
                            selectPoolNameRow.addView(mSelectPoolNameSpinner);
                            mLayout.removeView(closedWorldContainerRow);
                        }
                    }

                });
        mClosedWorldCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton button,
                            boolean checked) {
                        if (checked) {
                            // button is checked, it should contain the edit
                            // text!
                            closedWorldContainerRow.addView(selectPoolSizeRow);
                        } else {
                            closedWorldContainerRow
                                    .removeView(selectPoolSizeRow);
                        }
                    }

                });

    }

    @SuppressWarnings("unchecked")
    public Object[] getValues() {
        if (mNewPoolCheckBox.isChecked()) {
            CharSequence poolName = (mSelectPoolNameEditText.getText() == null || mSelectPoolNameEditText
                    .getText().toString().equals("")) ? mSelectPoolNameEditText
                    .getHint() : mSelectPoolNameEditText.getText();
            IbisPool newPool = new IbisPool(poolName.toString(),
                    mClosedWorldCheckBox.isChecked(), ((mClosedWorldCheckBox
                            .isChecked()) ? Integer
                            .parseInt(mSelectPoolSizeEditText.getText()
                                    .toString()) : -1));
            ((ArrayAdapter<IbisPool>) mSelectPoolNameSpinner.getAdapter())
                    .add(newPool);
            return new Object[] { newPool };
        } else {
            return new Object[] { mSelectPoolNameSpinner.getSelectedItem() };
        }
    }

    public View getView() {
        return mLayout;
    }

    public void update() {
    }

}
