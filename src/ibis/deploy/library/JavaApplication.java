package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * This object contains all the information needed to describe a java
 * application. This information is about <b>which</b> application should be
 * run and not about where this application should be run.
 * 
 * @author rkemp
 */
public class JavaApplication extends Application {

    /**
     * Constructs a java application from a {@link TypedProperties} object. This
     * java application will be part of the given {@link ApplicationGroup}. The
     * keys that will be used to load this application are the keys described in
     * {@link JavaBasedApplicationGroup#KEYS} and {@link ApplicationGroup#KEYS},
     * but then prefixed by the name of this application. A key '<i>k</i>'
     * from the {@link ApplicationGroup#KEYS} or
     * {@link JavaBasedApplicationGroup#KEYS} will contain a default value for
     * the whole application group. A key 'name.<i>k</i>' will contain a
     * specific value for the application with this name (possibly overriding
     * the default value).
     * 
     * @param name
     *                The name of the application
     * @param group
     *                The group where this application belongs to
     * @param properties
     *                The properties where this application is constructed from
     * @throws Exception
     *                 If name is <code>null</code>.
     */
    public JavaApplication(String name, ApplicationGroup group,
            TypedProperties properties) throws Exception {
        super(name, group, properties);

        SortedMap<String, String> javaApplicationProperties = new TreeMap<String, String>();
        for (String key : JavaBasedApplicationGroup.KEYS) {
            javaApplicationProperties.put(key, properties.getProperty(name
                    + "." + key, null));
        }
        getCategories().add(
                new PropertyCategory("java", javaApplicationProperties));
    }

    /**
     * Constructs an empty java application object. This java application will
     * belong to the given {@link ApplicationGroup}.
     * 
     * @param name
     *                The name of the application
     * @param group
     *                The group where this application belongs to
     * @throws Exception
     *                 If name is <code>null</code>
     */
    public JavaApplication(String name, ApplicationGroup group)
            throws Exception {
        super(name, group);
        SortedMap<String, String> javaApplicationProperties = new TreeMap<String, String>();
        for (String key : JavaBasedApplicationGroup.KEYS) {
            javaApplicationProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("java", javaApplicationProperties));
    }

    /**
     * Returns a JavaGAT SoftwareDescription that reflects this Application. If
     * the application contains a 'java.main' the return value will be a
     * JavaSoftwareDescription, otherwise it will be a SoftwareDescription.
     * 
     * @return a {@link JavaSoftwareDescription} or a
     *         {@link SoftwareDescription} that reflects this Application
     * 
     * @throws GATObjectCreationException
     *                 if there a file (stdout/stderr/prestage/poststage) could
     *                 not be created by JavaGAT
     * 
     */
    public SoftwareDescription getSoftwareDescription(GATContext context)
            throws GATObjectCreationException {
        SoftwareDescription tmp = super.getSoftwareDescription(context);
        SortedMap<String, String> mergedData = new TreeMap<String, String>();
        for (PropertyCategory category : group.getCategories()) {
            mergedData.putAll(category.getData());
        }
        // put the specific data
        for (PropertyCategory category : getCategories()) {
            for (String key : category.getData().keySet()) {
                if (category.getData().get(key) != null) {
                    mergedData.put(key, category.getData().get(key));
                }
            }
        }
        JavaSoftwareDescription result;
        if (mergedData.get("java.main") == null) {
            return tmp;
        }
        if (mergedData.get("java.main").startsWith("intent:")) {
            result = new IntentSoftwareDescription();
            result.setJavaMain(mergedData.get("java.main").substring(
                    "intent:".length()));
        } else {
            result = new JavaSoftwareDescription();
            result.setJavaMain(mergedData.get("java.main"));
        }
        result.setExecutable(tmp.getExecutable());
        result.setStdout(tmp.getStdout());
        result.setStderr(tmp.getStderr());
        if (tmp.getPostStaged() != null) {
            for (File key : tmp.getPostStaged().keySet()) {
                result.addPostStagedFile(key, tmp.getPostStaged().get(key));
            }
        }
        if (tmp.getPreStaged() != null) {
            for (File key : tmp.getPreStaged().keySet()) {
                result.addPreStagedFile(key, tmp.getPreStaged().get(key));
            }
        }
        if (tmp.getEnvironment() != null) {
            result.setEnvironment(tmp.getEnvironment());
        }
        result.setAttributes(tmp.getAttributes());
        result.setJavaClassPath(mergedData.get("java.classpath"));
        if (mergedData.get("java.system.properties") != null) {
            String[] systemPropertiesVariable = mergedData.get(
                    "java.system.properties").split(" ");
            Map<String, String> systemProperties = new HashMap<String, String>();
            for (String systemProperty : systemPropertiesVariable) {
                if (systemProperty.indexOf("=") > 0
                        && !systemProperty.endsWith("=")) {
                    systemProperties.put(systemProperty.substring(0,
                            systemProperty.indexOf("=")), systemProperty
                            .substring(systemProperty.indexOf("=") + 1,
                                    systemProperty.length()));
                } else {
                    systemProperties.put(systemProperty, null);
                }
            }
            result.setJavaSystemProperties(systemProperties);
        }
        if (mergedData.get("java.options") != null) {
            result.setJavaOptions(mergedData.get("java.options").split(" "));
        }
        if (mergedData.get("java.arguments") != null) {
            result
                    .setJavaArguments(mergedData.get("java.arguments").split(
                            " "));
        }
        return result;
    }

    public String getJavaArguments() {
        if (getCategories().get(1).getData().get("java.arguments") == null) {
            return group.getCategories().get(1).getData().get("java.arguments");
        } else {
            return getCategories().get(1).getData().get("java.arguments");
        }
    }

    public void setJavaArguments(String javaArguments) {
        getCategories().get(1).getData().put("java.arguments", javaArguments);
    }

    public String getJavaClassPath() {
        if (getCategories().get(1).getData().get("java.classpath") == null) {
            return group.getCategories().get(1).getData().get("java.classpath");
        } else {
            return getCategories().get(1).getData().get("java.classpath");
        }
    }

    public void setJavaClassPath(String javaClassPath) {
        getCategories().get(1).getData().put("java.classpath", javaClassPath);
    }

    public String getJavaMain() {
        if (getCategories().get(1).getData().get("java.main") == null) {
            return group.getCategories().get(1).getData().get("java.main");
        } else {
            return getCategories().get(1).getData().get("java.main");
        }
    }

    public void setJavaMain(String javaMain) {
        getCategories().get(1).getData().put("java.main", javaMain);
    }

    public String getJavaOptions() {
        if (getCategories().get(1).getData().get("java.options") == null) {
            return group.getCategories().get(1).getData().get("java.options");
        } else {
            return getCategories().get(1).getData().get("java.options");
        }
    }

    public void setJavaOptions(String javaOptions) {
        getCategories().get(1).getData().put("java.options", javaOptions);
    }

    public String getJavaSystemProperties() {
        if (getCategories().get(1).getData().get("java.system.properties") == null) {
            return group.getCategories().get(1).getData().get(
                    "java.system.properties");
        } else {
            return getCategories().get(1).getData().get(
                    "java.system.properties");
        }
    }

    public void setJavaSystemProperties(String javaSystemProperties) {
        getCategories().get(1).getData().put("java.system.properties",
                javaSystemProperties);
    }

}
