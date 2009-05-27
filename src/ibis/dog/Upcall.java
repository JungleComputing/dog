package ibis.dog;

import ibis.ipl.IbisIdentifier;

public interface Upcall {
    public void gotMessage(Object object) ;

    public void newServer(IbisIdentifier identifier);

    public void serverGone(IbisIdentifier identifier);
}