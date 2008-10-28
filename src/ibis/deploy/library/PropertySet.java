package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A PropertySet is the abstract base class of a named object that contains data
 * in the form of a set of key, value pairs.
 * 
 * @author rkemp
 */
public class PropertySet implements Parcelable {

    private String name;

    private List<PropertyCategory> categories = new ArrayList<PropertyCategory>();

    /**
     * Constructs a new property set with the given name.
     * 
     * @param name
     *                the name of the new property set
     * @throws Exception
     *                 if name is <code>null</code>
     */
    public PropertySet(String name) throws Exception {
        if (name == null) {
            throw new Exception("property 'name' not found, but mandatory");
        }
        this.name = name;
    }

    /**
     * Loads a properties object from a file.
     * 
     * @param fileName
     *                the filename to load from
     * @return the loaded properties
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static TypedProperties getPropertiesFromFile(String fileName)
            throws FileNotFoundException, IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(fileName));
        return properties;
    }

    /**
     * Returns the PropertyCategories of this PropertySet.
     * 
     * @return the PropertyCategories of this PropertySet.
     */
    public List<PropertyCategory> getCategories() {
        return categories;
    }

    /**
     * Gets the name of the PropertySet
     * 
     * @return the name of the PropertySet
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of the PropertySet
     * 
     * @param name
     *                the name of the PropertySet
     */
    public final void setName(String name) {
        if (name == null) {
            throw new NullPointerException("setName got null parameter");
        }
        this.name = name;
    }

    /**
     * Returns the String representation of the PropertySet (this method will
     * call return {@link #getName()}
     * 
     * @return the String representation of the PropertySet (this method will
     *         call return {@link #getName()}
     */
    public final String toString() {
        return getName();
    }

    /**
     * Returns the default value for a given key in a given category or
     * <code>null</code> if the category or key doesn't exist.
     * 
     * @return the default value for a given key in a given category or
     *         <code>null</code> if the category or key doesn't exist.
     */
    public String defaultValueFor(String key, String category) {
        return null;
    }

    public void setPropertySet(PropertySet propertySet) {
        this.name = propertySet.name;
        this.categories = propertySet.categories;
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeInt(categories.size());
        for (PropertyCategory category : categories) {
            out.writeParcelable(category, flags);
        }
    }

    public static final Parcelable.Creator<PropertySet> CREATOR = new Parcelable.Creator<PropertySet>() {
        public PropertySet createFromParcel(Parcel in) {
            try {
                PropertySet result = new PropertySet(in.readString());
                int nrCategories = in.readInt();
                for (int i = 0; i < nrCategories; i++) {
                    result.categories.add((PropertyCategory) in
                            .readParcelable(this.getClass().getClassLoader()));
                }
                return result;
            } catch (Exception e) {
                System.out.println("EEPS: " + e);
                e.printStackTrace(System.out);
                return null;
            }

        }

        public PropertySet[] newArray(int size) {
            return new PropertySet[size];
        }
    };

    public PropertyCategory getCategory(String categoryName) {
        for (PropertyCategory category : categories) {
            if (category.getName().equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

}
