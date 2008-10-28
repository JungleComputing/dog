package ibis.deploy.library.gui;

import ibis.deploy.library.Cluster;
import ibis.deploy.library.Deployer;
import ibis.deploy.library.Grid;

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

public class ClusterSelectionComponent implements SelectionComponent {

    private TableLayout layout;

    private EditText mResourceCount;

    private final Spinner mGridSpinner;

    private final Spinner mClusterSpinner;

    private final Deployer mDeployer;

    public ClusterSelectionComponent(Deployer deployer, Context context) {
        this.mDeployer = deployer;
        layout = new TableLayout(context);
        layout.setStretchAllColumns(true);
        // set border/title?

        // create the first row containing the group selection
        TableRow groupRow = new TableRow(context);

        // add the text to the first row
        TextView gridText = new TextView(context);
        gridText.setText("grid: ");
        groupRow.addView(gridText);

        // and create the spinner
        mGridSpinner = new Spinner(context);

        // the initial grid (might be left out in the
        // future...)
        List<Grid> grids = new ArrayList<Grid>();
        for (Grid grid : deployer.getGrids()) {
            grids.add(grid);
        }

        // create the adapter belonging to the spinner
        final ArrayAdapter<Grid> gridAdapter = new ArrayAdapter<Grid>(context,
                android.R.layout.simple_spinner_item, grids);
        gridAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gridAdapter.setNotifyOnChange(true);
        mGridSpinner.setAdapter(gridAdapter);

        // finally add the spinner to the row
        groupRow.addView(mGridSpinner);

        // and add the whole row to the table
        layout.addView(groupRow);

        // second row
        final TableRow clusterRow = new TableRow(context);

        // add the text
        TextView clusterText = new TextView(context);
        clusterText.setText("cluster: ");
        clusterRow.addView(clusterText);

        // create the spinner
        mClusterSpinner = new Spinner(context);
        final List<Cluster> clusters = new ArrayList<Cluster>();
        final ArrayAdapter<Cluster> clusterAdapter = new ArrayAdapter<Cluster>(
                context, android.R.layout.simple_spinner_item, clusters);
        clusterAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clusterAdapter.setNotifyOnChange(true);
        mClusterSpinner.setAdapter(clusterAdapter);
        clusterRow.addView(mClusterSpinner);

        // and add the row to the table
        layout.addView(clusterRow);

        // third row
        final TableRow resourceCountRow = new TableRow(context);

        // add the text
        TextView resourceCountText = new TextView(context);
        resourceCountText.setText("resource count: ");
        resourceCountRow.addView(resourceCountText);

        // create and add the edit text
        mResourceCount = new EditText(context);
        mResourceCount.setHint("1");
        resourceCountRow.addView(mResourceCount);

        // and add the row to the table
        layout.addView(resourceCountRow);

        // add a listener to the group spinner, in order to change the
        // cluster spinner according to the selected group
        mGridSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                        clusterAdapter.clear();
                        for (Cluster cluster : gridAdapter.getItem(position)
                                .getClusters()) {
                            clusterAdapter.add(cluster);
                        }

                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }

                });
    }

    public Object[] getValues() {
        CharSequence resourceCount = (mResourceCount.getText() == null
                || mResourceCount.getText().toString().equals("") ? mResourceCount
                .getHint()
                : mResourceCount.getText());

        return new Object[] { mClusterSpinner.getSelectedItem(), resourceCount };

    }

    public View getView() {
        return layout;
    }

    @SuppressWarnings("unchecked")
    public void update() {
        ArrayAdapter<Grid> gridAdapter = ((ArrayAdapter<Grid>) mGridSpinner
                .getAdapter());
        gridAdapter.clear();
        for (Grid grid : mDeployer.getGrids()) {
            gridAdapter.add(grid);
        }
        Grid selected = (Grid) mGridSpinner.getSelectedItem();
        ArrayAdapter<Cluster> clusterAdapter = ((ArrayAdapter<Cluster>) mClusterSpinner
                .getAdapter());
        clusterAdapter.clear();
        for (Cluster cluster : selected.getClusters()) {
            clusterAdapter.add(cluster);
        }
    }

}
