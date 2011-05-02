package ibis.dog.server;

import ibis.dog.FeatureVector;
import ibis.media.imaging.Image;

import ibis.ipl.util.rpc.RemoteException;

public interface ServerInterface {

    FeatureVector calculateVector(Image image)
            throws RemoteException, Exception;

    void waitUntilInitialized() throws RemoteException;

}
