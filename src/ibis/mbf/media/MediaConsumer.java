package ibis.mbf.media;


public interface MediaConsumer {
    public void gotImage(byte [] pixels, int width, int height);
}
