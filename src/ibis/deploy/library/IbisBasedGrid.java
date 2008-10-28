package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A IbisBasedGrid contains a number of {@link Cluster}s that form together a
 * group. Like Clusters, a IbisBasedGrid also contains key,value data. This data
 * can be used to set defaults for the {@link Cluster}s in the IbisBasedGrid. A
 * IbisBasedGrid can be loaded from a file or saved to a file. The only
 * difference between the {@link Grid} object and the {@link IbisBasedGrid}
 * object is that the {@link IbisBasedGrid} can contain also defaults for
 * {@link IbisCluster}s.
 * 
 * @author rkemp
 */
public class IbisBasedGrid extends Grid {

    /**
     * The set of keys that are used by the IbisBasedGrid
     */
    public static final String[] KEYS = new String[] {
            "ibis.server.broker.uri", "ibis.server.broker.adaptors",
            "ibis.server.file.adaptors", "ibis.server.options",
            "ibis.server.java.options" };

    public IbisBasedGrid(String fileName) throws FileNotFoundException,
            IOException, Exception {
        this(PropertySet.getPropertiesFromFile(fileName));
    }

    public IbisBasedGrid(String name, Cluster... clusters) throws Exception {
        super(name, clusters);
        SortedMap<String, String> ibisGridProperties = new TreeMap<String, String>();
        for (String key : IbisBasedGrid.KEYS) {
            ibisGridProperties.put(key, null);
        }
        getCategories().add(new PropertyCategory("ibis", ibisGridProperties));
    }

    public IbisBasedGrid(TypedProperties properties) throws Exception {
        super(properties);

        SortedMap<String, String> ibisGridProperties = new TreeMap<String, String>();
        for (String key : IbisBasedGrid.KEYS) {
            ibisGridProperties.put(key, properties.getProperty(properties
                    .getProperty("name")
                    + "." + key, null));
        }
        getCategories().add(new PropertyCategory("ibis", ibisGridProperties));
    }

    public Cluster newCluster(String clusterName, TypedProperties properties)
            throws Exception {
        return new IbisCluster(clusterName, this, properties);
    }

}
