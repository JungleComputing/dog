package ibis.deploy.library.gui;

import ibis.deploy.library.Application;
import ibis.deploy.library.ApplicationGroup;
import ibis.deploy.library.Deployer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ApplicationSelectionComponent implements SelectionComponent {

    private TableLayout layout;

    private EditText mProcessCount;

    private final Spinner mApplicationGroupSpinner;

    private final Spinner mApplicationSpinner;

    private final Deployer mDeployer;

    public ApplicationSelectionComponent(Deployer deployer, Context context) {
        this.mDeployer = deployer;
        layout = new TableLayout(context);
        layout.setStretchAllColumns(true);
        // set border/title?

        // create the first row containing the group selection
        TableRow groupRow = new TableRow(context);

        // add the text to the first row
        TextView applicationGroupText = new TextView(context);
        applicationGroupText.setText("application group: ");
        groupRow.addView(applicationGroupText);

        // and create the spinner
        mApplicationGroupSpinner = new Spinner(context);

        // the initial group of applications (might be left out in the
        // future...)
        List<ApplicationGroup> applicationGroups = new ArrayList<ApplicationGroup>();
        for (ApplicationGroup applicationGroup : deployer
                .getApplicationGroups()) {
            applicationGroups.add(applicationGroup);
        }

        // create the adapter belonging to the spinner
        final ArrayAdapter<ApplicationGroup> applicationGroupAdapter = new ArrayAdapter<ApplicationGroup>(
                context, android.R.layout.simple_spinner_item,
                applicationGroups);
        applicationGroupAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        applicationGroupAdapter.setNotifyOnChange(true);
        mApplicationGroupSpinner.setAdapter(applicationGroupAdapter);

        // finally add the spinner to the row
        groupRow.addView(mApplicationGroupSpinner);

        // and add the whole row to the table
        layout.addView(groupRow);

        // second row
        final TableRow applicationRow = new TableRow(context);

        // add the text
        TextView applicationText = new TextView(context);
        applicationText.setText("application: ");
        applicationRow.addView(applicationText);

        // create the spinner
        mApplicationSpinner = new Spinner(context);
        final List<Application> applications = new ArrayList<Application>();
        final ArrayAdapter<Application> applicationAdapter = new ArrayAdapter<Application>(
                context, android.R.layout.simple_spinner_item, applications);
        applicationAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        applicationAdapter.setNotifyOnChange(true);
        mApplicationSpinner.setAdapter(applicationAdapter);
        applicationRow.addView(mApplicationSpinner);

        // and add the row to the table
        layout.addView(applicationRow);

        // third row
        final TableRow processCountRow = new TableRow(context);

        // add the text
        TextView processCountText = new TextView(context);
        processCountText.setText("process count: ");
        processCountRow.addView(processCountText);

        // create and add the edit text
        mProcessCount = new EditText(context);
        mProcessCount.setHint("1");
        processCountRow.addView(mProcessCount);

        // and add the row to the table
        layout.addView(processCountRow);

        // add a listener to the group spinner, in order to change the
        // application spinner according to the selected group
        mApplicationGroupSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                        applicationAdapter.clear();
                        for (Application application : applicationGroupAdapter
                                .getItem(position).getApplications()) {
                            applicationAdapter.add(application);
                        }

                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }

                });
    }

    public Object[] getValues() {
        CharSequence processCount = (mProcessCount.getText() == null
                || mProcessCount.getText().toString().equals("") ? mProcessCount
                .getHint()
                : mProcessCount.getText());

        return new Object[] { mApplicationSpinner.getSelectedItem(),
                processCount };
    }

    public View getView() {
        return layout;
    }

    @SuppressWarnings("unchecked")
    public void update() {
        ArrayAdapter<ApplicationGroup> groupAdapter = ((ArrayAdapter<ApplicationGroup>) mApplicationGroupSpinner
                .getAdapter());
        groupAdapter.clear();
        for (ApplicationGroup applicationGroup : mDeployer
                .getApplicationGroups()) {
            groupAdapter.add(applicationGroup);
        }
        ApplicationGroup selected = (ApplicationGroup) mApplicationGroupSpinner
                .getSelectedItem();
        ArrayAdapter<Application> applicationAdapter = ((ArrayAdapter<Application>) mApplicationSpinner
                .getAdapter());
        applicationAdapter.clear();
        for (Application application : selected.getApplications()) {
            applicationAdapter.add(application);
        }
    }

}
