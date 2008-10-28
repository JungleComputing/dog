/*
 * Created on Mar 6, 2006
 */
package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A Grid contains a number of {@link Cluster}s that form together a group.
 * Like Clusters, a Grid also contains key,value data. This data can be used to
 * set defaults for the {@link Cluster}s in the Grid. A Grid can be loaded from
 * a file or saved to a file.
 * 
 * @author rkemp
 */
public class Grid extends PropertySetGroup {

    /**
     * The set of keys that are used by the Grid.
     * 
     * <ul>
     * <li><code>broker.uri</code>: to describe the uri of the broker</li>
     * <li><code>broker.adaptors</code>: to describe which adaptors (comma
     * separated) might be used for resource brokering</li>
     * <li><code>file.adaptors</code>: to describe which adaptors (comma
     * separated) might be used for file operations</li>
     * <li><code>javapath</code>: to describe the path to where the java
     * executable is for the cluster.</li>
     * <li><code>nodes.total</code>: total number of nodes for the cluster</li>
     * <li><code>cores.total</code>: total number of cores for the cluster</li>
     * <li><code>is.windows</code>: to describe whether or not the cluster
     * has a Windows Operating System.</li>
     * <li><code>user.name</code>: the username to be used for the cluster</li>
     * <li><code>geo.position</code>: the geo position of the cluster</li>
     * <li><code>wrapper.executable</code>: the executable to invoke if a
     * wrapper script is used (for relative executable paths, the path should be
     * relative to the javagat sandbox</li>
     * <ul>
     */
    public static final String[] KEYS = new String[] { "broker.uri",
            "broker.adaptors", "file.adaptors", "javapath", "nodes.total",
            "cores.total", "is.windows", "user.name", "geo.position",
            "wrapper.executable", "wrapper.script" };

    private Set<Cluster> clusters = new HashSet<Cluster>();

    /**
     * Constructs a grid from properties stored in the given file. And also
     * constructs the clusters inside this grid.
     * 
     * @param fileName
     *                the name of the file containing the properties
     * @throws FileNotFoundException
     *                 if the given file cannot be found
     * @throws IOException
     *                 if reading from the given file fails
     * @throws Exception
     *                 if the properties don't contain a 'name' property with
     *                 the name of the grid
     */
    public Grid(String fileName) throws FileNotFoundException, IOException,
            Exception {
        this(PropertySet.getPropertiesFromFile(fileName));
    }

    /**
     * Constructs a grid with the given name and the given clusters belonging to
     * this group.
     * 
     * @param name
     *                the name of the grid
     * @param clusters
     *                the clusters belonging to this grid.
     * @throws Exception
     *                 if the name is <code>null</code>
     */
    public Grid(String name, Cluster... clusters) throws Exception {
        super(name);
        SortedMap<String, String> gridProperties = new TreeMap<String, String>();
        for (String key : Grid.KEYS) {
            gridProperties.put(key, null);
        }
        getCategories().add(new PropertyCategory("basic", gridProperties));

        if (clusters != null) {
            for (Cluster cluster : clusters) {
                if (cluster != null) {
                    this.clusters.add(cluster);
                }
            }
        }
    }

    /**
     * Constructs a grid from a set of properties. Also constructs clusters that
     * are in this set of properties and belong to this grid. Those clusters
     * should be described in the property 'clusters' in a comma separated list.
     * 
     * @param properties
     *                the properties from which the grid is constructed
     * @throws Exception
     *                 if the properties object doesn't contain a key 'name'
     */
    public Grid(TypedProperties properties) throws Exception {
        super(properties.getProperty("name", null));

        SortedMap<String, String> gridProperties = new TreeMap<String, String>();
        for (String key : Grid.KEYS) {
            gridProperties.put(key, properties.getProperty(key, null));
        }
        getCategories().add(new PropertyCategory("basic", gridProperties));

        // now load the applications in the application group
        String[] clusterNames = properties.getStringList("clusters", ",", null);
        if (clusterNames != null) {
            for (String clusterName : clusterNames) {
                addCluster(newCluster(clusterName, properties));
            }
        }
    }

