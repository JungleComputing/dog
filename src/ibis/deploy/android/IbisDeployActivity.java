package ibis.deploy.android;

import ibis.deploy.library.Application;
import ibis.deploy.library.ApplicationGroup;
import ibis.deploy.library.Cluster;
import ibis.deploy.library.Grid;
import ibis.deploy.library.IbisApplication;
import ibis.deploy.library.IbisBasedApplicationGroup;
import ibis.deploy.library.IbisBasedGrid;
import ibis.deploy.library.IbisCluster;
import ibis.deploy.library.IbisDeployer;
import ibis.deploy.library.IbisPool;
import ibis.deploy.library.PropertySet;
import ibis.deploy.library.PropertySetGroup;
import ibis.deploy.library.Server;
import ibis.deploy.library.gui.ApplicationSelectionComponent;
import ibis.deploy.library.gui.ClusterSelectionComponent;
import ibis.deploy.library.gui.IbisHubSelectionComponent;
import ibis.deploy.library.gui.IbisPoolSelectionComponent;
import ibis.deploy.library.gui.SelectionComponent;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.Hub;
import ibis.smartsockets.util.TypedProperties;
import ibis.smartsockets.viz.SmartsocketsViz;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class IbisDeployActivity extends TabActivity {

    private static final String NAME = "NAME";

    private static final String OBJECT = "OBJECT";

    private static final int DEPLOY_TAB = 0;

    private static final int MONITOR_TAB = 1;

    private static final int APPLICATION_TAB = 2;

    private static final int GRID_TAB = 3;

    private static final int ACTIVITY_EDIT = 1;

    private IbisDeployer mDeployer;

    private List<Map<String, Object>> mApplicationGroupData;

    private List<Map<String, Object>> mGridGroupData;

    private List<List<Map<String, Object>>> mApplicationChildData;

    private List<List<Map<String, Object>>> mGridChildData;

    @Override
    protected void onDestroy() {
        GAT.end();
        super.onDestroy();
    }

    private void initDeployer() {
        try {
            IbisBasedApplicationGroup group = new IbisBasedApplicationGroup(
                    "experiments", (IbisApplication) null);
            IbisApplication app1 = new IbisApplication("R.I.P.", group);
            app1.setJavaArguments("100");
            app1.setJavaMain("Sleep");
            app1.setIbisPreStage("/data/local/ipl=.");
            app1.setStdout("/data/local/out/sleep.stdout#");
            app1.setStderr("/data/local/out/sleep.stderr#");
            app1.setPreStage("/data/local/sleep=sleep");
            group.addApplication(app1);
            // IbisApplication app2 = new IbisApplication("app2", group);
            // app2.setExecutable("/other/executable");
            // group.addApplication(app2);
            mDeployer.addApplicationGroup(group);
            // IbisBasedApplicationGroup group2 = new IbisBasedApplicationGroup(
            // "my second group", (IbisApplication) null);
            // IbisApplication app3 = new IbisApplication("app3", group2);
            // app3.setExecutable("/bin/ls");
            // group2.addApplication(app3);
            // IbisApplication app4 = new IbisApplication("app4", group2);
            // app4.setExecutable("/other/executable");
            // group2.addApplication(app4);
            // mDeployer.addApplicationGroup(group2);
            IbisBasedGrid grid = new IbisBasedGrid("small", (IbisCluster) null);
            IbisCluster cluster = new IbisCluster("nobby", grid);
            cluster.setBroker(new URI("any://dyn191.roaming.few.vu.nl"));
            cluster.setBrokerAdaptors("sshtrilead");
            cluster.setFileAdaptors("sshtrilead,local");
            cluster.setWindows(false);
            cluster.setJavaPath("/usr/bin/java");
            cluster.setServerBroker(new URI("any://dyn191.roaming.few.vu.nl"));
            cluster.setServerBrokerAdaptors("sshtrilead");
            cluster.setServerFileAdaptors("sshtrilead,local");
            grid.addCluster(cluster);
            mDeployer.addGrid(grid);

        } catch (Exception e) {
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Logger.getRootLogger().addAppender(new AndroidAppender("Deploy"));
        // Logger.getRootLogger().setLevel(Level.DEBUG);

        // set a bunch of system properties which are correct on a computer, but
        // 'missing' or wrong on the phone.
        System.setProperty("user.home", "/data/data/ibis.deploy.android");
        System.setProperty("user.name", "rkemp");
        System.setProperty("user.dir", "/data/local/out");

        // initialize the deployer object
        mDeployer = new IbisDeployer();
        initDeployer();
        initGroupData();
        initChildData();

        // fill the tabhost
        TabHost tabHost = getTabHost();

        LayoutInflater.from(this).inflate(R.layout.main,
                tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("deploy")
                .setContent(R.id.deploy));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("monitor")
                .setContent(R.id.monitor));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("apps")
                .setContent(R.id.apps));
        tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator("grids")
                .setContent(R.id.grids));

        // monitoring stuff
        final LinearLayout monitorContent = (LinearLayout) findViewById(R.id.LinearLayout02);
        // final ListView monitorList = new ListView(this);
        // TextView heading = new TextView(this);
        // heading.setText("hi ha heading!");
        // monitorList.addHeaderView(heading);
        // final ArrayAdapter<TextObject> jobList = new
        // ArrayAdapter<TextObject>(
        // this, android.R.layout.simple_list_item_1,
        // new ArrayList<TextObject>());
        // jobList.setNotifyOnChange(true);
        // monitorList.setAdapter(jobList);
        // monitorContent.addView(monitorList);

        final TableLayout monitorTable = new TableLayout(this);
        monitorTable.setStretchAllColumns(true);
        TableRow heading = new TableRow(this);
        TextView jobIdTextView = new TextView(this);
        jobIdTextView.setText("job-id");
        heading.addView(jobIdTextView);
        TextView stateTextView = new TextView(this);
        stateTextView.setText("status");
        heading.addView(stateTextView);
        monitorTable.addView(heading);

        Hub tempVizHub = null;
        try {
            tempVizHub = new Hub(new TypedProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Hub vizHub = tempVizHub;
        List<DirectSocketAddress> hubAddress = new ArrayList<DirectSocketAddress>();
        hubAddress.add(vizHub.getHubAddress());
        SmartsocketsViz viz = new SmartsocketsViz(hubAddress, this);
        viz.setMinimumHeight(200);
        monitorContent.addView(viz);

        monitorContent.addView(monitorTable);

        // deployment stuff
        LinearLayout deployContent = (LinearLayout) findViewById(R.id.LinearLayout01);
        final SelectionComponent[] components = new SelectionComponent[] {
                new ApplicationSelectionComponent(mDeployer, this),
                new ClusterSelectionComponent(mDeployer, this),
                new IbisPoolSelectionComponent(this),
                new IbisHubSelectionComponent(this) };
        for (SelectionComponent selectionComponent : components) {
            deployContent.addView(selectionComponent.getView(),
                    new LayoutParams(LayoutParams.FILL_PARENT,
                            LayoutParams.WRAP_CONTENT));
            View separator = new View(this);
            separator.setMinimumHeight(3);
            separator.setBackgroundColor(Color.DKGRAY);
            deployContent.addView(separator);
            View empty = new View(this);
            empty.setMinimumHeight(7);
            empty.setBackgroundColor(Color.TRANSPARENT);
            deployContent.addView(empty);
        }
        final Button submitButton = new Button(this);
        submitButton.setText("submit");
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final IbisApplication application = (IbisApplication) components[0]
                        .getValues()[0];
                final int processCount = Integer.parseInt(components[0]
                        .getValues()[1].toString());
                final IbisCluster cluster = (IbisCluster) components[1]
                        .getValues()[0];
                final int resourceCount = Integer.parseInt(components[1]
                        .getValues()[1].toString());
                final IbisPool pool = (IbisPool) components[2].getValues()[0];
                final String existingServer = (String) components[3]
                        .getValues()[0];
                final String existingHubs = (String) components[3].getValues()[1];
                final String newServerName = (String) components[3].getValues()[2];
                final String newHubName = (String) components[3].getValues()[3];

                final TableRow submissionRow = new TableRow(
                        IbisDeployActivity.this);
                final TextView jobIdText = new TextView(IbisDeployActivity.this);
                final TextView statusText = new TextView(
                        IbisDeployActivity.this);

                final Handler statusHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        statusText.setText(msg.obj.toString());
                        statusText.invalidate();
                    }
                };

                final Handler jobIdHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        jobIdText.setText(msg.obj.toString());
                        jobIdText.invalidate();
                    }
                };

                new Thread() {
                    public void run() {
                        jobIdText.setText("n.a.");
                        statusText.setText("no status yet");
                        submissionRow.addView(jobIdText);
                        submissionRow.addView(statusText);
                        monitorTable.addView(submissionRow);

                        String existingServerAddress = existingServer;
                        String existingHubAddresses = existingHubs;
                        try {
                            if (newServerName != null) {
                                // start Server
                                Server newServer = new Server(newServerName,
                                        cluster, application);
                                newServer.startServer(new MetricListener() {

                                    public void processMetricEvent(
                                            MetricEvent event) {
                                        statusHandler
                                                .sendMessage(Message
                                                        .obtain(
                                                                statusHandler,
                                                                0,
                                                                "SERVER: "
                                                                        + event
                                                                                .getValue()
                                                                                .toString()));
                                    }
                                });
                                existingServerAddress = newServer
                                        .getServerClient().getLocalAddress();
                                existingHubAddresses = newServer
                                        .getServerClient().getLocalAddress()
                                        + existingHubAddresses;
                                ((IbisHubSelectionComponent) components[3])
                                        .addServer(newServer);
                                vizHub.addHubs(existingServerAddress);
                            } else if (newHubName != null) {
                                // start Hub
                                Server newHub = new Server(newHubName, cluster,
                                        application, existingServer);
                                newHub.startServer(new MetricListener() {

                                    public void processMetricEvent(
                                            MetricEvent event) {
                                        statusHandler
                                                .sendMessage(Message
                                                        .obtain(
                                                                statusHandler,
                                                                0,
                                                                "HUB: "
                                                                        + event
                                                                                .getValue()
                                                                                .toString()));
                                    }
                                });
                                existingHubAddresses = newHub.getServerClient()
                                        .getLocalAddress()
                                        + existingHubs;
                                ((IbisHubSelectionComponent) components[3])
                                        .addHub(newHub);
                                vizHub.addHubs(newHub.getServerClient()
                                        .getLocalAddress());
                            }
                        } catch (Exception e) {
                            // failed to start new server or hub
                            e.printStackTrace();
                        }
                        try {
                            Job job = mDeployer.deploy(application,
                                    processCount, cluster, resourceCount, pool,
                                    existingServerAddress,
                                    existingHubAddresses, new MetricListener() {

                                        public void processMetricEvent(
                                                MetricEvent event) {
                                            statusHandler
                                                    .sendMessage(Message
                                                            .obtain(
                                                                    statusHandler,
                                                                    0,
                                                                    "APP: "
                                                                            + event
                                                                                    .getValue()
                                                                                    .toString()));
                                            jobIdHandler
                                                    .sendMessage(Message
                                                            .obtain(
                                                                    jobIdHandler,
                                                                    0,
                                                                    ""
                                                                            + ((Job) event
                                                                                    .getSource())
                                                                                    .getJobID()));
                                        }
                                    });
                        } catch (GATInvocationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
        deployContent.addView(submitButton, new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        // attach a listener, so that we can do 'late updating' of the deploy
        // tab.
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            public void onTabChanged(String tab) {
                if ("tab1".equals(tab)) {
                    for (SelectionComponent selectionComponent : components) {
                        selectionComponent.update();
                    }
                }
            }

        });

        // init apps tab
        initApplicationsTab();
        // init grids tab
        initGridsTab();
    }

    private void initApplicationsTab() {
        ExpandableListView appsListView = (ExpandableListView) this
                .findViewById(R.id.appslist);
        ExpandableListAdapter appsAdapter = new SimpleExpandableListAdapter(
                this, mApplicationGroupData, R.layout.group_row,
                new String[] { NAME }, new int[] { R.id.groupname },
                mApplicationChildData, R.layout.child_row,
                new String[] { NAME }, new int[] { R.id.childname });
        appsListView.setAdapter(appsAdapter);
        appsListView
                .setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    public boolean onChildClick(ExpandableListView parent,
                            View v, int groupPosition, int childPosition,
                            long id) {
                        Intent i = new Intent(IbisDeployActivity.this,
                                PropertySetEditorActivity.class);
                        i.putExtra("groupPosition", groupPosition);
                        i.putExtra("childPosition", childPosition);
                        i.putExtra("propertySet",
                                (PropertySet) ((Map<String, Object>) parent
                                        .getExpandableListAdapter().getChild(
                                                groupPosition, childPosition))
                                        .get(OBJECT));
                        i.putExtra("defaultPropertySet",
                                ((Application) ((Map<String, Object>) parent
                                        .getExpandableListAdapter().getChild(
                                                groupPosition, childPosition))
                                        .get(OBJECT)).getGroup());

                        startActivityForResult(i, ACTIVITY_EDIT);
                        return true;
                    }
                });
        registerForContextMenu(appsListView);
    }

    private void initGridsTab() {
        ExpandableListView gridsListView = (ExpandableListView) this
                .findViewById(R.id.gridlist);

        ExpandableListAdapter gridsAdapter = new SimpleExpandableListAdapter(
                this, mGridGroupData, R.layout.group_row,
                new String[] { NAME }, new int[] { R.id.groupname },
                mGridChildData, R.layout.child_row, new String[] { NAME },
                new int[] { R.id.childname });
        gridsListView.setAdapter(gridsAdapter);
        gridsListView
                .setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    public boolean onChildClick(ExpandableListView parent,
                            View v, int groupPosition, int childPosition,
                            long id) {
                        Intent i = new Intent(IbisDeployActivity.this,
                                PropertySetEditorActivity.class);
                        i.putExtra("groupPosition", groupPosition);
                        i.putExtra("childPosition", childPosition);
                        i.putExtra("propertySet",
                                (PropertySet) ((Map<String, Object>) parent
                                        .getExpandableListAdapter().getChild(
                                                groupPosition, childPosition))
                                        .get(OBJECT));

                        i.putExtra("defaultPropertySet",
                                ((Cluster) ((Map<String, Object>) parent
                                        .getExpandableListAdapter().getChild(
                                                groupPosition, childPosition))
                                        .get(OBJECT)).getGrid());
                        startActivityForResult(i, ACTIVITY_EDIT);
                        return true;
                    }
                });
        registerForContextMenu(gridsListView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // back button hit!
        if (data == null) {
            return;
        }
        switch (requestCode) {
        case ACTIVITY_EDIT: {

            Bundle extras = data.getExtras();
            int groupPosition = extras.getInt("groupPosition");
            int childPosition = extras.getInt("childPosition");
            PropertySet propertySet = (PropertySet) extras
                    .getParcelable("propertySet");

            if (childPosition >= 0) {
                if (this.getTabHost().getCurrentTab() == APPLICATION_TAB) {
                    ((PropertySet) mApplicationChildData.get(groupPosition)
                            .get(childPosition).get(OBJECT))
                            .setPropertySet(propertySet);
                } else if (this.getTabHost().getCurrentTab() == GRID_TAB) {
                    ((PropertySet) mGridChildData.get(groupPosition).get(
                            childPosition).get(OBJECT))
                            .setPropertySet(propertySet);
                }
            } else if (groupPosition >= 0) {
                if (this.getTabHost().getCurrentTab() == APPLICATION_TAB) {
                    ((PropertySet) mApplicationGroupData.get(groupPosition)
                            .get(OBJECT)).setPropertySet(propertySet);
                } else if (this.getTabHost().getCurrentTab() == GRID_TAB) {
                    ((PropertySet) mGridGroupData.get(groupPosition)
                            .get(OBJECT)).setPropertySet(propertySet);
                }
            }
        }
        }
        this.getTabHost().invalidate();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            int packedId = ExpandableListView
                    .getPackedPositionType(((ExpandableListView.ExpandableListContextMenuInfo) menuInfo).packedPosition);
            if (packedId == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                menu.add("remove");
                menu.add("rename");
                menu
                        .add("new "
                                + ((getTabHost().getCurrentTab() == APPLICATION_TAB) ? "app"
                                        : "cluster"));
                menu
                        .add("save "
                                + ((getTabHost().getCurrentTab() == APPLICATION_TAB) ? "group"
                                        : "grid"));
            } else if (packedId == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                menu.add("edit defaults");
                menu.add("save");
                menu.add("load");
                menu.add("rename");
                menu.add("remove");
                menu
                        .add("new "
                                + ((getTabHost().getCurrentTab() == APPLICATION_TAB) ? "group"
                                        : "grid"));
                menu
                        .add("new "
                                + ((getTabHost().getCurrentTab() == APPLICATION_TAB) ? "app"
                                        : "cluster"));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        TabHost tabHost = getTabHost();
        switch (tabHost.getCurrentTab()) {
        case DEPLOY_TAB: {
            return false;
        }
        case MONITOR_TAB: {
            return false;
        }
        case APPLICATION_TAB: {
            return propertySetOnContextItemSelected(item,
                    mApplicationGroupData, mApplicationChildData);
        }
        case GRID_TAB: {
            return propertySetOnContextItemSelected(item, mGridGroupData,
                    mGridChildData);
        }
        default: {
            return false;
        }
        }
    }

    private boolean propertySetOnContextItemSelected(MenuItem item,
            final List<Map<String, Object>> groupData,
            final List<List<Map<String, Object>>> childData) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
                .getMenuInfo();
        final ExpandableListView view = (ExpandableListView) info.targetView
                .getParent();

        int childPosition = ExpandableListView
                .getPackedPositionChild(info.packedPosition);
        int groupPosition = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        Map<String, Object> tempData = null;

        if (ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            tempData = (Map<String, Object>) view.getExpandableListAdapter()
                    .getChild(groupPosition, childPosition);
        } else if (ExpandableListView
                .getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            tempData = (Map<String, Object>) view.getExpandableListAdapter()
                    .getGroup(groupPosition);
        }
        final Map<String, Object> data = tempData;

        if (item.getTitle().equals("rename")) {
            final LinearLayout renameView = new LinearLayout(this);
            final TextView renameTextView = new TextView(this);
            final EditText renameEditText = new EditText(this);
            renameTextView.setText("rename:");
            renameEditText.setHint("new name");
            renameView.addView(renameTextView);
            renameView.addView(renameEditText);

            new AlertDialog.Builder(IbisDeployActivity.this).setTitle(
                    "new name").setView(renameView).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            ((PropertySet) data.get(OBJECT))
                                    .setName(renameEditText.getText()
                                            .toString());
                            data.put(NAME, renameEditText.getText().toString());
                            view.invalidateViews();
                        }
                    })

            .create().show();
        } else if (item.getTitle().equals("remove")) {
            if (data.get(OBJECT) instanceof ApplicationGroup
                    || data.get(OBJECT) instanceof Grid) {
                // remove from deployer
                if (data.get(OBJECT) instanceof ApplicationGroup) {
                    mDeployer.removeApplicationGroup((ApplicationGroup) data
                            .get(OBJECT));
                } else {
                    mDeployer.removeGrid((Grid) data.get(OBJECT));
                }
                groupData.remove(groupPosition);
                childData.get(groupPosition).clear();
                childData.remove(groupPosition);
            } else if (data.get(OBJECT) instanceof Application
                    || data.get(OBJECT) instanceof Cluster) {
                if (data.get(OBJECT) instanceof Application) {
                    ((Application) data.get(OBJECT)).getGroup()
                            .removeApplication((Application) data.get(OBJECT));
                } else {
                    ((Cluster) data.get(OBJECT)).getGrid().removeCluster(
                            (Cluster) data.get(OBJECT));
                }
                childData.get(groupPosition).remove(childPosition);
            }
            // remove from list
            ((SimpleExpandableListAdapter) view.getExpandableListAdapter())
                    .notifyDataSetInvalidated();
            view.invalidateViews();
        } else if (item.getTitle().equals("new group")) {
            try {
                String newName = "new group";
                ApplicationGroup group = new ApplicationGroup(newName, null);
                Map<String, Object> applicationGroupData = new HashMap<String, Object>();
                applicationGroupData.put(NAME, newName);
                applicationGroupData.put(OBJECT, group);
                groupData.add(applicationGroupData);
                childData.add(new ArrayList<Map<String, Object>>());
                mDeployer.addApplicationGroup(group);
                ((SimpleExpandableListAdapter) view.getExpandableListAdapter())
                        .notifyDataSetInvalidated();
                view.invalidateViews();
            } catch (Exception e) {
                // ignore
                e.printStackTrace();
            }
        } else if (item.getTitle().equals("new grid")) {
            try {
                String newName = "new grid";
                Grid grid = new IbisBasedGrid(newName, null);
                Map<String, Object> gridData = new HashMap<String, Object>();
                gridData.put(NAME, newName);
                gridData.put(OBJECT, grid);
                groupData.add(gridData);
                childData.add(new ArrayList<Map<String, Object>>());
                mDeployer.addGrid(grid);
                ((SimpleExpandableListAdapter) view.getExpandableListAdapter())
                        .notifyDataSetInvalidated();
                view.invalidateViews();
            } catch (Exception e) {
                // ignore
                e.printStackTrace();
            }
        } else if (item.getTitle().equals("new app")) {
            try {
                String newName = "new application";
                ApplicationGroup group = null;
                if (ExpandableListView
                        .getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    group = ((Application) data.get(OBJECT)).getGroup();
                } else {
                    group = (ApplicationGroup) data.get(OBJECT);
                }
                Application application = new Application(newName, group);
                Map<String, Object> appData = new HashMap<String, Object>();
                appData.put(NAME, newName);
                appData.put(OBJECT, application);
                childData.get(groupPosition).add(appData);
                ((SimpleExpandableListAdapter) view.getExpandableListAdapter())
                        .notifyDataSetInvalidated();
                view.invalidateViews();
            } catch (Exception e) {
                // ignore
                e.printStackTrace();
            }
        } else if (item.getTitle().equals("new cluster")) {
            try {
                String newName = "new cluster";
                Grid grid = null;
                if (ExpandableListView
                        .getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    grid = ((Cluster) data.get(OBJECT)).getGrid();
                } else {
                    grid = (Grid) data.get(OBJECT);
                }
                Cluster cluster = new IbisCluster(newName, grid);
                Map<String, Object> clusterData = new HashMap<String, Object>();
                clusterData.put(NAME, newName);
                clusterData.put(OBJECT, cluster);
                childData.get(groupPosition).add(clusterData);
                ((SimpleExpandableListAdapter) view.getExpandableListAdapter())
                        .notifyDataSetInvalidated();
                view.invalidateViews();
            } catch (Exception e) {
                // ignore
                e.printStackTrace();
            }
        } else if (item.getTitle().equals("edit defaults")) {
            Intent i = new Intent(IbisDeployActivity.this,
                    PropertySetEditorActivity.class);
            i.putExtra("groupPosition", groupPosition);
            i.putExtra("childPosition", childPosition);
            i.putExtra("propertySet", (PropertySet) data.get(OBJECT));
            startActivityForResult(i, ACTIVITY_EDIT);
        } else if (item.getTitle().equals("load")) {
            String path = "/data/local/";
            final boolean application = getTabHost().getCurrentTab() == APPLICATION_TAB;
            if (application) {
                path += "applications";
            } else {
                path += "grids";
            }
            File file = new File(path);
            final File[] files = file.listFiles(new FilenameFilter() {
                public boolean accept(File file, String fileName) {
                    return fileName.endsWith(".properties");
                }
            });
            final String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                names[i] = files[i].getName();
            }

            new AlertDialog.Builder(IbisDeployActivity.this).setTitle(
                    "Choose a file").setItems(names,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                if (application) {
                                    ApplicationGroup applicationGroup = mDeployer
                                            .addApplicationGroup(files[which]
                                                    .getPath());
                                    Map<String, Object> applicationGroupData = new HashMap<String, Object>();
                                    applicationGroupData.put(NAME,
                                            applicationGroup.getName());
                                    applicationGroupData.put(OBJECT,
                                            applicationGroup);
                                    groupData.add(applicationGroupData);
                                    List<Map<String, Object>> applications = new ArrayList<Map<String, Object>>();
                                    for (Application application : applicationGroup
                                            .getApplications()) {
                                        Map<String, Object> clusterData = new HashMap<String, Object>();
                                        clusterData.put(NAME, application
                                                .getName());
                                        clusterData.put(OBJECT, application);
                                        applications.add(clusterData);
                                    }
                                    childData.add(applications);

                                } else {
                                    Grid grid = mDeployer.addGrid(files[which]
                                            .getPath());
                                    Map<String, Object> gridData = new HashMap<String, Object>();
                                    gridData.put(NAME, grid.getName());
                                    gridData.put(OBJECT, grid);
                                    groupData.add(gridData);
                                    List<Map<String, Object>> clusters = new ArrayList<Map<String, Object>>();
                                    for (Cluster cluster : grid.getClusters()) {
                                        Map<String, Object> clusterData = new HashMap<String, Object>();
                                        clusterData
                                                .put(NAME, cluster.getName());
                                        clusterData.put(OBJECT, cluster);
                                        clusters.add(clusterData);
                                    }
                                    childData.add(clusters);
                                }
                                view.invalidateViews();
                                Toast.makeText(IbisDeployActivity.this,
                                        files[which].getName() + " added!",
                                        Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).create().show();

        } else if (item.getTitle().equals("save")) {
            final LinearLayout renameView = new LinearLayout(this);
            final TextView renameTextView = new TextView(this);
            final EditText renameEditText = new EditText(this);
            renameTextView.setText("Enter file name:");
            renameEditText.setHint("filename");
            renameView.addView(renameTextView);
            renameView.addView(renameEditText);

            final String path = "/data/local/";
            final boolean application = getTabHost().getCurrentTab() == APPLICATION_TAB;
            new AlertDialog.Builder(IbisDeployActivity.this).setTitle(
                    "new name").setView(renameView).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            try {
                                ((PropertySetGroup) data.get(OBJECT)).save(path
                                        + (application ? "applications"
                                                : "grids") + "/"
                                        + renameEditText.getText().toString());
                                Toast.makeText(IbisDeployActivity.this,
                                        "saved", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    })

            .create().show();
        }

        return true;
    }

    private void initGroupData() {
        mApplicationGroupData = new ArrayList<Map<String, Object>>();
        for (ApplicationGroup group : mDeployer.getApplicationGroups()) {
            Map<String, Object> curGroupMap = new HashMap<String, Object>();
            mApplicationGroupData.add(curGroupMap);
            curGroupMap.put(NAME, group.getName());
            curGroupMap.put(OBJECT, group);
        }
        mGridGroupData = new ArrayList<Map<String, Object>>();
        for (Grid grid : mDeployer.getGrids()) {
            Map<String, Object> curGroupMap = new HashMap<String, Object>();
            mGridGroupData.add(curGroupMap);
            curGroupMap.put(NAME, grid.getName());
            curGroupMap.put(OBJECT, grid);
        }
    }

    private void initChildData() {
        mApplicationChildData = new ArrayList<List<Map<String, Object>>>();
        for (ApplicationGroup group : mDeployer.getApplicationGroups()) {
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for (Application application : group.getApplications()) {
                Map<String, Object> curChildMap = new HashMap<String, Object>();
                children.add(curChildMap);
                curChildMap.put(NAME, application.getName());
                curChildMap.put(OBJECT, application);
            }
            mApplicationChildData.add(children);
        }
        mGridChildData = new ArrayList<List<Map<String, Object>>>();
        for (Grid grid : mDeployer.getGrids()) {
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for (Cluster cluster : grid.getClusters()) {
                Map<String, Object> curChildMap = new HashMap<String, Object>();
                children.add(curChildMap);
                curChildMap.put(NAME, cluster.getName());
                curChildMap.put(OBJECT, cluster);
            }
            mGridChildData.add(children);
        }
    }
}