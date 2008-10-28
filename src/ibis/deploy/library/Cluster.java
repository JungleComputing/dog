/*
 * Created on Mar 6, 2006
 */
package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gridlab.gat.URI;

/**
 * This object contains all the information needed to describe a cluster. This
 * information is about <b>where</b> the application should be run and not
 * about which application should run.
 * 
 * @author rkemp
 */
public class Cluster extends PropertySet {

    protected Grid grid;

    /**
     * Constructs a cluster from a {@link TypedProperties} object. This
     * application will be part of the given {@link Grid}. The keys that will
     * be used to load this application are the keys described in
     * {@link Grid#KEYS}, but then prefixed by the name of this cluster. A key '<i>k</i>'
     * from the {@link Grid#KEYS} will contain a default value for the whole
     * grid. A key 'name.<i>k</i>' will contain a specific value for the
     * cluster with this name (possibly overriding the default value).
     * 
     * @param name
     *                The name of the cluster
     * @param grid
     *                The grid where this application belongs to
     * @param properties
     *                The properties where this cluster is constructed from
     * @throws Exception
     *                 If name is <code>null</code>.
     */
    public Cluster(String name, Grid grid, TypedProperties properties)
            throws Exception {
        super(name);
        this.grid = grid;

        SortedMap<String, String> clusterProperties = new TreeMap<String, String>();
        for (String key : Grid.KEYS) {
            clusterProperties.put(key, properties.getProperty(name + "." + key,
                    null));
        }
        getCategories().add(new PropertyCategory("basic", clusterProperties));
    }

    /**
     * Constructs an empty cluster object. This cluster will belong to the given
     * {@link Grid}.
     * 
     * @param name
     *                The name of the cluster
     * @param grid
     *                The grid where this application belongs to
     * @throws Exception
     *                 If name is <code>null</code>
     */
    public Cluster(String name, Grid grid) throws Exception {
        super(name);
        this.grid = grid;
        grid.addCluster(this);
        SortedMap<String, String> clusterProperties = new TreeMap<String, String>();
        for (String key : Grid.KEYS) {
            clusterProperties.put(key, null);
        }
        getCategories().add(new PropertyCategory("basic", clusterProperties));
    }

    /**
     * Gets the java path of this cluster. If the executable <code>java</code>
     * is located at <code>/usr/local/jdk-1.5/bin/java</code>, then the java
     * path will be: <code>/usr/local/jdk-1.5/bin/java</code>
     * 
     * @return the java path
     */
    public String getJavaPath() {
        if (getCategories().get(0).getData().get("javapath") == null) {
            return grid.getCategories().get(0).getData().get("javapath");
        } else {
            return getCategories().get(0).getData().get("javapath");
        }
    }

    /**
     * Gets the user name for this cluster
     * 
     * @return the user name for this cluster
     */
    public String getUserName() {
        if (getCategories().get(0).getData().get("user.name") == null) {
            return grid.getCategories().get(0).getData().get("user.name");
        } else {
            return getCategories().get(0).getData().get("user.name");
        }
    }

    /**
     * Gets the password for this cluster
     * 
     * @return the password for this cluster
     */
    public String getPassword() {
        if (getCategories().get(0).getData().get("password") == null) {
            return grid.getCategories().get(0).getData().get("password");
        } else {
            return getCategories().get(0).getData().get("password");
        }
    }

    /**
     * Sets the password for this cluster
     */
    public void setPassword(String password) {
        getCategories().get(0).getData().put("password", password);
    }

    /**
     * Gets the number of nodes in this cluster
     * 
     * @return the number of nodes in this cluster
     */
    public int getTotalNodes() {
        if (getCategories().get(0).getData().get("nodes.total") == null) {
            return Integer.parseInt(grid.getCategories().get(0).getData().get(
                    "nodes.total"));
        } else {
            return Integer.parseInt(getCategories().get(0).getData().get(
                    "nodes.total"));
        }
    }

    /**
     * Returns the x coordinate of the geo position of this cluster
     * 
     * @return the x coordinate of the geo position of this cluster
     */
    public double getGeoPositionX() {
        try {
            if (getCategories().get(0).getData().get("geo.position") == null) {
                return Double.valueOf(grid.getCategories().get(0).getData()
                        .get("geo.position").split(",")[0].trim());
            } else {
                return Double.valueOf(getCategories().get(0).getData().get(
                        "geo.position").split(",")[0].trim());
            }
        } catch (Exception e) {
            return -1;
        }

    }

