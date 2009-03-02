package ibis.dog.broker;

import java.io.Serializable;

import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.RGB24Image;

/**
 * An item in the database of objects.
 * 
 * @author ndrost
 */
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final FeatureVector vector;

    private final String name;

    private final String author;

    private final RGB24Image thumbnail;

    public Item(FeatureVector vector, String name, String author,
            RGB24Image thumbnail) {
        this.vector = vector;
        this.name = name;
        this.author = author;
        this.thumbnail = thumbnail;
    }

    /**
     * @return the vector
     */
    public FeatureVector getVector() {
        return vector;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the thumbnail
     */
    public RGB24Image getThumbnail() {
        return thumbnail;
    }


}
