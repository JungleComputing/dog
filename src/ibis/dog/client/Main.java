package ibis.dog.client;

import ibis.deploy.Application;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;
import ibis.dog.gui.application.ClientPanel;

public class Main {

    private static void unused() { 
        try {
        
        // Create Deployer & indicate location of ibis-server

//      Cluster serverCluster = new Cluster("IbisServer",
//                              new URI("any://fs0.das3.cs.vu.nl"));
//      Deployer deployer = new Deployer();
//      Deployer deployer = new Deployer(serverCluster);
//      Deployer deployer = new Deployer("das-3.properties", "VU");
        Deployer deployer = new Deployer();


        // Create Global Job instance and Grid objects

        Job globalJob = new Job("global job");
        deployer.addGrid("das-3.properties");
        Grid das3 = deployer.getGrid("DAS-3");

        deployer.addGrid("euro-grid.properties");
        Grid euro = deployer.getGrid("EURO-GRID");

        deployer.addGrid("usa-grid.properties");
        Grid usa = deployer.getGrid("USA-GRID");

        deployer.addGrid("aus-grid.properties");
        Grid aus = deployer.getGrid("AUS-GRID");


        // Create Broker Application/Job

        Application brokerApp = new Application("Broker",
            "ibis.mbf.broker.Broker", // main class
            null, // java options
            null, // java system properties
            null, // java arguments
            new String[] { "prestage-app", "jars" }, // app prestage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );

        deployer.addApplication(brokerApp);
        SubJob brokerJob = new SubJob("broker job");
        brokerJob.setApplication(brokerApp);
        brokerJob.setGrid(das3);
        brokerJob.setCluster(das3.getCluster("VU"));


        // Create Server Application/Job

/*
        // Zeus, Cracow...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "CracowPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(euro);
        serverJob.setCluster(euro.getCluster("Zeus"));
        serverJob.setNodes(1);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/people/crseinst/serverscript.sh");
*/

/*
        // Genoa...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "GenoaPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(euro);
        serverJob.setCluster(euro.getCluster("Genoa"));
        serverJob.setNodes(1);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/fjseins/serverscript.sh");

*/

/*
        // Infiniband Cluster, Munich...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "MunichPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(euro);
        serverJob.setCluster(euro.getCluster("Munich"));
        serverJob.setNodes(4);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/seinstrx/serverscript.sh");
*/


/*
        // Mahar, Melbourne...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "MaharPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(aus);
        serverJob.setCluster(aus.getCluster("Mahar"));
        serverJob.setNodes(4);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/tmp_mnt/u/cluster3/visitors/franks/serverscript.sh");
*/

        // Hiroshi, Sydney...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "SydneyPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(aus);
        serverJob.setCluster(aus.getCluster("Hiroshi"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/fjseins/serverscript.sh");
/*
*/

/*
        // Nico, Chicago...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "NicoPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(usa);
        serverJob.setCluster(usa.getCluster("Nico"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/sara/fjseins/nicoserverscript.sh");
*/


/*
        // Yorda, Chicago...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "YordaPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(usa);
        serverJob.setCluster(usa.getCluster("Yorda"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home/sara/fjseins/yordaserverscript.sh");
*/


/*
        // VU, Amsterdam...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "VUPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // app prestage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(das3);
        serverJob.setCluster(das3.getCluster("VU"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");
*/

/*
        // Leiden...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "LeidenPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(das3);
        serverJob.setCluster(das3.getCluster("Leiden"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");
*/

/*
        // UvA, Amsterdam...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "UvAPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(das3);
        serverJob.setCluster(das3.getCluster("UvA"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");
*/

/*
        // Delft...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "DelftPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(das3);
        serverJob.setCluster(das3.getCluster("Delft"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");
*/

/*
        // MultimediaN, Amsterdam...

        Application serverApp = new Application("Server",
            "ibis.mbf.server.Server", // main class
            null, // java options
            null, // java system properties
            new String[] { "MultimediaNPool" }, // main arguments
            new String[] { "prestage-app", "jars" }, // pre stage
            null, // post stage
            new String[] { "prestage-hub", "log4j.properties" } // hub prestage
        );
        deployer.addApplication(serverApp);
        SubJob serverJob = new SubJob("server job");
        serverJob.setApplication(serverApp);
        serverJob.setGrid(das3);
        serverJob.setCluster(das3.getCluster("MultimediaN"));
        serverJob.setNodes(8);
        serverJob.setWrapperExecutable("/bin/sh");
        serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");
*/


        // Deploy the just created Applations/Jobs

        globalJob.addSubJob(brokerJob);
        globalJob.addSubJob(serverJob);
        deployer.deploy(globalJob);


        // Obtain essential system properties from Deployer

        System.setProperty("ibis.pool.name",
                                    globalJob.getPoolID());
        System.setProperty("ibis.server.address",
                                    globalJob.getServerAddress());
        System.setProperty("ibis.server.hub.addresses",
                                    globalJob.getHubAddresses());
        
        } catch (Exception e) {
            System.out.println("FATAL MyApp ERROR");
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {
        try {

            // And start Client GUI and Application
            Client c = new Client();
            c.start();

            ClientPanel.createGUI(c);
            
/*          while (brokerJob.getStatus() !=
                org.gridlab.gat.resources.Job.STOPPED_STRING ||
                brokerJob.getStatus() !=
                org.gridlab.gat.resources.Job.SUBMISSION_ERROR_STRING) {
                Thread.sleep(1000);
            }
*/
        } catch (Exception e) {
            System.out.println("FATAL MyApp ERROR");
            e.printStackTrace();
        }
    } 
}
