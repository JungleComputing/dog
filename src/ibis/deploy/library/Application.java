package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * This object contains all the information needed to describe an application.
 * This information is about <b>which</b> application should be run and not
 * about where this application should be run.
 * 
 * @author rkemp
 */
public class Application extends PropertySet {

    protected ApplicationGroup group;

    private int counter = 0;

    /**
     * Constructs an application from a {@link TypedProperties} object. This
     * application will be part of the given {@link ApplicationGroup}. The keys
     * that will be used to load this application are the keys described in
     * {@link ApplicationGroup#KEYS}, but then prefixed by the name of this
     * application. A key '<i>k</i>' from the {@link ApplicationGroup#KEYS}
     * will contain a default value for the whole application group. A key
     * 'name.<i>k</i>' will contain a specific value for the application with
     * this name (possibly overriding the default value).
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
    public Application(String name, ApplicationGroup group,
            TypedProperties properties) throws Exception {
        super(name);
        this.group = group;

        SortedMap<String, String> applicationProperties = new TreeMap<String, String>();
        for (String key : ApplicationGroup.KEYS) {
            applicationProperties.put(key, properties.getProperty(name + "."
                    + key, null));
        }
        getCategories().add(
                new PropertyCategory("basic", applicationProperties));
    }

    /**
     * Constructs an empty application object. This application will belong to
     * the given {@link ApplicationGroup}.
     * 
     * @param name
     *                The name of the application
     * @param group
     *                The group where this application belongs to
     * @throws Exception
     *                 If name is <code>null</code>
     */
    public Application(String name, ApplicationGroup group) throws Exception {
        super(name);
        this.group = group;
        group.addApplication(this);
        SortedMap<String, String> applicationProperties = new TreeMap<String, String>();
        for (String key : ApplicationGroup.KEYS) {
            applicationProperties.put(key, null);
        }
        getCategories().add(
                new PropertyCategory("basic", applicationProperties));
    }

    /**
     * @deprecated wrapper script should be defined in the cluster properties
     * 
     * Gets the String containing the script that is to be used for a cluster
     * with a wrapper executable.
     * 
     * @return the String containing the script that is to be used for a cluster
     *         with a wrapper executable.
     */
    public String getWrapperScript() {
        if (getCategories().get(0).getData().get("wrapper.script") == null) {
            return group.getCategories().get(0).getData().get("wrapper.script");
        } else {
            return getCategories().get(0).getData().get("wrapper.script");
        }
    }

    /**
     * Returns a JavaGAT SoftwareDescription that reflects this Application.
     * Note that for the stderr and stdout values ending with an '#', this '#'
     * will be replaced by a counter for each time this method is invoked.
     * 
     * @return a {@link SoftwareDescription} that reflects this Application
     * 
     * @throws GATObjectCreationException
     *                 if a file (stdout/stderr/prestage/poststage) could not be
     *                 created by JavaGAT
     * 
     */
    public SoftwareDescription getSoftwareDescription(GATContext context)
            throws GATObjectCreationException {
        // in order to construct the software description, we've to merge the
        // defaults from the ApplicationGroup with the specific values of this
        // Application, the specific values overwrite the defaults.
        SoftwareDescription result = new SoftwareDescription();
        SortedMap<String, String> mergedData = new TreeMap<String, String>();
        // put the defaults
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
        // now construct the software description from the merged data
        result.setExecutable(mergedData.get("executable"));
        if (mergedData.get("arguments") != null) {
            result.setArguments(mergedData.get("arguments").split(" "));
        }
        synchronized (this) {
            counter++;
            if (mergedData.get("stdout") != null) {
                result.setStdout(GAT.createFile(context, mergedData.get(
                        "stdout").replace("#", "-" + counter)));
            }
            if (mergedData.get("stderr") != null) {
                result.setStderr(GAT.createFile(context, mergedData.get(
                        "stderr").replace("#", "-" + counter)));
            }
        }
        // add poststage files
        if (mergedData.get("poststage") != null) {
            String[] postStageFiles = mergedData.get("poststage").split(" ");
            if (postStageFiles != null) {
                for (String postStageFile : postStageFiles) {
                    if (postStageFile.indexOf("=") > 0
                            && !postStageFile.endsWith("=")) {
                        result.addPostStagedFile(GAT.createFile(context,
                                postStageFile.substring(0, postStageFile
                                        .indexOf("="))), GAT.createFile(
                                context, postStageFile.substring(postStageFile
                                        .indexOf("=") + 1, postStageFile
                                        .length())));
                    } else {
                        result.addPostStagedFile(GAT.createFile(context,
                                postStageFile), null);
                    }
                }
            }
        }
        // add prestage files
        if (mergedData.get("prestage") != null) {
            String[] preStageFiles = mergedData.get("prestage").split(" ");
            if (preStageFiles != null) {
                for (String preStageFile : preStageFiles) {
                    if (preStageFile.indexOf("=") > 0
                            && !preStageFile.endsWith("=")) {
                        result.addPreStagedFile(GAT.createFile(context,
                                preStageFile.substring(0, preStageFile
                                        .indexOf("="))), GAT.createFile(
                                context, preStageFile.substring(preStageFile
                                        .indexOf("=") + 1, preStageFile
                                        .length())));
                    } else {
                        result.addPreStagedFile(GAT.createFile(context,
                                preStageFile), null);
                    }
                }
            }
        }

        if (mergedData.get("environment") != null) {
            String[] environmentVariables = mergedData.get("environment")
                    .split(" ");
            Map<String, Object> environment = new HashMap<String, Object>();
            for (String environmentVariable : environmentVariables) {
                if (environmentVariable.indexOf("=") > 0
                        && !environmentVariable.endsWith("=")) {
                    environment.put(environmentVariable.substring(0,
                            environmentVariable.indexOf("=")),
                            environmentVariable.substring(environmentVariable
                                    .indexOf("=") + 1, environmentVariable
                                    .length()));
                } else {
                    environment.put(environmentVariable, null);
                }
            }
            result.setEnvironment(environment);
        }
        if (mergedData.get("attributes") != null) {
            String[] attributesVariables = mergedData.get("attributes").split(
                    " ");
            Map<String, Object> attributes = new HashMap<String, Object>();
            for (String attribute : attributesVariables) {
                if (attribute.indexOf("=") > 0 && !attribute.endsWith("=")) {
                    attributes.put(attribute.substring(0, attribute
                            .indexOf("=")), attribute.substring(attribute
                            .indexOf("=") + 1, attribute.length()));
                } else {
                    attributes.put(attribute, null);
                }
            }
            result.setAttributes(attributes);
        }

        return result;
    }

