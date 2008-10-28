package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gridlab.gat.URI;

/**
 * This object contains all the information needed to describe an ibis cluster.
 * This information is about <b>where</b> the application should be run and not
 * about which application should run.
 * 
 * @author rkemp
 */
public class IbisCluster extends Cluster {

    public IbisCluster(String name, Grid grid, TypedProperties properties)
            throws Exception {
        super(name, grid, properties);

        SortedMap<String, String> ibisClusterProperties = new TreeMap<String, String>();
        for (String key : IbisBasedGrid.KEYS) {
            ibisClusterProperties.put(key, properties.getProperty(name + "."
                    + key, null));
        }
        getCategories()
                .add(new PropertyCategory("ibis", ibisClusterProperties));
    }

    public IbisCluster(String name, Grid grid) throws Exception {
        super(name, grid);
        SortedMap<String, String> ibisClusterProperties = new TreeMap<String, String>();
        for (String key : IbisBasedGrid.KEYS) {
            ibisClusterProperties.put(key, null);
        }
        getCategories()
                .add(new PropertyCategory("ibis", ibisClusterProperties));
    }

    /**
     * Returns the {@link URI} of the broker that should be used for deploying
     * an Ibis server.
     * 
     * @return the {@link URI} of the broker that should be used for deploying
     *         an Ibis server.
     */
    public URI getServerBroker() {
        if (getCategories().get(1).getData().get("ibis.server.broker.uri") == null) {
            try {
                return new URI(grid.getCategories().get(1).getData().get(
                        "ibis.server.broker.uri"));
            } catch (URISyntaxException e) {
                // should not happen (this should be checked in 'load')
            }
        } else {
            try {
                return new URI(getCategories().get(1).getData().get(
                        "ibis.server.broker.uri"));
            } catch (URISyntaxException e) {
                // should not happen (this should be checked in 'load')
            }
        }
        return null;
    }

    /**
     * Sets the {@link URI} of the broker that should be used for deploying an
     * Ibis server.
     * 
     * @param broker
     *                the {@link URI} of the broker that should be used for
     *                deploying an Ibis server.
     */
    public void setServerBroker(URI broker) {
        getCategories().get(1).getData().put("ibis.server.broker.uri",
                broker.toString());
    }

    /**
     * Returns the broker adaptors that might be used to submit the ibis server
     * job to.
     * 
     * @return the broker adaptors that might be used to submit the ibis server
     *         job to.
     */
    public String getServerBrokerAdaptors() {
        if (getCategories().get(1).getData().get("ibis.server.broker.adaptors") == null) {
            return grid.getCategories().get(1).getData().get(
                    "ibis.server.broker.adaptors");
        } else {
            return getCategories().get(1).getData().get(
                    "ibis.server.broker.adaptors");
        }
    }

    /**
     * Sets the broker adaptors that might be used to submit the ibis server job
     * to.
     * 
     * @param brokerAdaptors
     *                the broker adaptors that might be used to submit the ibis
     *                server job to.
     */
    public void setServerBrokerAdaptors(String brokerAdaptors) {
        getCategories().get(1).getData().put("ibis.server.broker.adaptors",
                brokerAdaptors);
    }

    /**
     * Returns the file adaptors that might be used during the submission of the
     * ibis server job.
     * 
     * @return the file adaptors that might be used during the submission of the
     *         ibis server job.
     */
    public String getServerFileAdaptors() {
        if (getCategories().get(1).getData().get("ibis.server.file.adaptors") == null) {
            return grid.getCategories().get(1).getData().get(
                    "ibis.server.file.adaptors");
        } else {
            return getCategories().get(1).getData().get(
                    "ibis.server.file.adaptors");
        }
    }

    /**
     * Sets the file adaptors that might be used during the submission of the
     * ibis server job.
     * 
     * @param fileAdaptors
     *                the file adaptors that might be used during the submission
     *                of the ibis server job.
     */
    public void setServerFileAdaptors(String fileAdaptors) {
        getCategories().get(1).getData().put("ibis.server.file.adaptors",
                fileAdaptors);
    }

    /**
     * Returns the options that are used for starting a server/hub on this
     * cluster
     * 
     * @return the options that are used for starting a server/hub on this
     *         cluster
     */
    public String getServerOptions() {
        if (getCategories().get(1).getData().get("ibis.server.options") == null) {
            return grid.getCategories().get(1).getData().get(
                    "ibis.server.options");
        } else {
            return getCategories().get(1).getData().get("ibis.server.options");
        }
    }

    /**
     * Sets the options that are used for starting a server/hub on this cluster
     * 
     * @param options
     *                the options that are used for starting a server/hub on
     *                this cluster
     */
    public void setServerOptions(String options) {
        getCategories().get(1).getData().put("ibis.server.options", options);
    }

    public void setJavaOptions(String options) {
        getCategories().get(1).getData().put("ibis.server.java.options",
                options);
    }

    public String getJavaOptions() {
        if (getCategories().get(1).getData().get("ibis.server.java.options") == null) {
            return grid.getCategories().get(1).getData().get(
                    "ibis.server.java.options");
        } else {
            return getCategories().get(1).getData().get(
                    "ibis.server.java.options");
        }
    }

}
