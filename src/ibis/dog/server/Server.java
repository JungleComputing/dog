package ibis.dog.server;

import ibis.dog.shared.Communication;
import ibis.dog.shared.FeatureVector;
import ibis.dog.shared.MachineDescription;
import ibis.dog.shared.RGB24Image;
import ibis.dog.shared.Reply;
import ibis.dog.shared.Request;
import ibis.dog.shared.ServerDescription;
import ibis.dog.shared.Upcall;
import ibis.ipl.IbisCreationFailedException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;

import jorus.parallel.PxSystem;
import jorus.weibull.CxWeibull;

public class Server implements Upcall {

    public static final int DEFAULT_TIMEOUT = 5000;

    private ServerDescription me;

    private boolean master;

    private Communication comm;

    private boolean registered = false;

    private LinkedList<Request> requests = new LinkedList<Request>();

    private Server(String[] args) throws IbisCreationFailedException,
            IOException {
        // Node 0 needs to provide an Ibis to contact the outside world.

        if (master = (PxSystem.myCPU() == 0)) {

            comm = new Communication("Server", this);

            // Install a shutdown hook that terminates ibis.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    end();
                }
            });

            me = new ServerDescription(comm.getMachineDescription(), comm
                    .getLocation());
        }
    }

    private void end() {
        System.out.println("Ending server");
        unregister();
        comm.end();
    }

    private synchronized void registered(boolean value) {
        registered = value;
        notifyAll();
    }

    private synchronized boolean getRegistered() {
        return registered;
    }

    private synchronized boolean waitUntilRegistered(long timeout) {
        long start = System.currentTimeMillis();
        long timeLeft = timeout;

        while (!registered && timeLeft > 0) {
            try {
                wait(timeLeft);
            } catch (InterruptedException e) {
                // ignored
            }

            timeLeft = System.currentTimeMillis() - start;
        }

        return registered;
    }

    private void unregister() {

        System.out.println("Server unregistering with broker...");
        // Try and find the broker.
        MachineDescription broker = comm.findMachine("Broker", "Broker");

        if (broker == null) {
            System.err.println("Failed to find broker!");
            return;
        }

        try {
            comm.send(broker, Communication.BROKER_REQ_UNREGISTER, me);
        } catch (Exception e) {
            System.err.println("Problem while contacting broker!");
            e.printStackTrace(System.err);
        }
    }

    private boolean register(long timeout) {
        System.out.println("Server registering with broker...");

        // Try and find the broker.
        MachineDescription broker = comm.findMachine("Broker", "Broker");

        if (broker == null) {
            System.err.println("Failed to find broker!");
            return false;
        }

        try {
            comm.send(broker, Communication.BROKER_REQ_REGISTER, me);
        } catch (Exception e) {
            System.err.println("Problem while contacting broker!");
            e.printStackTrace(System.err);
            return false;
        }

        waitUntilRegistered(timeout);

        System.out.println("Succesfully registered at broker!");
        return true;
    }

    private synchronized void queueRequest(Request request) {
        requests.addLast(request);
        notifyAll();
    }

    private synchronized Request getRequest(long timeout) {
        long start = System.currentTimeMillis();
        long timeLeft = timeout;

        while (requests.size() == 0 && timeLeft > 0) {
            try {
                wait(timeLeft);
            } catch (InterruptedException e) {
                // ignored
            }

            timeLeft = System.currentTimeMillis() - start;
        }

        if (requests.size() > 0) {
            return requests.removeFirst();
        } else {
            return null;
        }
    }

    public void upcall(byte opcode, Object... objects) throws IOException {
        // We can get messages here from both clients and the broker.

        switch (opcode) {
        case Communication.SERVER_REGISTERED: {
            System.out.println("REQ_REGISTERED received...");
            registered(true);
            break;
        }

        case Communication.SERVER_REQUEST: {
            System.out.println("SERVER_REQUEST received...");
            queueRequest((Request) objects[0]);
            break;
        }

        default: {
            System.err.println("Server received unknown opcode!");
            // TODO: what should we do with the message here ?
        }
        }
    }

    private void processRequest(boolean master) {
        Request r = null;
        Serializable reply = null;

        int operation = -1;
        RGB24Image img = null;

        long start = System.currentTimeMillis();
        long request = start;
        long decompress = start;
        long endop;
        long commdone;
        long end;

        if (master) {

            // The master should dequeue a request and broadcast
            // the details.

            r = getRequest(DEFAULT_TIMEOUT);

            request = System.currentTimeMillis();

            System.err.println(PxSystem.myCPU() + " Got request " + r);
            if (r == null) {
                return;
            }
            operation = r.operation;
            img = r.image.toRGB24();

            decompress = System.currentTimeMillis();

            try {
                PxSystem.broadcastValue(img.width);
                PxSystem.broadcastValue(img.height);
                PxSystem.broadcastValue(operation);
            } catch (Exception e) {
                // TODO: REACT TO FAILURE PROPERLY
                e.printStackTrace(System.err);
            }

        } else {

            try {
                int width = PxSystem.broadcastValue(0);
                int height = PxSystem.broadcastValue(0);
                operation = (byte) PxSystem.broadcastValue(0);
                img = new RGB24Image(width, height);
            } catch (Exception e) {
                // TODO: REACT TO FAILURE PROPERLY
                e.printStackTrace(System.err);
            }
        }

        commdone = System.currentTimeMillis();

        switch (operation) {
        case Request.OPERATION_LEARN:
        case Request.OPERATION_RECOGNIZE: {

            FeatureVector v = new FeatureVector(CxWeibull.getNrInvars(),
                    CxWeibull.getNrRfields());
            CxWeibull.doRecognize(img.width, img.height, img.pixels, v.vector);
            reply = v;

            break;
        }
            // case Request.OPERATION_LABELING: {
            // pxhi.doTrecLabeling(img.width, img.height, img.pixels);
            // reply = img;
            // break;
            // }
        case Request.OPERATION_DUMMY: {
            reply = new Integer(123);
            break;
        }
        default: {
            if (master) {
                System.out.println("Unknown operation: " + r.operation);
            }
        }
        }

        endop = System.currentTimeMillis();

        if (master) {
            System.err.println("Sending reply....");
            try {
                comm.send(r.replyAddress, Communication.CLIENT_REPLY_REQUEST,
                        new Reply(me, r.sequenceNumber, r.operation, reply));
            } catch (Exception e) {
                System.err.println("Failed to return reply to "
                        + r.replyAddress);
            }
            System.err.println("Reply send....");
        }

        end = System.currentTimeMillis();

        System.out.println("Total time   " + (end - start) + " ms.");
        System.out.println("  request    " + (request - start) + " ms.");
        System.out.println("  decompress " + (decompress - request) + " ms.");
        System.out.println("  bcast      " + (commdone - decompress) + " ms.");
        System.out.println("  op         " + (endop - commdone) + " ms.");
        System.out.println("  reply      " + (end - endop) + " ms.");

        PxSystem.printStatistics();

    }

    private void run() {
        while (true) {
            if (master && !getRegistered()) {
                if (!register(DEFAULT_TIMEOUT)) {
                    System.err.println("Server not registered yet...");
                    try {
                        Thread.sleep(DEFAULT_TIMEOUT);
                    } catch (InterruptedException e) {
                        // ignored
                    }
                }
            } else {
                processRequest(master);
            }
        }
    }

    public static void main(String[] args) {
        String poolName = null;
        String poolSize = null;

        System.out.println("Server: " + Arrays.toString(args));

        if (args.length == 0) {
            poolName = System.getProperty("ibis.deploy.job.id", null);
            poolSize = System.getProperty("ibis.deploy.job.size", null);
        } else if (args.length == 2) {
            poolName = args[0];
            poolSize = args[1];
        }

        if (poolName == null || poolSize == null) {

            System.err
                    .println("USAGE: Server poolname poolsize OR set ibis.deploy.job.id and ibis.deploy.job.size properties");
            System.exit(1);
        }

        System.out.println("Initializing Parallel System...");
        try {
            PxSystem.initParallelSystem(poolName, poolSize);
        } catch (Exception e) {
            System.err.println("Could not initialize Parallel system");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.out.println("nrCPUs = " + PxSystem.nrCPUs());
        System.out.println("myCPU = " + PxSystem.myCPU());

        try {
            new Server(args).run();
        } catch (Throwable e) {
            System.err.println("Server died unexpectedly!");
            e.printStackTrace(System.err);
        }

        System.out.println("Exit Parallel System...");
        try {
            PxSystem.exitParallelSystem();
        } catch (Exception e) {
            // Nothing we can do now...
        }
    }
}
