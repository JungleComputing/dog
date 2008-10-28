package ibis.deploy.library;

import java.io.IOException;
import java.util.Set;

/**
 * A PropertySetGroup is the abstract base class of a group of PropertySets that
 * has some default values.
 * 
 * @author rkemp
 */
public abstract class PropertySetGroup extends PropertySet {

    /**
     * Constructs a PropertySetGroup with the given name.
     * 
     * @param name
     *                the name of the new PropertySetGroup
     * @throws Exception
     *                 if the name is <code>null</code>
     */
    public PropertySetGroup(String name) throws Exception {
        super(name);
    }

    /**
     * Returns a Set of the PropertySets that are in this PropertySetGroup
     * 
     * @return Returns a Set of the PropertySets that are in this
     *         PropertySetGroup
     */
    public abstract Set<? extends PropertySet> getPropertySets();

    /**
     * Saves this PropertySetGroup to a properties file with the provided
     * <code>filename</code>. Note that any comments in an existing property
     * file will be removed.
     * 
     * @param filename
     *                the file to write the PropertySetGroup to
     * @throws IOException
     *                 if something goes wrong during the save process
     */
    public abstract void save(String filename) throws IOException;

}
