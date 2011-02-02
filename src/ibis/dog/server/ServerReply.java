package ibis.dog.server;

import ibis.dog.FeatureVector;
import ibis.ipl.IbisIdentifier;

import java.io.Serializable;

public class ServerReply implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long timeStamp;
    private final IbisIdentifier server;
    private final FeatureVector result;
    private final Exception exception;

    public ServerReply(IbisIdentifier server, long sequenceNumber,
            FeatureVector result) {
        this.server = server;
        this.timeStamp = sequenceNumber;
        this.result = result;
        this.exception = null;
    }

    public ServerReply(IbisIdentifier server, long sequenceNumber,Exception exception) {
        this.server = server;
        this.timeStamp = sequenceNumber;
        this.result = null;
        this.exception = exception;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    public IbisIdentifier getServer() {
        return server;
    }

    public FeatureVector getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}
