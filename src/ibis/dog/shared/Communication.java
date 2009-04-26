package ibis.dog.shared;

import ibis.ipl.ConnectionFailedException;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.ReceivePortConnectUpcall;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.SendPort;
import ibis.ipl.SendPortIdentifier;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Communication implements MessageUpcall, ReceivePortConnectUpcall,
        RegistryEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(Communication.class);
    
    public static final int DEFAULT_TIMEOUT = 30000;

    public static final byte BROKER_REQ_REGISTER = 0;

    public static final byte BROKER_REQ_GET_SERVERS = 1;

    public static final byte BROKER_REQ_UNREGISTER = 2;

    public static final byte BROKER_REQ_RECOGNIZE = 3;

    public static final byte BROKER_REQ_LEARN = 4;

    public static final byte CLIENT_REPLY_GETSERVERS = 10;

    public static final byte CLIENT_REPLY_REQUEST = 11;

    public static final byte CLIENT_REPLY_RECOGNIZE = 12;

    public static final byte SERVER_REGISTERED = 20;

    public static final byte SERVER_REQUEST = 21;

    private final PortType portType = new PortType(
            PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT,
            PortType.RECEIVE_AUTO_UPCALLS, PortType.CONNECTION_MANY_TO_ONE,
            PortType.CONNECTION_UPCALLS);

    private final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.MALLEABLE, IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

    private Ibis ibis;

    private ReceivePort receive;

    private final Upcall upcall;

    private String myName;

    private final HashMap<MachineDescription, SendPort> connectionCache;

    public Communication(String name, Upcall upcall)
            throws IbisCreationFailedException, IOException {

        connectionCache = new HashMap<MachineDescription, SendPort>();

        this.upcall = upcall;
        myName = name;

        System.out.println("####### starting ibis");

        System.out.println("PROP:" + System.getProperties());

        // Create an Ibis
        ibis = IbisFactory.createIbis(ibisCapabilities, null, true, this,
                portType);

        System.out.println("####### DONE starting ibis");

        // Create the receive port for the broker and switch it on.
        receive = ibis.createReceivePort(portType, name, this, this, null);
        receive.enableConnections();
        receive.enableMessageUpcalls();

    }

    public void end() {
        try {
            receive.close(DEFAULT_TIMEOUT);
            System.err.println(myName + " terminating");
            ibis.end();
        } catch (IOException e) {
            // Ignored
        }
    }

    public void elect(String name) throws IOException {
        ibis.registry().elect(name);
    }

    public IbisIdentifier electionResult(String name) throws IOException {

        return ibis.registry().getElectionResult(name, DEFAULT_TIMEOUT);
    }
    
    private SendPort getConnection(MachineDescription target) throws IOException {
        SendPort result;
        
        synchronized(this) {
            result = connectionCache.get(target);
        }
        
        if (result == null) {
            
            result = ibis.createSendPort(portType);
            
            if (target.receiveID != null) {
                result.connect(target.receiveID, DEFAULT_TIMEOUT, true);
            } else {
                result.connect(target.ibisID, target.port, DEFAULT_TIMEOUT, true);
            }
            
            synchronized(this) {
                SendPort old = connectionCache.put(target, result);
                
                if (old != null) {
                    logger.error("Eep! Multiple sendports to a single target: " + target);
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug("New connection created to \"" + target + "\", connection size now: "+ connectionCache.size());
                }
            }
        }
        
        return result;
    }
    
    private synchronized void removeConnection(SendPort connection) {
        for(Map.Entry<MachineDescription, SendPort> entry: connectionCache.entrySet()) {
            if (entry.getValue().equals(connection)) {
                connectionCache.remove(entry.getKey());
                return;
            }
        }
        logger.warn("Connection " + connection + " not found");
    }

    public void send(MachineDescription target, byte opcode) throws IOException {
        send(target, opcode, (Object[]) null);
    }
    
    public void send(MachineDescription target, byte opcode, Object... objects)
            throws IOException {
        SendPort connection = getConnection(target);

        try {
            WriteMessage wm = connection.newMessage();
            wm.writeByte(opcode);

            if (objects != null) {
                wm.writeInt(objects.length);

                for (Object o : objects) {
                    wm.writeObject(o);
                }
            } else {
                wm.writeInt(0);
            }

            wm.finish();
        } catch (Exception e) {
            removeConnection(connection);
        }
    }

    public void upcall(ReadMessage rm) throws IOException,
            ClassNotFoundException {
        byte opcode = rm.readByte();
        int objects = rm.readInt();

        Object[] tmp = null;

        if (objects > 0) {
            tmp = new Object[objects];

            for (int i = 0; i < objects; i++) {
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

    public boolean gotConnection(ReceivePort rp, SendPortIdentifier sp) {
        // Only used for verboseness
        // System.out.println("Received connection from: "
        // + sp.ibisIdentifier().toString());
        return true;
    }

    public void lostConnection(ReceivePort rp, SendPortIdentifier sp,
            Throwable cause) {
        // Only used for verboseness
        // System.out.println("Lost connection from: "
        // + sp.ibisIdentifier().toString()
        // + (cause != null ? " because " + cause.getMessage():""));
    }

    public MachineDescription getMachineDescription() {
        return new MachineDescription(receive.identifier());
    }

    public MachineDescription findMachine(String name, String port) {
        IbisIdentifier id = null;
        try {
            id = ibis.registry().getElectionResult(name, DEFAULT_TIMEOUT);
            // Test below added. A timeout results in null, not in an exception.
            // --Ceriel
            if (id == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return new MachineDescription(id, port);
    }

    public void exit() {

        // NOTE: we should make sure no sendport is in use...

        try {
            receive.close(DEFAULT_TIMEOUT);
        } catch (Exception e) {
            System.out.println("Problems while closing receiveport!");
            e.printStackTrace();
        }

        try {
            ibis.end();
        } catch (Exception e) {
            System.out.println("Problems while closing ibis!");
            e.printStackTrace();
        }

        System.out.println("Communication done");
    }

    public String getLocation() {
        Location location = ibis.identifier().location();

        if (location.numberOfLevels() == 0) {
            return "unknown";

        }

        return location.getLevel(location.numberOfLevels() - 1);
    }

    @Override
    public void died(IbisIdentifier corpse) {
        upcall.gone(corpse);
    }

    @Override
    public void electionResult(String electionName, IbisIdentifier winner) {
        // IGNORE
    }

    @Override
    public void gotSignal(String signal, IbisIdentifier source) {
        // IGNORE
    }

    @Override
    public void joined(IbisIdentifier joinedIbis) {
        // IGNORE
    }

    @Override
    public void left(IbisIdentifier leftIbis) {
        upcall.gone(leftIbis);
    }

    @Override
    public void poolClosed() {
        // IGNORe
    }

    @Override
    public void poolTerminated(IbisIdentifier source) {
        // IGNORE
    }

}
