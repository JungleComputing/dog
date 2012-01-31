package ibis.dog.server;

import ibis.media.imaging.Image;

import java.rmi.RemoteException;

public interface ServerInterface {

    Image calculateDisparity(Image[] images)
            throws RemoteException, Exception;

    void waitUntilInitialized() throws RemoteException;

}