    /**
     * Constructs a new cluster from the properties and the cluster name.
     * 
     * @param clusterName
     *                the name of the cluster
     * @param properties
     *                the properties to construct the cluster from
     * @return the cluster
     * @throws Exception
     *                 if the construction fails
     */
    public Cluster newCluster(String clusterName, TypedProperties properties)
            throws Exception {
        return new Cluster(clusterName, this, properties);
    }

    /**
     * Adds a {@link Cluster} to this Grid
     * 
     * @param cluster
     *                the {@link Cluster} to be added
     */
    public void addCluster(Cluster cluster) {
        this.clusters.add(cluster);
    }

    /**
     * Gets the {@link Cluster}s in this Grid
     * 
     * @return the {@link Cluster}s in this Grid.
     */
    public Set<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Gets the total number of nodes in this Grid
     * 
     * @return the total number of nodes in this Grid
     */
    public int getTotalNodes() {
        int res = 0;
        for (Cluster cluster : clusters) {
            res += cluster.getTotalNodes();
        }
        return res;
    }

    /**
     * Gets the total number of cores in this Grid
     * 
     * @return the total number of cores in this Grid
     */
    public int getTotalCores() {
        int res = 0;
        for (Cluster cluster : clusters) {
            res += cluster.getTotalCores();
        }
        return res;
    }

    public Set<? extends PropertySet> getPropertySets() {
        return getClusters();
    }

    public void save(String path) throws IOException {
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + path
                        + "'.");
            }
        }
        java.io.FileOutputStream out = null;
        try {
            out = new java.io.FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // should not happen
            e.printStackTrace();
        }
        // write defaults
        out.write(("# Grid properties file, generated by JavaGAT Deploy on "
                + new Date(System.currentTimeMillis()) + "\n").getBytes());
        out.write("\n".getBytes());
        out.write("# Grid name\n".getBytes());
        out.write(("name=" + getName() + "\n").getBytes());

        out.write("\n".getBytes());
        out.write("# Default settings\n".getBytes());
        for (PropertyCategory category : getCategories()) {
            out.write(("# " + category.getName() + " defaults\n").getBytes());
            for (String key : category.getData().keySet()) {
                if (category.getData().get(key) != null) {
                    out.write((key + "=" + category.getData().get(key) + "\n")
                            .getBytes());
                } else {
                    out.write(("# " + key + "=\n").getBytes());
                }
            }
        }
        // write names of applications
        out.write("\n".getBytes());
        out.write("# This grid consists of these clusters\n".getBytes());
        out.write("clusters=".getBytes());
        System.out.println("cluster.size: " + clusters.size());

        for (Cluster cluster : clusters) {
            out.write((cluster.getName() + ",").getBytes());
        }
        out.write("\n".getBytes());

        // write applications
        for (Cluster cluster : clusters) {
            out.write("\n".getBytes());
            out.write(("# Details of cluster '" + cluster.getName() + "'\n")
                    .getBytes());
            for (PropertyCategory category : cluster.getCategories()) {
                for (String key : category.getData().keySet()) {
                    if (category.getData().get(key) != null) {
                        out.write((cluster.getName() + "." + key + "="
                                + category.getData().get(key) + "\n")
                                .getBytes());
                    } else {
                        out
                                .write(("# " + cluster.getName() + "." + key + "=\n")
                                        .getBytes());
                    }
                }
            }
        }
        out.flush();
        out.close();

    }

    /**
     * Removes the given cluster from the grid (if it belongs to the grid at
     * all).
     * 
     * @param cluster
     *                the cluster to be removed from this grid
     */
    public void removeCluster(Cluster cluster) {
        clusters.remove(cluster);
    }

}