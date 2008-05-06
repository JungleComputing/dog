package ibis.mbf.shared;

import java.io.IOException;
import java.util.Properties;

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.ReceivePortConnectUpcall;
import ibis.ipl.SendPort;
import ibis.ipl.SendPortIdentifier;
import ibis.ipl.WriteMessage;



public class Communication
implements MessageUpcall, ReceivePortConnectUpcall
{
    public static final int DEFAULT_TIMEOUT = 5000;
    
    public static final byte BROKER_REQ_REGISTER       = 0;
    public static final byte BROKER_REQ_GET_SERVERS    = 1;
    public static final byte BROKER_REQ_UNREGISTER     = 2;
    public static final byte CLIENT_REPLY_GETSERVERS   = 10;
    public static final byte CLIENT_REPLY_REQUEST      = 11;
    public static final byte SERVER_REGISTERED         = 20;
    public static final byte SERVER_REQUEST            = 21;
    

    private final PortType portType =
							new PortType(
								PortType.COMMUNICATION_RELIABLE,
								PortType.SERIALIZATION_OBJECT,
								PortType.RECEIVE_AUTO_UPCALLS,
								PortType.CONNECTION_MANY_TO_ONE, 
								PortType.CONNECTION_UPCALLS);

    private final IbisCapabilities ibisCapabilities =
								new IbisCapabilities(
								IbisCapabilities.MALLEABLE,
								IbisCapabilities.ELECTIONS_STRICT);

    private Ibis ibis;
    private ReceivePort receive;
    private final Upcall upcall;
    private String myName;
    

    public Communication(String name, Upcall upcall)
	throws IbisCreationFailedException, IOException
	{ 
        this.upcall = upcall;
		myName = name;
        
        // Create an Ibis

/*		Properties ibisDistProps = new Properties();
		ibisDistProps.setProperty("ibis.pool.name", poolName);
        ibis = IbisFactory.createIbis(ibisCapabilities, ibisDistProps,
									  true, null, portType);
*/
        ibis = IbisFactory.createIbis(ibisCapabilities, null, portType);
        
        // Create the receive port for the broker and switch it on. 
        receive = ibis.createReceivePort(portType,
										 name, this, this, null);
        receive.enableConnections();
        receive.enableMessageUpcalls();
        
        // Install a shutdown hook that terminates ibis. 
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    receive.close(DEFAULT_TIMEOUT);
                    System.err.println(myName + " terminating");
                    ibis.end();
                } catch (IOException e) {
                    // Ignored
                }
            }
        });
    }
    

    public void elect(String name) throws IOException
	{ 
        ibis.registry().elect(name);
    }
    

    public IbisIdentifier electionResult(String name)
	throws IOException
	{
        return ibis.registry().getElectionResult(name, DEFAULT_TIMEOUT);
    }
   

    public void send(MachineDescription target, byte opcode)
	throws IOException
	{ 
        send(target, opcode, (Object []) null);
    }
    

    public void send(MachineDescription target,
					byte opcode, Object ... objects) throws IOException
	{ 
        // Create a sendport and connect to the target
        SendPort sp = ibis.createSendPort(portType);
        
        if (target.receiveID != null) { 
            sp.connect(target.receiveID, DEFAULT_TIMEOUT, true);
        } else { 
            sp.connect(target.ibisID,
					   target.port, DEFAULT_TIMEOUT, true);
        }
        
        try { 
            WriteMessage wm = sp.newMessage();
            wm.writeByte(opcode);
            
            if (objects != null) { 
                wm.writeInt(objects.length);

                for (Object o: objects) { 
                    wm.writeObject(o);
                }
            } else { 
                wm.writeInt(0);
            }
                
            wm.finish();
        } finally { 
            sp.close();                
        }    
    }


    public void upcall(ReadMessage rm)
	throws IOException, ClassNotFoundException
	{
        byte opcode = rm.readByte();
        int objects = rm.readInt();
        
        Object [] tmp = null;
        
        if (objects > 0) { 
            tmp = new Object[objects];
            
            for (int i=0;i<objects;i++) { 
                tmp[i] = rm.readObject();
            }
        }

        rm.finish();
      
        try { 
            upcall.upcall(opcode, tmp);
        } catch (Throwable e) { 
            System.err.println("Upcall produced exception!");
            e.printStackTrace(System.err);
        }
    }
    

    public boolean gotConnection(ReceivePort rp,
								 SendPortIdentifier sp)
	{
        // Only used for verboseness 
        //System.out.println("Received connection from: " 
        //        + sp.ibisIdentifier().toString());
        return true;
    }


    public void lostConnection(ReceivePort rp,
							   SendPortIdentifier sp, Throwable cause)
	{
        // Only used for verboseness 
        //System.out.println("Lost connection from: " 
        //    + sp.ibisIdentifier().toString() 
        //    + (cause != null ? " because " + cause.getMessage():"")); 
    }
    

    public MachineDescription getMachineDescription() { 
        return new MachineDescription(receive.identifier());
    }
    

    public MachineDescription findMachine(String name, String port)
	{ 
        IbisIdentifier id = null;
        try { 
            id = ibis.registry().getElectionResult(name,
													DEFAULT_TIMEOUT);
        } catch (Exception e) {
            return null;
        }
        return new MachineDescription(id, port);
    }
}
