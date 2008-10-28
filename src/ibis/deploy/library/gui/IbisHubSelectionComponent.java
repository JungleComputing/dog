package ibis.deploy.library.gui;

import ibis.deploy.library.Server;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class IbisHubSelectionComponent implements SelectionComponent {

    private TableLayout layout;

    private final CheckBox mNewServerCheckBox;

    private final EditText mSelectServerEditText;

    private final Spinner mSelectServerSpinner;

    private final CheckBox mNewHubCheckBox;

    private final EditText mSelectHubEditText;

    private final Spinner mSelectHubSpinner;

    public IbisHubSelectionComponent(Context context) {
        layout = new TableLayout(context);
        layout.setStretchAllColumns(true);
        // set border/title?

        TextView newServerTextView = new TextView(context);
        mNewServerCheckBox = new CheckBox(context);
        mNewServerCheckBox.setChecked(true);
        TextView selectServerTextView = new TextView(context);
        mSelectServerEditText = new EditText(context);
        mSelectServerSpinner = new Spinner(context);

        TextView newHubTextView = new TextView(context);
        mNewHubCheckBox = new CheckBox(context);
        TextView selectHubTextView = new TextView(context);
        mSelectHubEditText = new EditText(context);
        mSelectHubSpinner = new Spinner(context);

        final TableRow newServerRow = new TableRow(context);
        final TableRow selectServerRow = new TableRow(context);

        final TableRow newHubRow = new TableRow(context);
        final TableRow selectHubRow = new TableRow(context);

        newServerRow.addView(newServerTextView);
        newServerRow.addView(mNewServerCheckBox);

        newHubRow.addView(newHubTextView);
        newHubRow.addView(mNewHubCheckBox);

        selectServerRow.addView(selectServerTextView);
        selectServerRow.addView(mSelectServerEditText);

        selectHubRow.addView(selectHubTextView);
        selectHubRow.addView(mSelectHubSpinner);

        layout.addView(newServerRow);
        layout.addView(selectServerRow);
        // layout.addView(newHubRow);
        // layout.addView(selectHubRow);

        newServerTextView.setText("new server/hub");
        mNewServerCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton button,
                            boolean checked) {
                        if (checked) {
                            // button is checked, it should contain the edit
                            // text!
                            selectServerRow.removeView(mSelectServerSpinner);
                            selectServerRow.addView(mSelectServerEditText);
                            layout.removeView(selectHubRow);
                            layout.removeView(newHubRow);
                        } else {
                            selectServerRow.removeView(mSelectServerEditText);
                            selectServerRow.addView(mSelectServerSpinner);
                            layout.addView(newHubRow);
                            layout.addView(selectHubRow);
                        }
                    }

                });
        selectServerTextView.setText("server: ");
        mSelectServerEditText.setHint("new-server");
        ArrayAdapter<Server> serverSpinnerAdapter = new ArrayAdapter<Server>(
                context, android.R.layout.simple_spinner_item,
                new ArrayList<Server>());
        serverSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinnerAdapter.setNotifyOnChange(true);
        mSelectServerSpinner.setAdapter(serverSpinnerAdapter);

        ArrayAdapter<Server> hubSpinnerAdapter = new ArrayAdapter<Server>(
                context, android.R.layout.simple_spinner_item,
                new ArrayList<Server>());
        hubSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hubSpinnerAdapter.setNotifyOnChange(true);
        mSelectHubSpinner.setAdapter(hubSpinnerAdapter);

        newHubTextView.setText("new hub");
        mNewHubCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton button,
                            boolean checked) {
                        if (checked) {
                            // button is checked, it should contain the edit
                            // text!
                            selectHubRow.removeView(mSelectHubSpinner);
                            selectHubRow.addView(mSelectHubEditText);
                        } else {
                            selectHubRow.removeView(mSelectHubEditText);
                            selectHubRow.addView(mSelectHubSpinner);
                        }
                    }

                });
        selectHubTextView.setText("hub: ");
        mSelectHubEditText.setHint("new-hub");

    }

    @SuppressWarnings("unchecked")
    public Object[] getValues() {
        Object[] result = new Object[4];

        // 0 = existing server address

        if (mNewServerCheckBox.isChecked()) {
            result[0] = null;
            result[2] = mSelectServerEditText.getText().toString();
        } else {
            try {
                result[0] = ((Server) mSelectServerSpinner.getSelectedItem())
                        .getServerClient().getLocalAddress();
            } catch (IOException e) {
                result[0] = null;
            }
            result[2] = null;
        }
        String knownHubAddresses = "";
        for (int i = 0; i < ((ArrayAdapter<Server>) mSelectHubSpinner
                .getAdapter()).getCount(); i++) {
            try {
                knownHubAddresses += ","
                        + ((ArrayAdapter<Server>) mSelectHubSpinner
                                .getAdapter()).getItem(i).getServerClient()
                                .getLocalAddress();
            } catch (IOException e) {
            }
        }
        if (mNewHubCheckBox.isChecked()) {
            result[1] = knownHubAddresses;
            result[3] = mSelectHubEditText.getText().toString();
        } else {
            if (!mNewServerCheckBox.isChecked()) {
                try {
                    result[1] = ((Server) mSelectHubSpinner.getSelectedItem())
                            .getServerClient().getLocalAddress()
                            + knownHubAddresses;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    result[1] = knownHubAddresses;
                }
            } else {
                result[1] = knownHubAddresses;
            }
            result[3] = null;
        }
        return result;
    }

    public View getView() {
        return layout;
    }

    public void update() {
    }

    @SuppressWarnings("unchecked")
    public void addServer(Server newServer) {
        ((ArrayAdapter<Server>) mSelectHubSpinner.getAdapter()).add(newServer);
        ((ArrayAdapter<Server>) mSelectServerSpinner.getAdapter())
                .add(newServer);
        // invalidate ...
    }

    @SuppressWarnings("unchecked")
    public void addHub(Server newHub) {
        ((ArrayAdapter<Server>) mSelectHubSpinner.getAdapter()).add(newHub);
    }

}
