package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A JavaBasedApplicationGroup contains a number of {@link Application}s that
 * form together a group. Like Applications, a JavaApplicationGroup also
 * contains key,value data. This data can be used to set defaults for the
 * {@link Application}s in the JavaApplicationGroup. A JavaApplicationGroup can
 * be loaded from a file or saved to a file. The only difference between the
 * {@link ApplicationGroup} object and the {@link JavaBasedApplicationGroup}
 * object is that the {@link JavaBasedApplicationGroup} can contain also
 * defaults for {@link JavaApplication}s.
 * 
 * @author rkemp
 */
public class JavaBasedApplicationGroup extends ApplicationGroup {

    /**
     * The set of keys that are used by the JavaApplicationGroup
     */
    public static final String[] KEYS = new String[] { "java.main",
            "java.classpath", "java.options", "java.arguments",
            "java.system.properties" };

    public JavaBasedApplicationGroup(String fileName)
            throws FileNotFoundException, IOException, Exception {
        this(PropertySet.getPropertiesFromFile(fileName));
    }

    public JavaBasedApplicationGroup(String name, Application... applications)
            throws Exception {
        super(name, applications);
        SortedMap<String, String> javaApplicationGroupProperties = new TreeMap<String, String>();
        for (String key : JavaBasedApplicationGroup.KEYS) {
            javaApplicationGroupProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("java", javaApplicationGroupProperties));
    }

    public JavaBasedApplicationGroup(TypedProperties properties)
            throws Exception {
        super(properties);

        SortedMap<String, String> javaApplicationGroupProperties = new TreeMap<String, String>();
        for (String key : JavaBasedApplicationGroup.KEYS) {
            javaApplicationGroupProperties.put(key, properties.getProperty(
                    properties.getProperty("name") + "." + key, null));
        }
        getCategories().add(
                new PropertyCategory("java", javaApplicationGroupProperties));
    }

    public Application newApplication(String applicationName,
            TypedProperties properties) throws Exception {
        return new JavaApplication(applicationName, this, properties);
    }

}