    public String defaultValueFor(String key, String category) {
        for (PropertyCategory propertyCategory : group.getCategories()) {
            if (propertyCategory.getName().equals(category)) {
                return propertyCategory.getData().get(key);
            }
        }
        return null;
    }

    /**
     * Returns the {@link ApplicationGroup} where this application belongs to.
     * 
     * @return the {@link ApplicationGroup} where this application belongs to.
     */
    public ApplicationGroup getGroup() {
        return group;
    }

    public String getArguments() {
        if (getCategories().get(0).getData().get("arguments") == null) {
            return group.getCategories().get(1).getData().get("arguments");
        } else {
            return getCategories().get(0).getData().get("arguments");
        }
    }

    public void setArguments(String arguments) {
        getCategories().get(0).getData().put("arguments", arguments);
    }

    public String getAttributes() {
        if (getCategories().get(0).getData().get("attributes") == null) {
            return group.getCategories().get(1).getData().get("attributes");
        } else {
            return getCategories().get(0).getData().get("attributes");
        }
    }

    public void setAttributes(String attributes) {
        getCategories().get(0).getData().put("attributes", attributes);
    }

    public String getEnvironment() {
        if (getCategories().get(0).getData().get("environment") == null) {
            return group.getCategories().get(1).getData().get("environment");
        } else {
            return getCategories().get(0).getData().get("environment");
        }
    }

    public void setEnvironment(String environment) {
        getCategories().get(0).getData().put("environment", environment);
    }

    public String getExecutable() {
        if (getCategories().get(0).getData().get("executable") == null) {
            return group.getCategories().get(1).getData().get("executable");
        } else {
            return getCategories().get(0).getData().get("executable");
        }
    }

    public void setExecutable(String executable) {
        getCategories().get(0).getData().put("environment", executable);
    }

    public String getPostStage() {
        if (getCategories().get(0).getData().get("poststage") == null) {
            return group.getCategories().get(1).getData().get("poststage");
        } else {
            return getCategories().get(0).getData().get("poststage");
        }
    }

    public void setPostStage(String postStage) {
        getCategories().get(0).getData().put("poststage", postStage);
    }

    public String getPreStage() {
        if (getCategories().get(0).getData().get("prestage") == null) {
            return group.getCategories().get(1).getData().get("prestage");
        } else {
            return getCategories().get(0).getData().get("prestage");
        }
    }

    public void setPreStage(String preStage) {
        getCategories().get(0).getData().put("prestage", preStage);
    }

    public String getStderr() {
        if (getCategories().get(0).getData().get("stderr") == null) {
            return group.getCategories().get(1).getData().get("stderr");
        } else {
            return getCategories().get(0).getData().get("stderr");
        }
    }

    public void setStderr(String stderr) {
        getCategories().get(0).getData().put("stderr", stderr);
    }

    public String getStdout() {
        if (getCategories().get(0).getData().get("stdout") == null) {
            return group.getCategories().get(1).getData().get("stdout");
        } else {
            return getCategories().get(0).getData().get("stdout");
        }
    }

    public void setStdout(String stdout) {
        getCategories().get(0).getData().put("stdout", stdout);
    }

}