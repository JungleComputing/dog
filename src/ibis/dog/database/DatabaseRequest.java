package ibis.dog.database;

import ibis.dog.FeatureVector;
import ibis.ipl.IbisIdentifier;
import java.io.Serializable;

public class DatabaseRequest implements Serializable {
    
    public enum Function {
        LEARN, RECOGNIZE;
    }
    
    private static final long serialVersionUID = 1L;
    
    private final Function function;
    
    //only valid for recognise
    private final int nrOfResults;
    
    //only valid for learn
    private final Item item;
    
    private final long sequenceNumber;
    
    private final IbisIdentifier server;

    private final FeatureVector vector;

    private final IbisIdentifier replyAddress;
    
    public DatabaseRequest(Function function, int nrOfResults, Item item, long sequenceNumber, IbisIdentifier server, FeatureVector vector, IbisIdentifier replyAddress) {
        this.function = function;
        this.nrOfResults = nrOfResults;
        this.item = item;
        this.sequenceNumber = sequenceNumber;
        this.server = server;
        this.vector = vector;
        this.replyAddress = replyAddress;
    }
    
    public Function getFunction() {
        return function;
    }

    public int getNrOfResults() {
        return nrOfResults;
    }
    
    public Item getItem() {
        return item;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public IbisIdentifier getServer() {
        return server;
    }


    public FeatureVector getVector() {
        return vector;
    }

    public IbisIdentifier getReplyAddress() {
        return replyAddress;
    }

}
