package ibis.dog.shared;


import java.io.Serializable;


public class Request implements Serializable
{
    // Generated by eclipse
    private static final long serialVersionUID = 6017303910252228667L;

    public static final byte OPERATION_RECOGNISE = 1; 
    public static final byte OPERATION_LABELING  = 2; 
    public static final byte OPERATION_DUMMY     = 3; 
    
    public byte operation;    
    public long sequenceNumber;
//    public RGB24Image image;
    public CompressedImage image;
    public MachineDescription replyAddress;        
        

    public Request(byte operation, long sequenceNumber,
				CompressedImage image, MachineDescription replyAddress)
//				   RGB24Image image, MachineDescription replyAddress)
	{ 
        this.operation = operation;
        this.sequenceNumber = sequenceNumber;
        this.image = image;
        this.replyAddress = replyAddress;
    }
}
