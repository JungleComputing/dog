package ibis.dog.client;

import ibis.deploy.Application;
import ibis.deploy.Cluster;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;
import ibis.smartsockets.direct.DirectSocketAddress;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Deployment {

    private final static String SUPPORT_FILE    = "grids/support.properties";
    private final static String SUPPORT_GRID    = "support";
    private final static String SUPPORT_CLUSTER = "laptop";
    
    private final Deployer deployer;
    
    private Grid supportGrid; 
    
    private Job supportJob;
    
    private HashMap<String, ComputeResource> resources = 
        new HashMap<String, ComputeResource>();
     
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

    public List<DirectSocketAddress> getHubAddresses() { 
        
        LinkedList<DirectSocketAddress> result = 
            new LinkedList<DirectSocketAddress>();
        
        try {
            String tmp = supportJob.getHubAddresses();
            
            StringTokenizer st = new StringTokenizer(tmp, ",");
            
            while (st.hasMoreTokens()) { 
                
                String token = st.nextToken();
                
                try { 
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
            
            String tmp = c.getName();
            
            synchronized (this) {
                if (!resources.containsKey(tmp)) { 
                    System.out.println("Storing cluster: " + tmp);
                    resources.put(tmp, new ComputeResource(c)); 
                } else { 
                    System.out.println("Cluster " + tmp + " already known");
                }
            }
        }
    }

    public synchronized ComputeResource [] getComputeResources() {
        return resources.values().toArray(new ComputeResource[resources.size()]);
    }
    
    public void done() { 
  
        System.out.println("Stopping all servers");
        
        for (ComputeResource r : resources.values()) { 
            r.killAllJobs();
        }
        
        System.out.println("Stopping broker");
       
        try {
            supportJob.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        deployer.end();
   
        System.out.println("Deployment done!");
    }
    
}