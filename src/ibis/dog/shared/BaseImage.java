package ibis.dog.shared;

import java.io.Serializable;

public interface BaseImage extends Serializable {
    
    public RGB24Image toRGB24();

}