    /**
     * Returns the y coordinate of the geo position of this cluster
     * 
     * @return the y coordinate of the geo position of this cluster
     */
    public double getGeoPositionY() {
        try {
            if (getCategories().get(0).getData().get("geo.position") == null) {
                return Double.valueOf(grid.getCategories().get(0).getData()
                        .get("geo.position").split(",")[1].trim());
            } else {
                return Double.valueOf(getCategories().get(0).getData().get(
                        "geo.position").split(",")[1].trim());
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets the java path for this cluster
     * 
     * @param javapath
     *                the java path for this cluster.
     */
    public void setJavaPath(String javapath) {
        getCategories().get(0).getData().put("javapath", javapath);
    }

    /**
     * Sets the cluster type to Windows or non-Windows.
     * 
     * @param isWindows
     *                <code>true</code> if cluster is Windows,
     *                <code>false</code> otherwise
     */
    public void setWindows(boolean isWindows) {
        getCategories().get(0).getData().put("is.windows", "" + isWindows);
    }

    /**
     * Gets whether the cluster is a Windows or non Windows cluster.
     * 
     * @return whether the cluster is a Windows or non Windows cluster.
     */
    public boolean isWindows() {
        if (getCategories().get(0).getData().get("is.windows") == null) {
            return grid.getCategories().get(0).getData().get("is.windows")
                    .equalsIgnoreCase("true");
        } else {
            return getCategories().get(0).getData().get("is.windows")
                    .equalsIgnoreCase("true");
        }
    }

    /**
     * Returns the URI of the broker that is used to submit the application to.
     * 
     * @return the URI of the broker that is used to submit the application to.
     */
    public URI getBroker() {
        if (getCategories().get(0).getData().get("broker.uri") == null) {
            try {
                return new URI(grid.getCategories().get(0).getData().get(
                        "broker.uri"));
            } catch (URISyntaxException e) {
                // should not happen (this should be checked in 'load')
            }
        } else {
            try {
                return new URI(getCategories().get(0).getData().get(
                        "broker.uri"));
            } catch (URISyntaxException e) {
                // should not happen (this should be checked in 'load')
            }
        }
        return null;
    }

    /**
     * Sets the URI of the broker that is used to submit the application to.
     * 
     * @param broker
     *                the URI of the broker that is used to submit the
     *                application to.
     * 
     */
    public void setBroker(URI broker) {
        getCategories().get(0).getData().put("broker.uri", broker.toString());
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the application.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the resource broker for the application.
     */
    public String getBrokerAdaptors() {
        if (getCategories().get(0).getData().get("broker.adaptors") == null) {
            return grid.getCategories().get(0).getData().get("broker.adaptors");
        } else {
            return getCategories().get(0).getData().get("broker.adaptors");
        }
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the application.
     * 
     * @param brokerAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the resource broker for the
     *                application.
     */
    public void setBrokerAdaptors(String brokerAdaptors) {
        getCategories().get(0).getData().put("broker.adaptors", brokerAdaptors);
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the application.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the file operations for the application.
     */
    public String getFileAdaptors() {
        if (getCategories().get(0).getData().get("file.adaptors") == null) {
            return grid.getCategories().get(0).getData().get("file.adaptors");
        } else {
            return getCategories().get(0).getData().get("file.adaptors");
        }
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the application.
     * 
     * @param fileAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the file operations for the
     *                application.
     */
    public void setFileAdaptors(String fileAdaptors) {
        getCategories().get(0).getData().put("file.adaptors", fileAdaptors);
    }

    /**
     * Gets the total number of cores of this cluster
     * 
     * @return the total number of cores of this cluster
     */
    public int getTotalCores() {
        if (getCategories().get(0).getData().get("cores.total") == null) {
            return Integer.parseInt(grid.getCategories().get(0).getData().get(
                    "cores.total"));
        } else {
            return Integer.parseInt(getCategories().get(0).getData().get(
                    "cores.total"));
        }
    }

    /**
     * Sets the total number of cores of this cluster
     * 
     * @param totalCores
     *                the total number of cores of this cluster
     */
    public void setTotalCores(int totalCores) {
        getCategories().get(0).getData().put("cores.total", "" + totalCores);
    }

    /**
     * Gets the geo position of this cluster
     * 
     * @return the geo position of this cluster
     */
    public String getGeoPosition() {
        if (getCategories().get(0).getData().get("geo.position") == null) {
            return grid.getCategories().get(0).getData().get("geo.position");
        } else {
            return getCategories().get(0).getData().get("geo.position");
        }
    }

    /**
     * Sets the geo position of this cluster
     * 
     * @param geoPosition
     *                the physical location of this cluster
     */
    public void setGeoPosition(String geoPosition) {
        getCategories().get(0).getData().put("geo.position", geoPosition);
    }

    /**
     * Returns the wrapper executable of this cluster
     * 
     * @return the wrapper executable of this cluster
     */
    public String getWrapperExecutable() {
        if (getCategories().get(0).getData().get("wrapper.executable") == null) {
            return grid.getCategories().get(0).getData().get(
                    "wrapper.executable");
        } else {
            return getCategories().get(0).getData().get("wrapper.executable");
        }
    }

    /**
     * Sets the wrapper executable of this cluster
     * 
     * @param wrapperExecutable
     *                the wrapper executable of this cluster
     */
    public void setWrapperExecutable(String wrapperExecutable) {
        getCategories().get(0).getData().put("wrapper.executable",
                wrapperExecutable);
    }

    /**
     * Returns the wrapper script of this cluster
     * 
     * @return the wrapper script of this cluster
     */
    public String getWrapperScript() {
        if (getCategories().get(0).getData().get("wrapper.script") == null) {
            return grid.getCategories().get(0).getData().get("wrapper.script");
        } else {
            return getCategories().get(0).getData().get("wrapper.script");
        }
    }

    /**
     * Sets the wrapper script of this cluster
     * 
     * @param wrapperScript
     *                the wrapper script of this cluster
     */
    public void setWrapperScript(String wrapperScript) {
        getCategories().get(0).getData().put("wrapper.script", wrapperScript);
    }

    /**
     * Sets the total number of nodes for this cluster
     * 
     * @param totalNodes
     *                the total number of nodes for this cluster
     */
    public void setTotalNodes(int totalNodes) {
        getCategories().get(0).getData().put("nodes.total", "" + totalNodes);
    }

    /**
     * Returns the grid where this cluster belongs to.
     * 
     * @return the grid where this cluster belongs to.
     */
    public Grid getGrid() {
        return grid;
    }

    public String defaultValueFor(String key, String category) {
        for (PropertyCategory propertyCategory : grid.getCategories()) {
            if (propertyCategory.getName().equals(category)) {
                return propertyCategory.getData().get(key);
            }
        }
        return null;
    }

}