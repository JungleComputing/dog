package ibis.dog.client;

import ibis.deploy.Application;
import ibis.deploy.Cluster;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;
import ibis.smartsockets.direct.DirectSocketAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Deployment {

    private final static String SUPPORT_FILE = "grids/support.properties";

    private final static String SUPPORT_GRID = "support";

    private final static String SUPPORT_CLUSTER = "laptop";

    private final Deployer deployer;

    private final Grid supportGrid;

    private final Job global;

    private final HashMap<String, ComputeResource> resources = new HashMap<String, ComputeResource>();

    private String password;

    private class DeploymentThread extends Thread {

        private final SubJob job;

        DeploymentThread(SubJob job) {
            super("DeploymentThread");
            this.job = job;
        }

        public void run() {
            try {
                System.out.println("Starting deployment of " + job);

                deployer.deploy(job, global);
            } catch (Exception e) {
                System.out.println("Failed to deploy job!");
                e.printStackTrace();
            }
        }
    }

    public Deployment(String password) throws Exception {

        deployer = new Deployer(SUPPORT_FILE, SUPPORT_CLUSTER);

        // By default we load the support grid (local laptop, etc).
        // deployer.addGrid(SUPPORT_FILE);

        supportGrid = deployer.getGrid(SUPPORT_GRID);

        global = new Job("support");

        // stuff all log files in a seperate dir
        global.setOutputDirectory("logs");

        this.password = password;

        startBroker();
    }

    private void startBroker() throws Exception {

        System.out.println("Starting broker on: " + SUPPORT_GRID + "/"
                + SUPPORT_CLUSTER);

        Application brokerApp = new Application("Broker",
                "ibis.dog.broker.Broker", // main class
                null, // java options
                null, // java system properties
                null, // java arguments
                new String[] { "prestage-server", "log4j.properties" }, // app
                // prestage
                null, // post stage
                new String[] { "prestage-hub", "log4j.properties" } // hub
        // prestage
        );

        deployer.addApplication(brokerApp);

        SubJob brokerJob = new SubJob("BrokerJob");
        brokerJob.setApplication(brokerApp);
        brokerJob.setGrid(supportGrid);

        Cluster cluster = supportGrid.getCluster(SUPPORT_CLUSTER);
        brokerJob.setCluster(cluster);

        supportGrid.getCluster(SUPPORT_CLUSTER).setPassword(password);

        global.addSubJob(brokerJob);

        System.out.println("Starting broker on: " + SUPPORT_GRID + "/"
                + SUPPORT_CLUSTER);

        deployer.deploy(global);

        System.out.println("Broker started!");

        System.out.println("pool = " + global.getPoolID());
        System.out.println("address = " + global.getServerAddress());
        // System.out.println("hub address = " + global.getHubAddresses());

        // TODO: Nasty hack!!
        System.setProperty("ibis.pool.name", global.getPoolID());
        System.setProperty("ibis.server.address", global.getServerAddress());
        // System.setProperty("ibis.server.hub.addresses",
        // global.getHubAddresses());

    }

    public List<DirectSocketAddress> getHubAddresses() {

        LinkedList<DirectSocketAddress> result = new LinkedList<DirectSocketAddress>();

        try {

            System.out.println("Getting hub addresses!!!!");

            String tmp = global.getServerAddress();

            System.out.println("Hub addresses: " + tmp);

            StringTokenizer st = new StringTokenizer(tmp, ",");

            while (st.hasMoreTokens()) {

                String token = st.nextToken();

                try {
                    System.out.println("Next address: " + token);

                    result.addLast(DirectSocketAddress.getByAddress(token));
                } catch (Exception e) {
                    System.out.println("Failed to parse address " + token);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to get hub addresses!");
            e.printStackTrace();
        }

        return result;
    }

    public void loadGrid(String filename) {

        System.out.println("Loading grid: " + filename);

        String name = null;

        try {
            name = deployer.addGrid(filename);
        } catch (Exception e) {
            System.out.println("Failed to add grid " + filename);
        }

        Grid grid = deployer.getGrid(name);

        for (Cluster c : grid.getClusters()) {

            c.setPassword(password);

            String tmp = c.getName();

            synchronized (this) {
                if (!resources.containsKey(tmp)) {
                    System.out.println("Storing cluster: " + tmp);
                    resources.put(tmp, new ComputeResource(grid, c));
                } else {
                    System.out.println("Cluster " + tmp + " already known");
                }
            }
        }
    }

    public synchronized ComputeResource[] getComputeResources() {
        return resources.values()
                .toArray(new ComputeResource[resources.size()]);
    }

    public void deployApplication(ComputeResource target) { 
        
            String name = target.getFriendlyName() + "-" + target.getJobID();
            
            System.out.println("Creating application description: " + name);
            
            
            ArrayList<String> prestage = new ArrayList<String>();
            prestage.add("prestage-app");
            prestage.add("jars");
            prestage.add("log4j.properties");
            
            if (target.getCluster().getStartupScript() != null) {
                prestage.add(target.getCluster().getStartupScript());
            }
            
            
            Application application = new Application(name,
                    "ibis.dog.server.Server",                // main class
                    null,                                    // java options
                    null,                                    // java system
                                                             // properties
                    new String[] { name },                   // main
                                                             // arguments
                    prestage.toArray(new String[0]),         // pre stage
                    null,                                    // post stage
                    new String[] { "prestage-hub", "log4j.properties" } // hub
                                                                        // prestage
            );

            deployer.addApplication(application);
            
            // We perform the deployment in a seperate thread!
            new DeploymentThread(target.getSubJob(application,
                    target.getCluster().getStartupScript())).start();    
    }

    public void done() {

        System.out.println("Stopping all servers");

        for (ComputeResource r : resources.values()) {
            r.killAllJobs();
        }

        System.out.println("Stopping broker");

        try {
            global.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        deployer.end();

        System.out.println("Deployment done!");
    }

}
