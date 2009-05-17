package ibis.dog.database;

import ibis.ipl.IbisIdentifier;

import java.io.Serializable;
import java.util.SortedMap;

public class DatabaseReply implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long sequenceNumber;

    private final IbisIdentifier server;

    private final SortedMap<Double, Item> results;

    public DatabaseReply(IbisIdentifier server, long sequenceNumber,
            SortedMap<Double, Item> results) {
        this.server = server;
        this.sequenceNumber = sequenceNumber;
        this.results = results;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public IbisIdentifier getServer() {
        return server;
    }

    public SortedMap<Double, Item> getResults() {
        return results;
    }
}
