package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An IbisBasedApplicationGroup contains a number of {@link Application}s that
 * form together a group. Like Applications, an IbisApplicationGroup also
 * contains key,value data. This data can be used to set defaults for the
 * {@link Application}s in the IbisApplicationGroup. An IbisApplicationGroup
 * can be loaded from a file or saved to a file. The only difference between the
 * {@link ApplicationGroup} object and the {@link IbisBasedApplicationGroup}
 * object is that the {@link JavaBasedApplicationGroup} can contain also
 * defaults for {@link IbisApplication}s.
 * 
 * @author rkemp
 */
public class IbisBasedApplicationGroup extends JavaBasedApplicationGroup {

    /**
     * The set of keys that are used by the IbisApplicationGroup
     */
    public static final String[] KEYS = new String[] { "ibis.prestage" };

    public IbisBasedApplicationGroup(String fileName)
            throws FileNotFoundException, IOException, Exception {
        this(PropertySet.getPropertiesFromFile(fileName));
    }

    public IbisBasedApplicationGroup(String name, Application... applications)
            throws Exception {
        super(name, applications);
        SortedMap<String, String> ibisApplicationGroupProperties = new TreeMap<String, String>();
        for (String key : IbisBasedApplicationGroup.KEYS) {
            ibisApplicationGroupProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("ibis", ibisApplicationGroupProperties));
    }

    public IbisBasedApplicationGroup(TypedProperties properties)
            throws Exception {
        super(properties);

        SortedMap<String, String> ibisApplicationGroupProperties = new TreeMap<String, String>();
        for (String key : IbisBasedApplicationGroup.KEYS) {
            ibisApplicationGroupProperties.put(key, properties.getProperty(key,
                    null));
        }
        getCategories().add(
                new PropertyCategory("ibis", ibisApplicationGroupProperties));
    }

    public Application newApplication(String applicationName,
            TypedProperties properties) throws Exception {
        return new IbisApplication(applicationName, this, properties);
    }

}
