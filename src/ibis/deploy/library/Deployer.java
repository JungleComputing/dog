package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.util.HashSet;
import java.util.Set;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;

/**
 * A Deployer object can be used to deploy applications on clusters. It also
 * acts as store for grids and application groups.
 * 
 * @author rkemp
 * 
 */
public class Deployer {

    private Set<ApplicationGroup> applicationGroups = new HashSet<ApplicationGroup>();

    private Set<Grid> grids = new HashSet<Grid>();

    /**
     * Adds an application group to this deployer.
     * 
     * @param applicationGroup
     *                the application group to be added
     * @return the added application group
     */
    public ApplicationGroup addApplicationGroup(
            ApplicationGroup applicationGroup) {
        this.applicationGroups.add(applicationGroup);
        return applicationGroup;
    }

    /**
     * Adds an application group to this deployer, the application group will be
     * loaded from the given file.
     * 
     * @param applicationFileName
     *                the file containing the application group
     * @return the added application group
     */
    public ApplicationGroup addApplicationGroup(String applicationFileName)
            throws Exception {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(applicationFileName));

        return addApplicationGroup(properties);
    }

    /**
     * Adds an application group to this deployer, the application group will be
     * loaded from the given properties.
     * 
     * @param properties
     *                the properties containing the application group
     * @return the added application group
     */
    public ApplicationGroup addApplicationGroup(TypedProperties properties)
            throws Exception {
        properties.expandSystemVariables();
        return addApplicationGroup(new ApplicationGroup(properties));
    }

    /**
     * Adds a grid to this deployer.
     * 
     * @param grid
     *                the grid to be added
     * @return the added grid
     */
    public Grid addGrid(Grid grid) {
        this.grids.add(grid);
        return grid;
    }

    /**
     * Adds a grid to this deployer, the grid will be loaded from the given
     * file.
     * 
     * @param gridFileName
     *                the file containing the grid
     * @return the added grid
     */
    public Grid addGrid(String gridFileName) throws Exception {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(gridFileName));
        return addGrid(properties);
    }

    /**
     * Adds a grid to this deployer, the grid will be loaded from the given
     * properties.
     * 
     * @param properties
     *                the properties containing the grid
     * @return the added grid
     */
    public Grid addGrid(TypedProperties properties) throws Exception {
        properties.expandSystemVariables();
        return addGrid(new Grid(properties));
    }

    /**
     * Deploys an application to a cluster. This returns a JavaGAT {@link Job}
     * which can be used to retrieve information about this job. The process
     * count is the number of processes that should be started, the resource
     * count is the number of resources where these processes should be
     * distributed over. A listener can be provided to listen to state changes
     * of the job.
     * 
     * @param application
     *                the application to be deployed
     * @param processCount
     *                the number of processes to start
     * @param cluster
     *                the cluster where the application should be deployed on.
     * @param resourceCount
     *                the number of resources to distribute the application on.
     * @param listener
     *                the listener for job state changes
     * @return a {@link Job} object
     * @throws Exception
     *                 if the submission fails
     */
    public Job deploy(Application application, int processCount,
            Cluster cluster, int resourceCount, MetricListener listener)
            throws Exception {
        GATContext context = new GATContext();
        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), cluster.getPassword());
            // securityContext.addNote("adaptors", "commandlinessh,sshtrilead");
            context.addSecurityContext(securityContext);
        }
        context.addPreference("file.chmod", "0755");
        if (cluster.getBrokerAdaptors() != null) {
            context.addPreference("resourcebroker.adaptor.name", cluster
                    .getBrokerAdaptors());
        }
        if (cluster.getFileAdaptors() != null) {
            context.addPreference("file.adaptor.name", cluster
                    .getFileAdaptors());
        }

        JobDescription jd = new JobDescription(application
                .getSoftwareDescription(context));
        jd.setProcessCount(processCount);
        jd.setResourceCount(resourceCount);

        ResourceBroker broker = GAT.createResourceBroker(context, cluster
                .getBroker());
        return broker.submitJob(jd, listener, "job.status");
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public void end() {
        GAT.end();
    }

    /**
     * Returns all the grids of this deployer
     * 
     * @return all the grids of this deployer
     */
    public Set<Grid> getGrids() {
        return grids;
    }

    /**
     * Returns all the application groups of this deployer.
     * 
     * @return all the application groups of this deployer.
     */
    public Set<ApplicationGroup> getApplicationGroups() {
        return applicationGroups;
    }

    /**
     * Removes an application group from this deployer.
     * 
     * @param applicationGroup
     *                the application group to be removed
     */
    public void removeApplicationGroup(ApplicationGroup applicationGroup) {
        applicationGroups.remove(applicationGroup);
    }

    /**
     * Removes a grid from this deployer.
     * 
     * @param grid
     *                the grid to be removed.
     */
    public void removeGrid(Grid grid) {
        grids.remove(grid);
    }

}
