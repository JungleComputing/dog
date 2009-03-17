/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra  (fjseins@cs.vu.nl)
 *
 */

package jorus.parallel;

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.MessageUpcall;
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import jorus.array.CxArray2d;
import jorus.array.CxArray2dDoubles;
import jorus.operations.CxRedOp;
import jorus.operations.CxRedOpArray;

public class PxSystem {
    /** * Ibis Capabilities & PortTypes ******************************* */

    private static final int ALLREDUCE_FLAT = 0;
    private static final int ALLREDUCE_RING = 1;
    private static final int ALLREDUCE_TREE = 2;    
    private static final int ALLREDUCE_MPICH = 3;
    
    private static final int ALLREDUCE_MAX = 3;
    
    // NOTE: set to arbitrary value!
    private static int ALLREDUCE_SHORT_MSG = 2048;

    private static PortType portType = new PortType(
            PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_DATA,
            PortType.RECEIVE_EXPLICIT, PortType.CONNECTION_ONE_TO_ONE);


    // These are experimental to see which reduce to all implementation performs best -- J.
    /*	private static PortType portTypeOneToMany = new PortType(
			PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_DATA,
			PortType.RECEIVE_EXPLICIT, PortType.CONNECTION_ONE_TO_MANY);

	private static PortType portTypeManyToOne = new PortType(
			PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_DATA,
			PortType.RECEIVE_EXPLICIT, PortType.CONNECTION_MANY_TO_ONE);

	private static PortType portTypeManyToOneUpcalls = new PortType(
			PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT_IBIS,
			PortType.RECEIVE_AUTO_UPCALLS, PortType.CONNECTION_MANY_TO_ONE);
     */	
    // End -- J


    private static IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.ELECTIONS_STRICT, IbisCapabilities.CLOSED_WORLD);


    /** * Send & ReceivePorts to/from all particpipants *************** */

    private static int NR_PORTS = 0;

    private static SendPort[] sps = null;
    private static ReceivePort[] rps = null;

