package ibis.deploy.library;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Properties might be grouped together in a category. So all 'java' properties
 * of an application might form the 'java' PropertyCategory.
 * 
 * @author rkemp
 */
public class PropertyCategory implements Parcelable {

    private SortedMap<String, String> data;

    private String name;

    /**
     * Constructs a PropertyCategory with a given name and a data set containing
     * key value pairs belonging to this category.
     * 
     * @param name
     *                the name of the category
     * @param data
     *                the data in the category
     */
    public PropertyCategory(String name, SortedMap<String, String> data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Returns the name of the category
     * 
     * @return the name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data set containing the key value pairs belonging to this
     * category
     * 
     * @return the data set containing the key value pairs belonging to this
     *         category
     */
    public SortedMap<String, String> getData() {
        return data;
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        List<String> keys = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        keys.addAll(data.keySet());
        values.addAll(data.values());
        out.writeStringList(keys);
        out.writeStringList(values);

    }

    public static final Parcelable.Creator<PropertyCategory> CREATOR = new Parcelable.Creator<PropertyCategory>() {
        public PropertyCategory createFromParcel(Parcel in) {
            try {
                PropertyCategory result = new PropertyCategory(in.readString(),
                        null);
                List<String> keys = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                System.out.println("reading keys...");
                in.readStringList(keys);
                System.out.println("reading values...");
                in.readStringList(values);
                SortedMap<String, String> data = new TreeMap<String, String>();
                for (int i = 0; i < keys.size(); i++) {
                    data.put(keys.get(i), values.get(i));
                }
                result.data = data;
                return result;
            } catch (Exception e) {
                System.out.println("OOPS");
                e.printStackTrace(System.out);
                return null;
            }

        }

        public PropertyCategory[] newArray(int size) {
            return new PropertyCategory[size];
        }
    };

}
