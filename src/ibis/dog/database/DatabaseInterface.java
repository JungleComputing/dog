package ibis.dog.database;

import ibis.dog.FeatureVector;

import java.rmi.RemoteException;
import java.util.TreeMap;

public interface DatabaseInterface {

    public void learn(Item item) throws RemoteException;

    public TreeMap<Double, Item> recognize(FeatureVector vector, int nrOfResults) throws RemoteException;

    public Item recognize(FeatureVector vector) throws RemoteException;

    public int size() throws RemoteException;

}
