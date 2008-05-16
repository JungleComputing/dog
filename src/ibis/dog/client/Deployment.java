package ibis.dog.client;

import ibis.deploy.Application;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;

public class Deployment {

    private final static String SUPPORT_FILE    = "grids/support.properties";
    private final static String SUPPORT_GRID    = "support";
    private final static String SUPPORT_CLUSTER = "laptop";
    
    private final Deployer deployer;
    
    private Grid supportGrid; 
    
    private Job supportJob;
    
    public Deployment() throws Exception {
        
        deployer = new Deployer();
        
        // By default we load the support grid (local laptop, etc). 
        deployer.addGrid(SUPPORT_FILE);
        
        supportGrid = deployer.getGrid(SUPPORT_GRID);

        supportJob = new Job("support");
        
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
                new String[] { "prestage-server" }, // app prestage
                null, // post stage
                new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        
        deployer.addApplication(brokerApp);
        SubJob brokerJob = new SubJob("broker job");
        brokerJob.setApplication(brokerApp);
        brokerJob.setGrid(supportGrid);
        brokerJob.setCluster(supportGrid.getCluster(SUPPORT_CLUSTER));
        
        supportJob.addSubJob(brokerJob);
        
        System.out.println("Starting broker on: " + SUPPORT_GRID + "/" 
                + SUPPORT_CLUSTER);
        
        deployer.deploy(supportJob);
        
        System.out.println("Broker started!");

        System.out.println("pool = " + supportJob.getPoolID());
        System.out.println("address = " + supportJob.getServerAddress());
        System.out.println("hub address = " + supportJob.getHubAddresses());
        
        // TODO: Nasty hack!!
        System.setProperty("ibis.pool.name",
                                    supportJob.getPoolID()); 
        System.setProperty("ibis.server.address",
                                    supportJob.getServerAddress());
        System.setProperty("ibis.server.hub.addresses",
                                    supportJob.getHubAddresses());

    }
    
    
}