//  private static ReceivePort rpReduce = null;
//  private static SendPort spReduce = null;

    private static final String COMM_ID = "px_comm";

    // Experimental -- J.

    private static class ReduceToAllUpcallHandler implements MessageUpcall {
        public void upcall(ReadMessage rm) throws IOException, ClassNotFoundException {
        	handleIncomingExchange(rm);
        } 
    }

    private static double [][] exchange;
    private static boolean [] exchangeSet;
    
    // End -- J

    /** * GENERAL 'PARALLEL WORLD' INFORMATION ************************ */

    private static Ibis ibis = null;

    private static IbisIdentifier[] world = null;

    private static int nrCPUs = -1;

    private static int logCPUs = -1;

    private static int maxCPUs = -1;

    private static int myCPU = -1;

    private static int allreduce = ALLREDUCE_TREE;
    
    private static boolean initialized = false;

    private static long timeBarrierSBT;
    private static long countBarrierSBT;

    private static long timeReduceValueToRoot0FT;
    private static long countReduceValueToRoot0FT;

    private static long timeReduceArrayToRoot0FT;
    private static long countReduceArrayToRoot0FT;
    private static long dataInReduceArrayToRoot0FT;
    private static long dataOutReduceArrayToRoot0FT;

    private static long timeReduceArrayToAll0FT;
    private static long countReduceArrayToAll0FT;
    private static long dataInReduceArrayToAll0FT;
    private static long dataOutReduceArrayToAll0FT;

    private static long timeScatter0FT;
    private static long countScatter0FT;
    private static long dataInScatter0FT;
    private static long dataOutScatter0FT;

    private static long timeGather0FT;
    private static long countGather0FT;
    private static long dataInGather0FT;
    private static long dataOutGather0FT;

    private static long timeBroadcastSBT;
    private static long countBroadcastSBT;
    private static long dataInBroadcastSBT;
    private static long dataOutBroadcastSBT;

    private static long timeBroadcastValue;
    private static long countBroadcastValue;

    private static long timeBorderExchange;
    private static long countBorderExchange;
    private static long dataInBorderExchange;
    private static long dataOutBorderExchange;

    
    /** * Public Methods ********************************************** */

    public static void initParallelSystem(String name, String size)
    throws Exception {
        
        String tmp = System.getProperty("PxSystem.allreduce");
        
        if (tmp != null && tmp.length() > 0) { 
            allreduce = Integer.parseInt(tmp);
        
            if (allreduce < 0 && allreduce > ALLREDUCE_MAX) { 
                throw new Exception("Illegal allreduce implementation selected");
            }
        }
        
        tmp = System.getProperty("PxSystem.allreduce.short");
        
        if (tmp != null && tmp.length() > 0) { 
            ALLREDUCE_SHORT_MSG = Integer.parseInt(tmp);
        
            if (ALLREDUCE_SHORT_MSG < 0) { 
                throw new Exception("Illegal allreduce short message size selected");
            }
        }
        
        Properties props = new Properties();
        props.setProperty("ibis.pool.name", name);
        props.setProperty("ibis.pool.size", size);
        
        String internalImpl = System.getProperty("PxSystem.impl");
        
        if (internalImpl != null) { 
            
            System.out.println("PxSystem using implemementation: " + internalImpl);
            
            props.setProperty("ibis.implementation", internalImpl);
        }
        
        // Create Ibis & obtain parallel environment parameters (local)

        // ibis = IbisFactory.createIbis(ibisCapabilities, null, portType);
        ibis = IbisFactory.createIbis(ibisCapabilities, props, true, null,
                portType); // portTypeOneToMany, portTypeManyToOne, portTypeManyToOneUpcalls);
        nrCPUs = ibis.registry().getPoolSize();
        myCPU = (int) ibis.registry().getSequenceNumber("counter");
        logCPUs = (int) (Math.log((double) nrCPUs) / Math.log(2.0));
        maxCPUs = (int) Math.pow(2, logCPUs);

        if (maxCPUs < nrCPUs) {
            logCPUs++;
            maxCPUs *= 2;
        }

        // Let each node elect itself as the Ibis with 'myCPU' as rank.
        // Then, obtain Ibis identifiers for all CPUs.

        IbisIdentifier me = ibis.registry().elect(Integer.toString(myCPU));
        world = new IbisIdentifier[nrCPUs];

        for (int i = 0; i < nrCPUs; i++) {
            String rank = Long.toString(i);
            world[i] = ibis.registry().getElectionResult(rank);
        }

        // Hack -- J
        exchange = new double[nrCPUs][];
        exchangeSet = new boolean[nrCPUs];
        
        // Initialize Send/ReceivePorts to/from all participants
        NR_PORTS = nrCPUs;
        sps = new SendPort[NR_PORTS];
        rps = new ReceivePort[NR_PORTS];
        
        // Added -- J.
        //
        // Init all send and receive ports here. This will give 
        // us an all-to-all setup which is more than we need and 
        // doesn't scale. However, since the application does not 
        // seem to scale anyway we don't really care about this. 

        for (int i=0;i<nrCPUs;i++) { 
            if (i != myCPU) { 
                rps[i] = ibis.createReceivePort(portType, COMM_ID + i);
                rps[i].enableConnections();
            }
        }

        for (int i=0;i<nrCPUs;i++) { 
            
            if (i != myCPU) {
            
                System.out.println("Connecting to " + i);
                
                if (i > myCPU) {

                    System.out.println("Need to wait");
                    
                    while (rps[i].connectedTo().length == 0) { 
                        
                        System.out.println("Waiting for connection from " + i);
                        
                        try { 
                            Thread.sleep(500);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        
                    }
                }
                
                System.out.println("Really connecting to " + i);

                
                sps[i] = ibis.createSendPort(portType);
                sps[i].connect(world[i], COMM_ID + myCPU);                    
            }
        }

        initialized = true;

        if (myCPU == 0) { 

            String version = "UNKNOWN";

            switch (allreduce) {
            case ALLREDUCE_FLAT:
                version = "FLAT";
                break;
            case ALLREDUCE_RING:
                version = "RING";
                break;
            case ALLREDUCE_TREE:
                version = "TREE";
                break;
            case ALLREDUCE_MPICH:
                version = "MPICH";
                break;            
            }

            System.out.println("Selected " + version + " allreduce");
        }
    }

    private static double getThroughput(long data, long nanos) { 

        double Mbits = (data * 8.0) / 1000000.0;
        double sec = nanos / (1000.0 * 1000.0 * 1000.0);

        return (Mbits / sec);
    }

    public static void printStatistics() {

        long totalTime = timeBarrierSBT + timeReduceValueToRoot0FT
        + timeReduceArrayToRoot0FT + timeReduceArrayToAll0FT
        + timeScatter0FT + timeGather0FT + timeBroadcastSBT
        + timeBroadcastValue + timeBorderExchange;

        long totalCount = countBarrierSBT + countReduceValueToRoot0FT
        + countReduceArrayToRoot0FT + countReduceArrayToAll0FT
        + countScatter0FT + countGather0FT + countBroadcastSBT
        + countBroadcastValue + countBorderExchange;

        System.out.printf("Total communication time %.2f usec, count %d\n", (totalTime / 1000.0), totalCount);
        System.out.printf("            barrier time %.2f usec, count %d\n", (timeBarrierSBT / 1000.0), countBarrierSBT);
        System.out.printf("     broadcastValue time %.2f usec, count %d\n", (timeBroadcastValue / 1000.0), countBroadcastValue);
        System.out.printf("          reduceV2R time %.2f usec, count %d\n", (timeReduceValueToRoot0FT / 1000.0), countReduceValueToRoot0FT);
        System.out.printf("          reduceA2R time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeReduceArrayToRoot0FT / 1000.0), 
                countReduceArrayToRoot0FT, 
                dataInReduceArrayToRoot0FT, 
                dataOutReduceArrayToRoot0FT, 
                getThroughput(dataInReduceArrayToRoot0FT + dataOutReduceArrayToRoot0FT, timeReduceArrayToRoot0FT));

        System.out.printf("          reduceA2A time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeReduceArrayToAll0FT / 1000.0), 
                countReduceArrayToAll0FT, 
                dataInReduceArrayToAll0FT, 
                dataOutReduceArrayToAll0FT, 
                getThroughput(dataInReduceArrayToAll0FT + dataOutReduceArrayToAll0FT, timeReduceArrayToAll0FT));

        System.out.printf("            scatter time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeScatter0FT / 1000.0), 
                countScatter0FT, 
                dataInScatter0FT, 
                dataOutScatter0FT, 
                getThroughput(dataInScatter0FT + dataOutScatter0FT, timeScatter0FT));

        System.out.printf("             gather time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeGather0FT / 1000.0), 
                countGather0FT, 
                dataInGather0FT,
                dataOutGather0FT, 
                getThroughput(dataInGather0FT + dataOutGather0FT, timeGather0FT));

        System.out.printf("       broadcastSBT time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeBroadcastSBT / 1000.0), 
                countBroadcastSBT, 
                dataInBroadcastSBT, 
                dataOutBroadcastSBT, 
                getThroughput(dataInBroadcastSBT + dataOutBroadcastSBT, timeBroadcastSBT));

        System.out.printf("     borderExchange time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeBorderExchange / 1000.0), 
                countBorderExchange, 
                dataInBorderExchange, 
                dataOutBorderExchange,
                getThroughput(dataInBorderExchange + dataOutBorderExchange, timeBorderExchange));

        timeBarrierSBT = 0;
        timeReduceValueToRoot0FT = 0;
        timeReduceArrayToRoot0FT = 0;
        timeReduceArrayToAll0FT = 0;
        timeScatter0FT = 0;
        timeGather0FT = 0;
        timeBroadcastSBT = 0;
        timeBroadcastValue = 0;
        timeBorderExchange = 0;

        countBarrierSBT = 0;
        countReduceValueToRoot0FT = 0;
        countReduceArrayToRoot0FT = 0;
        countReduceArrayToAll0FT = 0;
        countScatter0FT = 0;
        countGather0FT = 0;
        countBroadcastSBT = 0;
        countBroadcastValue = 0;
        countBorderExchange = 0;

        dataInReduceArrayToRoot0FT = 0;
        dataOutReduceArrayToRoot0FT = 0;

        dataInReduceArrayToAll0FT = 0;
        dataOutReduceArrayToAll0FT = 0;

        dataInScatter0FT = 0;
        dataOutScatter0FT = 0;

        dataInGather0FT = 0;
        dataOutGather0FT = 0;

        dataInBroadcastSBT = 0;
        dataOutBroadcastSBT = 0;

        dataInBorderExchange = 0;
        dataOutBorderExchange = 0;

    }

    public static void exitParallelSystem() throws Exception {
        for (int i = 0; i < NR_PORTS; i++) {
            if (sps[i] != null)
                sps[i].close();
            if (rps[i] != null)
                rps[i].close();
        }
        ibis.end();
        initialized = false;
    }

    public static boolean initialized() {
        return initialized;
    }

    public static int myCPU() {
        return myCPU;
    }

    public static int nrCPUs() {
        return nrCPUs;
    }

    public static void barrierSBT() throws Exception {
        // Added -- J
        long start = System.nanoTime();

        int mask = 1;
        for (int i = 0; i < logCPUs; i++) {
            int partner = myCPU ^ mask;
            if ((myCPU % mask == 0) && (partner < nrCPUs)) {
                if (myCPU > partner) {
//                  if (sps[partner] == null) {
//                  sps[partner] = ibis.createSendPort(portType);
//                  sps[partner].connect(world[partner], COMM_ID + myCPU);
//                  }
                    WriteMessage w = sps[partner].newMessage();
                    w.finish();
                } else {
//                  if (rps[partner] == null) {
//                  rps[partner] = ibis.createReceivePort(portType, COMM_ID
//                  + partner);
//                  rps[partner].enableConnections();
//                  }
                    ReadMessage r = rps[partner].receive();
                    r.finish();
                }
            }
            mask <<= 1;
        }
        mask = 1 << (logCPUs - 1);
        for (int i = 0; i < logCPUs; i++) {
            int partner = myCPU ^ mask;
            if ((myCPU % mask == 0) && (partner < nrCPUs)) {
                if (myCPU < partner) {
//                  if (sps[partner] == null) {
//                  sps[partner] = ibis.createSendPort(portType);
//                  sps[partner].connect(world[partner], COMM_ID + myCPU);
//                  }
                    WriteMessage w = sps[partner].newMessage();
                    w.finish();
                } else {
//                  if (rps[partner] == null) {
//                  rps[partner] = ibis.createReceivePort(portType, COMM_ID
//                  + partner);
//                  rps[partner].enableConnections();
//                  }
                    ReadMessage r = rps[partner].receive();
                    r.finish();
                }
            }
            mask >>= 1;
        }

        // Added -- J
        timeBarrierSBT += System.nanoTime() - start;
        countBarrierSBT++;
    }

    public static double reduceValueToRootOFT(double val, CxRedOp op)
    throws Exception {
        // Added -- J
        long start = System.nanoTime();
        
        if (nrCPUs == 1) { 
            return val;
        }
        
        double result = val;

        if (myCPU == 0) {
            for (int partner = 1; partner < nrCPUs; partner++) {
//              if (rps[partner] == null) {
//              rps[partner] = ibis.createReceivePort(portType, COMM_ID
//              + partner);
//              rps[partner].enableConnections();
//              }
                ReadMessage r = rps[partner].receive();
                double recvVal = r.readDouble();
                r.finish();
                result = (Double) op.doIt(result, recvVal);
            }
        } else {
//          if (sps[0] == null) {
//          sps[0] = ibis.createSendPort(portType);
//          sps[0].connect(world[0], COMM_ID + myCPU);
//          }
            WriteMessage w = sps[0].newMessage();
            w.writeDouble(val);
            w.finish();
        }

        // Added -- J
        timeReduceValueToRoot0FT += System.nanoTime() - start;
        countReduceValueToRoot0FT++;

        return result;
    }

    public static double[] reduceArrayToRootOFT(double[] a, CxRedOpArray op)
    throws Exception {
        // Added -- J
        long start = System.nanoTime();

        if (nrCPUs == 1) {
            return a;
        }
        
        if (myCPU == 0) {
            double[] recvArray = new double[a.length];

            for (int partner = 1; partner < nrCPUs; partner++) {
                //               if (rps[partner] == null) {
                //                   rps[partner] = ibis.createReceivePort(portType, COMM_ID
                //                           + partner);
                //                   rps[partner].enableConnections();
                //               }
                ReadMessage r = rps[partner].receive();
                r.readArray(recvArray);
                r.finish();
                op.doIt(a, recvArray);

     //           dataInReduceArrayToRoot0FT += a.length * 8; 
            }
        } else {
            //           if (sps[0] == null) {
            //              sps[0] = ibis.createSendPort(portType);
            //              sps[0].connect(world[0], COMM_ID + myCPU);
            //          }
            WriteMessage w = sps[0].newMessage();
            w.writeArray(a);
            w.finish();

     //       dataOutReduceArrayToRoot0FT += a.length * 8; 
        }

        // Added -- J
        timeReduceArrayToRoot0FT += System.nanoTime() - start;
        countReduceArrayToRoot0FT++;

        return a;
    }
    
    
    public static double[] reduceArrayToAllOFT_Ring(double [] a, CxRedOpArray op) throws Exception {
        // Added -- J

        if (nrCPUs == 1) { 
            return a;
        }

        long start = System.nanoTime();

        // Start by dividing the array into 'nrCPUs' partitions. This is a bit tricky, since the array 
        // size my not be dividable by nrCPUs. We ensure here that the difference in size is at most 1.
        // We also remeber the start indexes of each partition.
        final int [] sizes = new int[nrCPUs];
        final int [] index = new int[nrCPUs];

        final int size = a.length / nrCPUs; 
        final int left = a.length % nrCPUs; 

        //	System.out.println("Data size: " + a.length + " div: " + size + " mod: " + left);

        for (int i=0;i<nrCPUs;i++) { 

            if (left > 0) {
                if (i < left) { 
                    sizes[i] = size + 1;
                    index[i] = size * i + i;
                } else { 
                    sizes[i] = size;
                    index[i] = size * i + left;
                }
            } else { 
                sizes[i] = size;
                index[i] = size * i;
            }

            //		System.out.println(i + " index: " + index[i] + " size: " + sizes[i]);
        }

        // Create a temporary array for receiving data
        final double [] tmp = new double[sizes[0]];  

        final int sendPartner = (myCPU + 1) % nrCPUs;
        final int receivePartner = (myCPU + nrCPUs - 1) % nrCPUs;

        //	System.out.println("Send partner: " + sendPartner);
        //	System.out.println("Receive partner: " + receivePartner);

//      if (rps[receivePartner] == null) {
//      rps[receivePartner] = ibis.createReceivePort(portType, COMM_ID
//      + receivePartner);
//      rps[receivePartner].enableConnections();
//      }

        final ReceivePort rp = rps[receivePartner];

//      if (sps[sendPartner] == null) {
//      sps[sendPartner] = ibis.createSendPort(portType);
//      sps[sendPartner].connect(world[sendPartner], COMM_ID + myCPU);
//      }

        final SendPort sp = sps[sendPartner];

        /*
        SendPortIdentifier [] s = rp.connectedTo();

        while (s.length == 0) { 
            //System.out.println("EEP: no connections to RP yet!");

            try { 
                Thread.sleep(10);
            } catch (Exception e) {
                // ignore
            }

            s = rp.connectedTo();			
        }
*/
        
        // Determine the starting partition for this node.
        int sendPartition = myCPU;
        int receivePartition = (myCPU + nrCPUs - 1) % nrCPUs;

        //System.out.println("Send partition: " + sendPartition);
        //System.out.println("Receive partition: " + receivePartition);

        // Perform nrCPUs-1 rounds of the algorithm
        for (int i=0;i<nrCPUs-1;i++) { 

            //		System.out.println("Iteration: " + i);

            if ((myCPU & 1) == 0) { 

                WriteMessage wm = sp.newMessage();
                wm.writeArray(a, index[sendPartition], sizes[sendPartition]);
                wm.finish();

         //       dataOutReduceArrayToAll0FT += sizes[sendPartition] * 8;

                ReadMessage rm = rp.receive();
                rm.readArray(tmp, 0, sizes[receivePartition]);
                rm.finish();

           //     dataInReduceArrayToAll0FT += sizes[receivePartition] * 8;

            } else { 

                ReadMessage rm = rp.receive();
                rm.readArray(tmp, 0, sizes[receivePartition]);
                rm.finish();

          //      dataInReduceArrayToAll0FT += sizes[receivePartition] * 8;

                WriteMessage wm = sp.newMessage();
                wm.writeArray(a, index[sendPartition], sizes[sendPartition]);
                wm.finish();

           //     dataOutReduceArrayToAll0FT += sizes[sendPartition] * 8;
            }

            op.doItRange(a, tmp, index[receivePartition], sizes[receivePartition]);	

            // Shift the active partition by one.
            sendPartition = receivePartition;
            receivePartition = (receivePartition + nrCPUs - 1) % nrCPUs;

            //	System.out.println("Send partition: " + sendPartition);
            //	System.out.println("Receive partition: " + receivePartition);

        }

        // The 'sendpartition' part of the data now contains the final result. We should now continue for
        // another nrCPUs-1 steps to 'allgather' the result to all machines.
        for (int i=0;i<nrCPUs-1;i++) { 

            if ((myCPU & 1) == 0) { 

                WriteMessage wm = sp.newMessage();
                wm.writeArray(a, index[sendPartition], sizes[sendPartition]);
                wm.finish();

         //       dataOutReduceArrayToAll0FT += sizes[sendPartition] * 8;

                ReadMessage rm = rp.receive();
                rm.readArray(a, index[receivePartition], sizes[receivePartition]);
                rm.finish();

         //       dataInReduceArrayToAll0FT += sizes[receivePartition] * 8;

            } else { 

                ReadMessage rm = rp.receive();
                rm.readArray(a, index[receivePartition], sizes[receivePartition]);
                rm.finish();

         //       dataInReduceArrayToAll0FT += sizes[receivePartition] * 8;

                WriteMessage wm = sp.newMessage();
                wm.writeArray(a, index[sendPartition], sizes[sendPartition]);
                wm.finish();

         //       dataOutReduceArrayToAll0FT += sizes[sendPartition] * 8;

            }

            // Shift the active partition by one.
            sendPartition = receivePartition;
            receivePartition = (receivePartition + nrCPUs - 1) % nrCPUs;
        }

        // Added -- J
        timeReduceArrayToAll0FT += System.nanoTime() - start;
        countReduceArrayToAll0FT++;

        return a;
    }

    @SuppressWarnings("unchecked")
	public static double[] reduceArrayToAllOFT_BinomialSimple(double[] a, 
            CxRedOpArray op) throws Exception {

        // Added -- J
        long start = System.nanoTime();

        final double [] tmp = new double[a.length];
        
        int mask = 1;

        for (int i=0; i<logCPUs; i++) {
            
            exchange(myCPU ^ mask, a, tmp);
            
            /*
            final int partner = myCPU ^ mask;

            if (myCPU > partner) {
                WriteMessage w = sps[partner].newMessage();
                w.writeArray(a);
                w.finish();
            
                ReadMessage r = rps[partner].receive();
                r.readArray(tmp);
                r.finish();
                
            } else {
                ReadMessage r = rps[partner].receive();
                r.readArray(tmp);
                r.finish();
            
                WriteMessage w = sps[partner].newMessage();
                w.writeArray(a);
                w.finish();
            }
            */
            
            op.doIt(a, tmp);
            
            mask <<= 1;
        }

        // Added -- J
     
        timeReduceArrayToAll0FT += System.nanoTime() - start;
     //   dataInReduceArrayToAll0FT += a.length * 8 * logCPUs;
     //   dataOutReduceArrayToAll0FT += a.length * 8 * logCPUs;
        countReduceArrayToAll0FT++;
     
        return a;
    }
    
    private static void handleIncomingExchange(ReadMessage m) 
    	throws IOException { 
    	
    	int target = m.readInt();
    	
    	// TODO: try polling here!
    	synchronized (exchange) {
    		while (exchange[target] == null) { 
    			try {
    				exchange.wait();
    			} catch (Exception e) {
					// TODO: handle exception
				}
    		}
    		
    		m.readArray(exchange[target]);
    		exchangeSet[target] = true;
    	}
    	
    }
    	
    private static void exchange(int target, double [] out, double [] in) 
    	throws IOException { 
    	
    	synchronized (exchange) {
    		exchange[target] = in;
    		exchange.notifyAll();
    	}
    	
        WriteMessage w = sps[target].newMessage();
        w.writeInt(myCPU);
        w.writeArray(out);
        w.finish();

        // TODO: try polling here!
        synchronized (exchange) {        	
        	while (!exchangeSet[target]) {
        		try {
        			exchange.wait();
        		} catch (Exception e) {
					// ignored
				}
        	}
    	
        	exchangeSet[target] = false;
        	exchange[target] = null;
        	
        	// side effect: out has been filled
        }
    }
    
    public static double[] reduceArrayToAllOFT_RecursiveHalving(double[] a, 
            CxRedOpArray op) throws Exception {
 
        // NOTE: this is a port of the MPICH allreduce algorithm, which uses 
        // a recursive halving approach. We assume that the reduce operation 
        // in commutative.
        
        // Added -- J
        long start = System.nanoTime();

        // First allocate a temporary buffer.
        final double [] tmp = new double[a.length];
        
        // Next, we need to find nearest power-of-two less than or equal to 
        // the number of participating machines.

        final int rank = myCPU;
        final int comm_size = nrCPUs;
        final int count = a.length;
        
        int pof2 = 1;
        while (pof2 <= comm_size) pof2 <<= 1;
        pof2 >>=1;
        
        int rem = comm_size - pof2;
        int newrank;
        
        /* In the non-power-of-two case, all even-numbered
           processes of rank < 2*rem send their data to
           (rank+1). These even-numbered processes no longer
           participate in the algorithm until the very end. The
           remaining processes form a nice power-of-two. */
        
        if (rank < 2*rem) {
            
          //  System.out.println("ALLREDUCE: Adjust processes (PRE)!");
            
            if (rank % 2 == 0) { /* even */
                WriteMessage w = sps[rank+1].newMessage();
                w.writeArray(a);
                w.finish();
            
                /* temporarily set the rank to -1 so that this
                   process does not pariticipate in recursive
                   doubling */
                newrank = -1;
                
            } else { /* odd */
                
                ReadMessage r = rps[rank-1].receive();
                r.readArray(tmp);
                r.finish();
            
                /* do the reduction on received data. since the
                   ordering is right, it doesn't matter whether
                   the operation is commutative or not. */
            
                op.doIt(a, tmp);
                
                /* change the rank */
                newrank = rank / 2;
            }
            
        } else { /* rank >= 2*rem */
        
            newrank = rank - rem;
        }
                
        // We will now perform a reduce using recursive doubling when there is 
        // little data, or recursive halving followed by an allgather when there
        // is enough data. Some machines will be left out here if the number of 
        // machines is not a power of two. 

        if (newrank != -1) {
            
            if (((a.length * 8) <= ALLREDUCE_SHORT_MSG) || (count < pof2)) { 
                /* use recursive doubling */                

            //    System.out.println("ALLREDUCE: Using doubling");

                
                int mask = 0x1;
                
                while (mask < pof2) {
                    int newdst = newrank ^ mask;
                    
                    /* find real rank of dest */
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;
                    
                    /* Send the most current data, which is in a. Receive
                       into tmp */
                    
                    if (myCPU > dst) {
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a);
                        w.finish();
                    
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp);
                        r.finish();
                        
                    } else {
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp);
                        r.finish();
                    
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a);
                        w.finish();
                    }
                    
                    /* tmp contains data received in this step.
                       a contains data accumulated so far */
                    
                    op.doIt(a, tmp);            
                    
                    mask <<= 1;
                }
            } else {
                /* do a reduce-scatter followed by allgather */
                     
                /* for the reduce-scatter, calculate the count that
                   each process receives and the displacement within
                   the buffer */
                     
         //       System.out.println("ALLREDUCE: Using halving");
                
                final int [] cnts = new int [pof2];
                final int [] disps = new int [pof2];
                
                for (int i=0; i<(pof2-1); i++) {
                    cnts[i] = count/pof2;
                }
                
                cnts[pof2-1] = count - (count/pof2)*(pof2-1);      

                disps[0] = 0;
                
                for (int i=1; i<pof2; i++) {
                    disps[i] = disps[i-1] + cnts[i-1];
                }
                
                int mask = 0x1;
                int send_idx = 0; 
                int recv_idx = 0;
                int last_idx = pof2;
                
                while (mask < pof2) {
                    
                    int newdst = newrank ^ mask;
                    /* find real rank of dest */
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    int send_cnt = 0;
                    int recv_cnt = 0;
                    
                    if (newrank < newdst) {
                        send_idx = recv_idx + pof2/(mask*2);
                    
                        for (int i=send_idx; i<last_idx; i++) { 
                            send_cnt += cnts[i];
                        } 
                        
                        for (int i=recv_idx; i<send_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    } else {
                        recv_idx = send_idx + pof2/(mask*2);
                        
                        for (int i=send_idx; i<recv_idx; i++) { 
                            send_cnt += cnts[i];
                        }
                        for (int i=recv_idx; i<last_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    }

                    /* Send data from a. Receive into tmp */
                    if (myCPU > dst) {
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a, disps[send_idx], send_cnt);
                        w.finish();
                    
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp, disps[recv_idx], recv_cnt);
                        r.finish();                        
                    } else {                        
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp, disps[recv_idx], recv_cnt);
                        r.finish();
                        
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a, disps[send_idx], send_cnt);
                        w.finish();                    
                    }
                    
                    /* tmp contains data received in this step.
                       a contains data accumulated so far */
                    op.doItRange(a, tmp, disps[recv_idx], recv_cnt);            

                    /* update send_idx for next iteration */
                    send_idx = recv_idx;
                    mask <<= 1;

                    /* update last_idx, but not in last iteration
                       because the value is needed in the allgather
                       step below. */
                    
                    if (mask < pof2) { 
                        last_idx = recv_idx + pof2/mask;
                    } 
                }

                /* now do the allgather */
                mask >>= 1;
                    
                while (mask > 0) {
                    int newdst = newrank ^ mask;

                    /* find real rank of dest */
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    int send_cnt = 0; 
                    int recv_cnt = 0;

                    if (newrank < newdst) {
                        /* update last_idx except on first iteration */
                        if (mask != pof2/2) {
                            last_idx = last_idx + pof2/(mask*2);
                        }

                        recv_idx = send_idx + pof2/(mask*2);

                        for (int i=send_idx; i<recv_idx; i++) { 
                            send_cnt += cnts[i];
                        }

                        for (int i=recv_idx; i<last_idx; i++) { 
                            recv_cnt += cnts[i];
                        } 
                    } else {
                        recv_idx = send_idx - pof2/(mask*2);

                        for (int i=send_idx; i<last_idx; i++) { 
                            send_cnt += cnts[i];
                        }

                        for (int i=recv_idx; i<send_idx; i++) { 
                            recv_cnt += cnts[i];
                        }
                    }

                    /* Send data from a. Receive into tmp */
                    if (myCPU > dst) {
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a, disps[send_idx], send_cnt);
                        w.finish();
                    
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp, disps[recv_idx], recv_cnt);
                        r.finish();                        
                    } else {                        
                        ReadMessage r = rps[dst].receive();
                        r.readArray(tmp, disps[recv_idx], recv_cnt);
                        r.finish();
                        
                        WriteMessage w = sps[dst].newMessage();
                        w.writeArray(a, disps[send_idx], send_cnt);
                        w.finish();                    
                    }
                    
                    if (newrank > newdst) { 
                        send_idx = recv_idx;
                    }

                    mask >>= 1;
                }
            }
        }

        /* In the non-power-of-two case, all odd-numbered
           processes of rank < 2*rem send the result to
          (rank-1), the ranks who didn't participate above. */
        
        if (rank < 2*rem) {
            
     //       System.out.println("ALLREDUCE: Adjust processes (POST)!");

            
            if (rank % 2 == 1) { /* odd */
                WriteMessage w = sps[rank-1].newMessage();
                w.writeArray(a);
                w.finish();
            } else {  
                ReadMessage r = rps[rank+1].receive();
                r.readArray(a);
                r.finish();
            }
        }

        // Added -- J
        timeReduceArrayToAll0FT += System.nanoTime() - start;
     //   dataInReduceArrayToAll0FT += a.length * 8 * logCPUs;
     //   dataOutReduceArrayToAll0FT += a.length * 8 * logCPUs;
        countReduceArrayToAll0FT++;
     
        return a;
    }
    
    public static double[] reduceArrayToAllOFT_Flat_Orig(double[] a, CxRedOpArray op)
    throws Exception {
//      Added -- J
        long start = System.nanoTime();

        if (myCPU == 0) {
            double[] recvArray = new double[a.length];
            for (int partner = 1; partner < nrCPUs; partner++) {
//              if (rps[partner] == null) {
//              rps[partner] = ibis.createReceivePort(portType, COMM_ID
//              + partner);
                //                   rps[partner].enableConnections();
                //              }
                ReadMessage r = rps[partner].receive();
                r.readArray(recvArray);
                r.finish();

       //         dataInReduceArrayToAll0FT += a.length * 8;

                op.doIt(a, recvArray);
            }
            for (int partner = 1; partner < nrCPUs; partner++) {
//              if (sps[partner] == null) {
//              sps[partner] = ibis.createSendPort(portType);
//              sps[partner].connect(world[partner], COMM_ID + 0);
                //               }
                WriteMessage w = sps[partner].newMessage();
                w.writeArray(a);
                w.finish();

      //          dataOutReduceArrayToAll0FT += a.length * 8;
            }
        } else {
//          if (sps[0] == null) {
//          sps[0] = ibis.createSendPort(portType);
//          sps[0].connect(world[0], COMM_ID + myCPU);
//          }
            WriteMessage w = sps[0].newMessage();
            w.writeArray(a);
            w.finish();

      //      dataOutReduceArrayToAll0FT += a.length * 8;

//          if (rps[0] == null) {
//          rps[0] = ibis.createReceivePort(portType, COMM_ID + 0);
//          rps[0].enableConnections();
//          }
            ReadMessage r = rps[0].receive();
            r.readArray(a);
            r.finish();

      //      dataInReduceArrayToAll0FT += a.length * 8;

        }

        // Added -- J
        timeReduceArrayToAll0FT += System.nanoTime() - start;
        countReduceArrayToAll0FT++;

        return a;
    }

    public static double[] reduceArrayToAllOFT(double[] a, CxRedOpArray op)
        throws Exception {
       
        if (nrCPUs == 1) { 
            return a;
        }
        
        switch (allreduce) {
        case ALLREDUCE_MPICH:
            return reduceArrayToAllOFT_RecursiveHalving(a, op);        
        case ALLREDUCE_TREE:
            return reduceArrayToAllOFT_BinomialSimple(a, op);
        case ALLREDUCE_RING:
            return reduceArrayToAllOFT_Ring(a, op);
        case ALLREDUCE_FLAT: 
            return reduceArrayToAllOFT_Flat_Orig(a, op);
        default: 
            System.err.println("WARNING: Unknown allreduce implementation " +
                        "selected, using default!");
            return reduceArrayToAllOFT_BinomialSimple(a, op);
        }
    }	

    public static void scatterOFT(CxArray2d a) throws Exception {
        // Added -- J
        long start = System.nanoTime();

        if (nrCPUs == 1) { 
            // On 1 CPU we simply create an alias to the same data
            a.setPartialData(a.getWidth(), a.getHeight(), a.getDataReadWrite(), 
                    CxArray2d.VALID, CxArray2d.PARTIAL);
            return;
        }
        
        if (a instanceof CxArray2dDoubles) {
            doScatterOFT((CxArray2dDoubles) a);
        } else {
            System.out.println("ERROR: SCATTER OFT NOT IMPLEMENTED YET!!!");
        }
        a.setLocalState(CxArray2d.VALID);
        a.setDistType(CxArray2d.PARTIAL);

        // Added -- J
        timeScatter0FT += System.nanoTime() - start;
        countScatter0FT++;
    }

    public static void gatherOFT(CxArray2d a) throws Exception {
        // Added -- J
        long start = System.nanoTime();

        // NOTE: the single CPU case is handled in doGather -- J
        
        if (a instanceof CxArray2dDoubles) {
            doGatherOFT((CxArray2dDoubles) a);
        } else {
            System.out.println("ERROR: GATHER OFT NOT IMPLEMENTED YET!!!");
        }
        a.setGlobalState(CxArray2d.VALID);

        // Added -- J
        timeGather0FT += System.nanoTime() - start;
        countGather0FT++;
    }

    public static void broadcastSBT(CxArray2d a) throws Exception {
        // Added -- J
        long start = System.nanoTime();

        if (nrCPUs == 1) { 
            return;
        }
        
        if (a instanceof CxArray2dDoubles) {
            doBroadcastSBT((CxArray2dDoubles) a);
        } else {
            System.out.println("ERROR: BROADCAST SBT NOT IMPLEMENTED YET!!!");
        }
        a.setLocalState(CxArray2d.VALID);
        a.setDistType(CxArray2d.FULL);

        // Added -- J
        timeBroadcastSBT += System.nanoTime() - start;
        countBroadcastSBT++;
    }

    public static void borderExchange(double[] a, int width, int height,
            int off, int stride, int ySize) throws Exception {	
        
        if (nrCPUs == 1) {
            return;
        }
        
        borderExchange_Jason(a, width, height, off, stride, ySize);		
    }

    public static void borderExchange_Jason(double[] a, int width, int height,
            int off, int stride, int ySize) throws Exception {

        // Added -- J
        long start = System.nanoTime();

        // Border exchange in vertical direction (top <---> bottom)
        int prevCPU = myCPU - 1;
        int nextCPU = myCPU + 1;

        int xSize = width + stride;

        // Do some necessary initialisations. Note that these are only performed
        // the first time the operation is used. 
        /*
        if (prevCPU >= 0 && rps[prevCPU] == null) {
            rps[prevCPU] = ibis.createReceivePort(portType, COMM_ID + prevCPU);
            rps[prevCPU].enableConnections();
        }

        if (nextCPU < PxSystem.nrCPUs() && rps[nextCPU] == null) {
            rps[nextCPU] = ibis.createReceivePort(portType, COMM_ID + nextCPU);
            rps[nextCPU].enableConnections();
        }

        if (prevCPU >= 0 && sps[prevCPU] == null) {
            sps[prevCPU] = ibis.createSendPort(portType);
            sps[prevCPU].connect(world[prevCPU], COMM_ID + myCPU);
        }

        if (nextCPU < PxSystem.nrCPUs() && sps[nextCPU] == null) {
            sps[nextCPU] = ibis.createSendPort(portType);
            sps[nextCPU].connect(world[nextCPU], COMM_ID + myCPU);
        }*/

//      System.out.println("Border exchange: " + xSize + "x" + ySize + " (" + (xSize*ySize) + ")");

        if ((myCPU & 1) == 0) { 

            if (prevCPU >= 0) {
                WriteMessage w = sps[prevCPU].newMessage();
                w.writeArray(a, off - stride / 2, xSize * ySize);
                w.finish();

       //         dataOutBorderExchange += xSize * ySize * 8;
            }

            if (nextCPU < PxSystem.nrCPUs()) {
                WriteMessage w = sps[nextCPU].newMessage();
                w.writeArray(a, off - stride / 2 + (height - ySize) * xSize, xSize * ySize);
                w.finish();

         //       dataOutBorderExchange += xSize * ySize * 8;             
            }

            if (prevCPU >= 0) {
                ReadMessage r = rps[prevCPU].receive();
                r.readArray(a, 0, xSize * ySize);
                r.finish();				

        //        dataInBorderExchange += xSize * ySize * 8;
            }

            if (nextCPU < PxSystem.nrCPUs()) {
                ReadMessage r = rps[nextCPU].receive();
                r.readArray(a, off - stride / 2 + height * xSize, xSize * ySize);
                r.finish();

         //       dataInBorderExchange += xSize * ySize * 8;        
            }

        } else { 

            if (nextCPU < PxSystem.nrCPUs()) {
                ReadMessage r = rps[nextCPU].receive();
                r.readArray(a, off - stride / 2 + height * xSize, xSize * ySize);
                r.finish();

        //        dataInBorderExchange += xSize * ySize * 8;
            }

            if (prevCPU >= 0) {
                ReadMessage r = rps[prevCPU].receive();
                r.readArray(a, 0, xSize * ySize);
                r.finish();

          //      dataInBorderExchange += xSize * ySize * 8;
            }

            if (nextCPU < PxSystem.nrCPUs()) {
                WriteMessage w = sps[nextCPU].newMessage();
                w.writeArray(a, off - stride / 2 + (height - ySize) * xSize, xSize * ySize);
                w.finish();

        //        dataOutBorderExchange += xSize * ySize * 8;
            }

            if (prevCPU >= 0) {
                WriteMessage w = sps[prevCPU].newMessage();
                w.writeArray(a, off - stride / 2, xSize * ySize);
                w.finish();                     

          //      dataOutBorderExchange += xSize * ySize * 8;
            }
        }

        // Added -- J
        timeBorderExchange += System.nanoTime() - start;
        countBorderExchange++;
    }

    public static void borderExchange_Orig(double[] a, int width, int height,
            int off, int stride, int ySize) throws Exception {
        // Added -- J
        long start = System.nanoTime();

        // Border exchange in vertical direction (top <---> bottom)
        int part1 = myCPU - 1;
        int part2 = myCPU + 1;
        int xSize = width + stride;

        // Send to first partner and receive from second partner

        if (part1 >= 0) {
            //if (sps[part1] == null) {
            //    sps[part1] = ibis.createSendPort(portType);
            //    sps[part1].connect(world[part1], COMM_ID + myCPU);
            // }
            WriteMessage w = sps[part1].newMessage();
            w.writeArray(a, off - stride / 2, xSize * ySize);
            w.finish();

        //    dataOutBorderExchange += xSize * ySize * 8;
        }

        if (part2 < PxSystem.nrCPUs()) {
            //if (rps[part2] == null) {
            //   rps[part2] = ibis.createReceivePort(portType, COMM_ID + part2);
            //    rps[part2].enableConnections();
            // }
            ReadMessage r = rps[part2].receive();
            r.readArray(a, off - stride / 2 + height * xSize, xSize * ySize);
            r.finish();

       //     dataInBorderExchange += xSize * ySize * 8;

            // Send to second partner and receive from first partner

//          if (sps[part2] == null) {
            //               sps[part2] = ibis.createSendPort(portType);
            //              sps[part2].connect(world[part2], COMM_ID + myCPU);
            //         }
            WriteMessage w = sps[part2].newMessage();
            w.writeArray(a, off - stride / 2 + (height - ySize) * xSize, xSize
                    * ySize);
            w.finish();

         //   dataOutBorderExchange += xSize * ySize * 8;

        }
        if (part1 >= 0) {
//          if (rps[part1] == null) {
//          rps[part1] = ibis.createReceivePort(portType, COMM_ID + part1);
            //               rps[part1].enableConnections();
            //          }
            ReadMessage r = rps[part1].receive();
            r.readArray(a, 0, xSize * ySize);
            r.finish();

         //   dataInBorderExchange += xSize * ySize * 8;
        }

        // Added -- J
        timeBorderExchange += System.nanoTime() - start;
        countBorderExchange++;
    }

    public static int getPartHeight(int height, int CPUnr) {
        int minLocalH = height / nrCPUs;
        int overflowH = height % nrCPUs;

        if (CPUnr < overflowH) {
            minLocalH++;
        }
        return minLocalH;
    }

    public static int getLclStartY(int height, int CPUnr) {
        int minLocalH = height / nrCPUs;
        int overflowH = height % nrCPUs;

        if (CPUnr < overflowH) {
            return (CPUnr * (minLocalH + 1));
        } else {
            return (CPUnr * minLocalH + overflowH);
        }
    }

    private static void doScatterOFT(CxArray2dDoubles a) throws Exception {
        // Here we assume CPU 0 (root) to have a full & valid structure
        // which is scattered to the partial structs of all nodes. East
        // and west borders are also communicated (not north and south).
        
        int globH = a.getHeight();
        int extent = a.getExtent();
        int pWidth = a.getWidth();
        int pHeight = getPartHeight(globH, myCPU);
        int bWidth = a.getBorderWidth();
        int bHeight = a.getBorderHeight();

        double[] pData = new double[(pWidth + bWidth * 2)
                                    * (pHeight + bHeight * 2) * extent];

        a.setPartialData(pWidth, pHeight, pData, CxArray2d.NONE, CxArray2d.NONE);

        int xSize = (pWidth + bWidth * 2) * extent;

        if (myCPU == 0) {
            for (int partner = 1; partner < nrCPUs; partner++) {
                int ySize = getPartHeight(globH, partner);
                int offset = xSize * (getLclStartY(globH, partner) + bHeight);
                //            if (sps[partner] == null) {
                //                sps[partner] = ibis.createSendPort(portType);
                //                sps[partner].connect(world[partner], COMM_ID + 0);
                //            }
                WriteMessage w = sps[partner].newMessage();
                w.writeArray(a.getDataReadOnly(), offset, xSize * ySize);
                w.finish();

                // Added-- J
         //       dataOutScatter0FT += 8 * xSize * ySize;               
            }

            int start = xSize * bHeight;

            System.arraycopy(a.getDataReadOnly(), start, pData, start, pData.length - 2
                    * start);
        } else {
            int ySize = getPartHeight(globH, myCPU);
            int offset = xSize * bHeight;
            //      if (rps[0] == null) {
            //         rps[0] = ibis.createReceivePort(portType, COMM_ID + 0);
            //         rps[0].enableConnections();
            //     }
            ReadMessage r = rps[0].receive();
            r.readArray(a.getPartialDataWriteOnly(), offset, xSize * ySize);
            r.finish();

            // Added-- J
        //    dataInScatter0FT += 8 * xSize * ySize;               
        }
    }

    private static void doGatherOFT(CxArray2dDoubles a) throws Exception {
        // Here we assume all nodes to have a full yet invalid global
        // structure and a valid partial structure, which is gathered
        // to the global structure of CPU 0; east and west borders are
        // also communicated (not north and south).

        if (nrCPUs == 1) {
            
            double [] data = a.getDataReadWrite();
            double [] pdata = a.getDataReadOnly();
            
            if (a.getDataReadWrite() == a.getDataReadOnly()) { 
                // Data is an alias, so we don't do anything
            } else { 
                System.arraycopy(pdata, 0, data, 0, pdata.length);
            }
            
            return;
        }
        
        int globH = a.getHeight();
        int extent = a.getExtent();
        int pWidth = a.getWidth();
        int bWidth = a.getBorderWidth();
        int bHeight = a.getBorderHeight();

        int xSize = (pWidth + bWidth * 2) * extent;

        if (myCPU == 0) {
            for (int partner = 1; partner < nrCPUs; partner++) {
                int ySize = getPartHeight(globH, partner);
                int offset = xSize * (getLclStartY(globH, partner) + bHeight);
//              if (rps[partner] == null) {
//              rps[partner] = ibis.createReceivePort(portType, COMM_ID
//              + partner);
//              rps[partner].enableConnections();
//              }
                ReadMessage r = rps[partner].receive();
                r.readArray(a.getDataWriteOnly(), offset, xSize * ySize);
                r.finish();

                // Added -- J.
        //        dataInGather0FT += 8 * xSize * ySize;
            }
            int start = xSize * bHeight;

            final double [] pdata = a.getPartialDataReadOnly();

            System.arraycopy(pdata, start, a.getDataWriteOnly(), 
                    start, pdata.length - 2 * start);

        } else {
            int ySize = getPartHeight(globH, myCPU);
            int offset = xSize * bHeight;
//          if (sps[0] == null) {
//          sps[0] = ibis.createSendPort(portType);
//          sps[0].connect(world[0], COMM_ID + myCPU);
//          }
            WriteMessage w = sps[0].newMessage();
            w.writeArray(a.getPartialDataReadOnly(), offset, xSize * ySize);
            w.finish();

            // Added -- J.
         //   dataOutGather0FT += 8 * xSize * ySize;
        }
    }

    private static void doBroadcastSBT(CxArray2dDoubles a) throws Exception {
        // Here we assume CPU 0 (root) to have a full & valid structure
        // which is broadcast to the partial structs of all nodes; east
        // and west borders are also communicated (not north and south).

        int globW = a.getWidth();
        int globH = a.getHeight();

        double[] pData = a.getDataReadOnly().clone();

        a.setPartialData(globW, globH, pData, CxArray2d.NONE, CxArray2d.NONE);

        int xSize = (globW + a.getBorderWidth() * 2) * a.getExtent();
        int length = xSize * globH;
        int offset = xSize * a.getBorderHeight();

        int mask = 1 << (logCPUs - 1);
        for (int i = 0; i < logCPUs; i++) {
            int partner = myCPU ^ mask;
            if ((myCPU % mask == 0) && (partner < nrCPUs)) {
                if (myCPU < partner) {
//                  if (sps[partner] == null) {
//                  sps[partner] = ibis.createSendPort(portType);
//                  sps[partner].connect(world[partner], COMM_ID + myCPU);
//                  }
                    WriteMessage w = sps[partner].newMessage();
                    w.writeArray(a.getPartialDataReadOnly(), offset, length);
                    w.finish();

            //        dataOutBroadcastSBT += length * 8;
                } else {
//                  if (rps[partner] == null) {
//                  rps[partner] = ibis.createReceivePort(portType, COMM_ID
//                  + partner);
//                  rps[partner].enableConnections();
//                  }
                    ReadMessage r = rps[partner].receive();
                    r.readArray(a.getPartialDataWriteOnly(), offset, length);
                    r.finish();

            //        dataInBroadcastSBT += length * 8; 
                }
            }
            mask >>= 1;
        }
    }

    public static int broadcastValue(int value) throws Exception {
        // Added -- J
        long start = System.nanoTime();

        if (nrCPUs == 1) { 
            return value;
        }
        
        int mask = 1 << (logCPUs - 1);
        for (int i = 0; i < logCPUs; i++) {
            int partner = myCPU ^ mask;
            if ((myCPU % mask == 0) && (partner < nrCPUs)) {
                if (myCPU < partner) {
//                  if (sps[partner] == null) {
//                  sps[partner] = ibis.createSendPort(portType);
//                  sps[partner].connect(world[partner], COMM_ID + myCPU);
//                  }
                    WriteMessage w = sps[partner].newMessage();
                    w.writeInt(value);
                    w.finish();
                } else {
//                  if (rps[partner] == null) {
//                  rps[partner] = ibis.createReceivePort(portType, COMM_ID
//                  + partner);
//                  rps[partner].enableConnections();
//                  }
                    ReadMessage r = rps[partner].receive();
                    value = r.readInt();
                    r.finish();
                }
            }
            mask >>= 1;
        }

        // Added -- J
        timeBroadcastValue += System.nanoTime() - start;
        countBroadcastValue++;

        return value;
    }
}
