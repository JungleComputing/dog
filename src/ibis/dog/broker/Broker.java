package ibis.dog.broker;

import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.ipl.IbisCreationFailedException;

import java.io.IOException;
import java.util.HashSet;
import java.util.SortedMap;

public class Broker implements Upcall {

    public static final int UPDATE_INTERVAL = 10000;

    private final Communication communication;

    private final Database database;

    private final HashSet<ServerDescription> servers = new HashSet<ServerDescription>();

    private Broker() throws IbisCreationFailedException, IOException {

        // Create an Communication object
        communication = new Communication("Broker", this);

        database = new Database();

        // Install a shutdown hook that terminates ibis.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.err.println("Ending broker");
                communication.end();
            }
        });

        // Elect this process to be the broker
        communication.elect("Broker");
    }

    private void pingServers() {

        // System.out.println("Ping to all servers to check if they are
        // alive...");

        ServerDescription[] servers = getServers();

        for (ServerDescription s : servers) {
            try {
                communication.send(s, Communication.SERVER_REGISTERED);
            } catch (Exception cre) {
                System.err.println("warning: cannot reach server: "
                        + s.getName());
                cre.printStackTrace(System.err);
                // FIXME: ignore errors for now
                // removeServer(s);
            }
        }
    }

    private synchronized boolean addServer(ServerDescription s) {
        if (servers.contains(s)) {
            return false;
        }

        servers.add(s);
        return true;
    }

    private synchronized boolean removeServer(ServerDescription s) {
        return servers.remove(s);
    }

    private synchronized ServerDescription[] getServers() {
        return servers.toArray(new ServerDescription[servers.size()]);
    }

    public void upcall(byte opcode, Object... objects) throws IOException {
        switch (opcode) {
        case Communication.BROKER_REQ_GET_SERVERS:
            // It a lookup request from a client.
            communication.send((MachineDescription) objects[0],
                    Communication.CLIENT_REPLY_GETSERVERS,
                    (Object[]) getServers());
            break;
        case Communication.BROKER_REQ_REGISTER:
            // It is a registration request from a server.
            ServerDescription s = (ServerDescription) objects[0];

            boolean accept = addServer(s);

            if (accept) {
                communication.send(s, Communication.SERVER_REGISTERED);
            }

            break;
        case Communication.BROKER_REQ_UNREGISTER:
            // It is a de-registration request from a server
            ServerDescription server = (ServerDescription) objects[0];
            removeServer(server);
            break;
        case Communication.BROKER_REQ_LEARN:
            // Request to add something to database
            Item item = (Item) objects[0];
            database.learn(item);
            break;
        case Communication.BROKER_REQ_RECOGNIZE:
            // Request to add something to database
            MachineDescription machineDescription = (MachineDescription) objects[0];
            FeatureVector vector = (FeatureVector) objects[1];
            Integer nrOfResults = (Integer) objects[2];
            // random object useful for clients
            Object tag = objects[3];

            SortedMap<Double, Item> results = database.recognize(vector,
                    nrOfResults);

            communication.send(machineDescription,
                    Communication.CLIENT_REPLY_RECOGNIZE, results, tag);
            break;
        default:
            System.err.println("Received unknown opcode: " + opcode);
        }
    }

    private void run() {

        try {
            while (true) {
                Thread.sleep(UPDATE_INTERVAL);
                pingServers();
                database.save();
            }
        } catch (Throwable e) {
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {

        try {
            new Broker().run();
        } catch (Throwable e) {
            System.err.println("Broker died unexpectedly!");
            e.printStackTrace(System.err);
        }
    }

}
