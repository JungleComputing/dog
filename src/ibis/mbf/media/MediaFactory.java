package ibis.mbf.media;

public interface MediaFactory {

    public Media getMedia(MediaConsumer consumer, String description, 
            int width, int height, int buffers, int delay);
    
    public Media getMedia(String description, int width, int height);
}
