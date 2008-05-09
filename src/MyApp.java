import ibis.deploy.Application;
import ibis.deploy.Cluster;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;

import ibis.mbf.client.Client;
import ibis.mbf.client.gui.ClientWindow;
import ibis.mbf.media.MediaConsumer;
import ibis.mbf.media.WindowsWebcam;

import org.gridlab.gat.URI;


public class MyApp extends WindowsWebcam
{
	/* This is a BIG HACK!!! We have no alternative at the moment since
 	 * we do not have any other way to access the native methods below!
 	 */
	public native int OpenVideo(String filename);
	public native int GetFrameNr();
	public native int GetFrameWidth();
	public native int GetFrameHeight();
	public native int LastFrame();
	public native void GetFrameData(byte [] pixels);
	public native void NextFrame();
	public native void GotoFrame();


	public MyApp(MediaConsumer consumer,
					int width, int height, int delay)
	{
		super(consumer, width, height, delay);
	}
	

	public MyApp(int width, int height)
	{
		super(width, height);
	}


	public static void main(String[] args)
	{
	    try {
			// Create Deployer & indicate location of ibis-server

//			Cluster serverCluster = new Cluster("IbisServer",
//									new URI("any://fs0.das3.cs.vu.nl"));
//			Deployer deployer = new Deployer();
//			Deployer deployer = new Deployer(serverCluster);

			Deployer deployer = new Deployer("grid-test.properties", "VU");
	

			// Create Global Job instance and Grid objects

			Job globalJob = new Job("global job");
			deployer.addGrid("grid-test.properties");
			Grid das3 = deployer.getGrid("DAS-3");


			// Create Broker Application/Job

			Application brokerApp = new Application("Broker",
        				"ibis.mbf.broker.Broker", // main class
        				null, // java options
        				null, // java system properties
        				null, // java arguments
        				new String[] { "prestage-hack", "jars" }, // pre stage
        				null // post stage
			);
			deployer.addApplication(brokerApp);
			SubJob brokerJob = new SubJob("broker job");
			brokerJob.setApplication(brokerApp);
			brokerJob.setGrid(das3);
			brokerJob.setCluster(das3.getCluster("VU"));


			// Create Server Application/Job

			Application serverApp = new Application("Server",
        				"ibis.mbf.server.Server", // main class
        				null, // java options
        				null, // java system properties
        				new String[] { "localPool1" }, // main arguments
        				new String[] { "prestage-hack", "jars" }, // pre stage
        				null // post stage
			);
			deployer.addApplication(serverApp);
			SubJob serverJob = new SubJob("server job");
			serverJob.setApplication(serverApp);
			serverJob.setGrid(das3);
			serverJob.setCluster(das3.getCluster("VU"));
			serverJob.setNodes(8);
			serverJob.setWrapperExecutable("/bin/sh");
			serverJob.setWrapperArguments("/home4/fjseins/serverscript.sh");

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


			// And start Client GUI and Application

			ClientWindow w = new ClientWindow();
			Client c = new Client(new MyMediaFactory());
			w.setClient(c);
			c.run();

/*			while (brokerJob.getStatus() !=
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
