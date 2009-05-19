package ibis.dog.shared;

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Communication implements MessageUpcall, RegistryEventHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(Communication.class);

    public static final int DEFAULT_TIMEOUT = 30000;

    public static final String DATABASE_ROLE = "database";
    public static final String CLIENT_ROLE = "client";
    public static final String SERVER_ROLE = "server";

    public static final String PORT_NAME = "receiveport";

    private final PortType portType = new PortType(
            PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT,
            PortType.RECEIVE_AUTO_UPCALLS, PortType.CONNECTION_MANY_TO_ONE,
            PortType.CONNECTION_UPCALLS);

    private final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.MALLEABLE, IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

    private Ibis ibis;

    private final String role;

    private ReceivePort receivePort;

    private final Upcall upcall;

    private final HashMap<IbisIdentifier, SendPort> connectionCache;

    private IbisIdentifier database = null;

    public Communication(String role, Upcall upcall)
            throws IbisCreationFailedException, IOException {

        connectionCache = new HashMap<IbisIdentifier, SendPort>();

        this.upcall = upcall;
        this.role = role;

        logger.info("Starting communication");

        // Create an Ibis
        ibis = IbisFactory.createIbis(ibisCapabilities, null, true, this, null,
                role, portType);

        // Create the receive port and switch it on.
        receivePort = ibis.createReceivePort(portType, PORT_NAME, this);
        receivePort.enableConnections();
        receivePort.enableMessageUpcalls();

        ibis.registry().enableEvents();

        // try to become the "main" database
        if (role.equals(DATABASE_ROLE)) {
            ibis.registry().elect(DATABASE_ROLE);
        }

        logger.info("Done starting communication");
    }
    
    public IbisIdentifier getIdentifier() {
        return ibis.identifier();
    }

    private SendPort getConnection(IbisIdentifier target) throws IOException {
        SendPort result;

        synchronized (this) {
            result = connectionCache.get(target);
        }

        if (result == null) {

            result = ibis.createSendPort(portType);

            result.connect(target, PORT_NAME, DEFAULT_TIMEOUT, true);

            synchronized (this) {
                if (connectionCache.get(target) != null) {
                    logger.warn("Multiple sendports to a single target: "
                            + target + ", removing one");
                    result.close();
                    return connectionCache.get(target);
                }

                connectionCache.put(target, result);

                if (logger.isDebugEnabled()) {
                    logger.debug("New connection created to \"" + target
                            + "\", connection size now: "
                            + connectionCache.size());
                }
            }
        }

        return result;
    }

    private synchronized void removeConnection(SendPort connection) {
        for (Map.Entry<IbisIdentifier, SendPort> entry : connectionCache
                .entrySet()) {
            if (entry.getValue().equals(connection)) {
                connectionCache.remove(entry.getKey());
                return;
            }
        }
        logger.warn("Connection " + connection + " not found");
    }

    public void send(IbisIdentifier target, Object object) throws IOException {
        SendPort connection = getConnection(target);

        try {
            WriteMessage wm = connection.newMessage();
            wm.writeObject(object);
            wm.finish();
        } catch (IOException e) {
            removeConnection(connection);
            throw e;
        }
    }

    public synchronized IbisIdentifier getDatabase() {
        return database;
    }

    public void upcall(ReadMessage rm) throws IOException,
            ClassNotFoundException {

        Object object = rm.readObject();

        rm.finish();

        try {
            upcall.gotMessage(object);
        } catch (Throwable e) {
            logger.error("Upcall produced exception!", e);
        }
    }

    public void end() {
        SendPort[] sendPorts;

        synchronized (this) {
            sendPorts = connectionCache.values().toArray(new SendPort[0]);
        }

        try {
            for (SendPort sendPort : sendPorts) {
                sendPort.close();
            }
        } catch (Exception e) {
            logger.error("Problems while closing sendport!", e);
        }

        try {
            receivePort.close(DEFAULT_TIMEOUT);
        } catch (Exception e) {
            logger.error("Problems while closing receiveport!", e);
        }

        try {
            ibis.end();
        } catch (Exception e) {
            logger.error("Problems while closing ibis!", e);
        }

        logger.info("Communication done");
    }

    @Override
    public void electionResult(String electionName, IbisIdentifier winner) {
        if (electionName == null) {
            logger.error("election Name = null");
            return;
        }
        
        if (electionName.equals("database")) {
            logger.info("Database election updated to: " + winner);
            synchronized (this) {
                this.database = winner;
            }

            if (role.equals(DATABASE_ROLE) && winner == null) {
                // we're the database, try to win election again
                try {
                    ibis.registry().elect(DATABASE_ROLE);
                } catch (IOException e) {
                    logger.error("Could not re-elect database", e);
                }
            }
        }
    }

    @Override
    public synchronized void joined(IbisIdentifier joinedIbis) {
        String role = joinedIbis.tagAsString();
        
        logger.info("New Ibis: " + joinedIbis + " role = \"" + role + "\"");


        if (role != null && role.equals(SERVER_ROLE)) {
            upcall.newServer(joinedIbis);
        }
    }

    @Override
    public void left(IbisIdentifier leftIbis) {
        gone(leftIbis);
    }

    @Override
    public void died(IbisIdentifier corpse) {
        gone(corpse);
    }

    private void gone(IbisIdentifier identifier) {
        synchronized (this) {
            if (identifier.equals(database)) {
                database = null;
            }
        }

        String role = identifier.tagAsString();
        
        logger.info("Ibis Gone: " + identifier + " role = \"" + role + "\"");


        if (role != null && role.equals(SERVER_ROLE)) {
            upcall.serverGone(identifier);
        }
    }

    @Override
    public void gotSignal(String signal, IbisIdentifier source) {
        // IGNORE
    }

    @Override
    public void poolClosed() {
        // IGNORE
    }

    @Override
    public void poolTerminated(IbisIdentifier source) {
        // IGNORE
    }

}
