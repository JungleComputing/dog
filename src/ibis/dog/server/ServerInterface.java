package ibis.dog.server;

import ibis.dog.FeatureVector;
import ibis.imaging4j.Image;

import java.rmi.RemoteException;

public interface ServerInterface {

    FeatureVector calculateVector(Image image)
            throws RemoteException, Exception;

    void waitUntilInitialized() throws RemoteException;

}
