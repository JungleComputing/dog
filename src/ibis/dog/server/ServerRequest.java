package ibis.dog.server;

import ibis.imaging4j.Image;
import ibis.ipl.IbisIdentifier;

import java.io.Serializable;

public class ServerRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long sequenceNumber;

    private final Image image;

    private final IbisIdentifier replyAddress;

    public ServerRequest(long sequenceNumber, Image image, IbisIdentifier replyAddress) {
        this.sequenceNumber = sequenceNumber;
        this.image = image;
        this.replyAddress = replyAddress;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Image getImage() {
        return image;
    }

    public IbisIdentifier getReplyAddress() {
        return replyAddress;
    }
    
    public String toString() {
        return "ServerRequest " + sequenceNumber + " from " + replyAddress + ", format = " + image.getFormat() + ", dimensions = " + image.getWidth() + "x" + image.getHeight();
    }
    
    
}
