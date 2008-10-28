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
 * An ApplicationGroup contains a number of {@link Application}s that form
 * together a group. Like Applications, an ApplicationGroup also contains
 * key,value data. This data can be used to set defaults for the
 * {@link Application}s in the ApplicationGroup. An ApplicationGroup can be
 * loaded from a file or saved to a file.
 * 
 * @author rkemp
 */
public class ApplicationGroup extends PropertySetGroup {

    /**
     * The set of keys that are used by the ApplicationGroup.
     * 
     * <ul>
     * <li><code>executable</code>: to describe the path to the executable</li>
     * <li><code>arguments</code>: to describe arguments of the executable
     * (separated by ' ')</li>
     * <li><code>environment</code>: to describe the environment of the
     * application. An environment variable should be written like this:
     * key=value. And multiple variables should be separated by a ' '</li>
     * <li><code>stdout</code>: to describe the path to where the stdout of
     * this application should be stored. (If this path ends with an '#', this
     * char will be replaced with a counter for each run of this application, so
     * that multiple instances of the same application won't overwrite each
     * others stdout</li>
     * <li><code>stderr</code>: to describe the path to where the stderr of
     * this application should be stored. (If this path ends with an '#', this
     * char will be replaced with a counter for each run of this application, so
     * that multiple instances of the same application won't overwrite each
     * others stderr</li>
     * <li><code>prestage</code>: to describe which files should be
     * prestaged. A prestage file might be written like a=b, then a is the
     * source and b the target location, otherwise the source and target will
     * follow the default behaviour of JavaGAT. Multiple files are separated by
     * a ' '.</li>
     * <li><code>poststage</code>: to describe which files should be
     * prestaged. A poststage file might be written like a=b, then a is the
     * source and b the target location, otherwise the source and target will
     * follow the default behaviour of JavaGAT. Multiple files are separated by
     * a ' '.</li>
     * <li><code>attributes</code>: to describe which JavaGAT attributes
     * should be used for this application. </li>
     * <li><code>wrapper.script</code>: to describe which script should be
     * invoked if a wrapper executable is used for the cluster</li>
     * <ul>
     */
    public static final String[] KEYS = new String[] { "executable",
            "arguments", "environment", "stdout", "stderr", "prestage",
            "poststage", "attributes" };

    private Set<Application> applications = new HashSet<Application>();

    /**
     * Constructs an application group from properties stored in the given file.
     * And also constructs the applications inside this application group.
     * 
     * @param fileName
     *                the name of the file containing the properties
     * @throws FileNotFoundException
     *                 if the given file cannot be found
     * @throws IOException
     *                 if reading from the given file fails
     * @throws Exception
     *                 if the properties don't contain a 'name' property with
     *                 the name of the application group
     */
    public ApplicationGroup(String fileName) throws FileNotFoundException,
            IOException, Exception {
        this(PropertySet.getPropertiesFromFile(fileName));
    }

    /**
     * Constructs an application group with the given name and the given
     * applications belonging to this group.
     * 
     * @param name
     *                the name of the application group
     * @param applications
     *                the applications belonging to this application group.
     * @throws Exception
     *                 if the name is <code>null</code>
     */
    public ApplicationGroup(String name, Application... applications)
            throws Exception {
        super(name);
        SortedMap<String, String> applicationGroupProperties = new TreeMap<String, String>();
        for (String key : ApplicationGroup.KEYS) {
            applicationGroupProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("basic", applicationGroupProperties));

        if (applications != null) {
            for (Application application : applications) {
                if (application != null) {
                    this.applications.add(application);
                }
            }
        }
    }

    /**
     * Constructs an application group from a set of properties. Also constructs
     * applications that are in this set of properties and belong to this
     * application group. Those applications should be described in the property
     * 'applications' in a comma separated list.
     * 
     * @param properties
     *                the properties from which the application group is
     *                constructed
     * @throws Exception
     *                 if the properties object doesn't contain a key 'name'
     */
    public ApplicationGroup(TypedProperties properties) throws Exception {
        super(properties.getProperty("name", null));

        SortedMap<String, String> applicationGroupProperties = new TreeMap<String, String>();
        for (String key : ApplicationGroup.KEYS) {
            applicationGroupProperties.put(key, properties.getProperty(key,
                    null));
        }
        getCategories().add(
                new PropertyCategory("basic", applicationGroupProperties));

        // now load the applications in the application group
        String[] applicationNames = properties.getStringList("applications",
                ",", null);
        if (applicationNames != null) {
            for (String applicationName : applicationNames) {
                addApplication(newApplication(applicationName, properties));
            }
        }
    }

    /**
     * Constructs a new application from the properties and the application
     * name.
     * 
     * @param applicationName
     *                the name of the application
     * @param properties
     *                the properties to construct the application from
     * @return the application
     * @throws Exception
     *                 if the construction fails
     */
    public Application newApplication(String applicationName,
            TypedProperties properties) throws Exception {
        return new Application(applicationName, this, properties);
    }

    /**
     * Returns the applications in this ApplicationGroup
     * 
     * @return the applications in this ApplicationGroup
     */
    public Set<Application> getApplications() {
        return applications;
    }

    /**
     * Sets the applications in this ApplicationGroup. Any earlier added
     * applications will be removed from this ApplicationGroup.
     * 
     * @param applications
     *                the applications to be set
     */
    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    /**
     * Adds an application to this ApplicationGroup.
     * 
     * @param application
     *                the application to be added.
     */
    public void addApplication(Application application) {
        this.applications.add(application);
    }

    /**
     * Get an application with a given name from this ApplicationGroup
     * 
     * @param applicationName
     *                the name of the application to search for
     * @return the application with the given name, or <code>null</code> if no
     *         applications with the given name exist in this ApplicationGroup.
     */
    public Application getApplication(String applicationName) {
        for (Application application : applications) {
            if (application.getName().equals(applicationName)) {
                return application;
            }
        }
        return null;
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
        out
                .write(("# Application properties file, generated by JavaGAT Deploy on "
                        + new Date(System.currentTimeMillis()) + "\n")
                        .getBytes());
        out.write("\n".getBytes());
        out.write("# Application group name\n".getBytes());
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
        out.write("# This application group consists of these applications\n"
                .getBytes());
        out.write("applications=".getBytes());
        for (Application application : applications) {
            out.write((application.getName() + ",").getBytes());
        }
        out.write("\n".getBytes());

        // write applications
        for (Application application : applications) {
            out.write("\n".getBytes());
            out
                    .write(("# Details of application '"
                            + application.getName() + "'\n").getBytes());
            for (PropertyCategory category : application.getCategories()) {
                for (String key : category.getData().keySet()) {
                    if (category.getData().get(key) != null) {
                        out.write((application.getName() + "." + key + "="
                                + category.getData().get(key) + "\n")
                                .getBytes());
                    } else {
                        out
                                .write(("# " + application.getName() + "."
                                        + key + "=\n").getBytes());
                    }
                }
            }
        }
    }

    public Set<? extends PropertySet> getPropertySets() {
        return getApplications();
    }

    /**
     * Removes the given application from the application group (if it belongs
     * to the application group at all).
     * 
     * @param application
     *                the application to be removed from this group
     */
    public void removeApplication(Application application) {
        applications.remove(application);
    }
}
