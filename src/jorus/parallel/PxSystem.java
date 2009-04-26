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
import ibis.ipl.PortType;
import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;

import java.io.IOException;
import java.util.Properties;

import jorus.array.CxArray2d;
import jorus.array.CxArray2dBytes;
import jorus.array.CxArray2dDoubles;
import jorus.operations.CxRedOp;
import jorus.operations.CxRedOpArray;

public class PxSystem {
    
    private static final int CLOSE_TIMEOUT = 100;

    /** * Ibis Capabilities & PortTypes ******************************* */
    
    private static final PortType portType = new PortType(
            PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_DATA,
            PortType.RECEIVE_EXPLICIT, PortType.CONNECTION_ONE_TO_ONE);

    private static final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.ELECTIONS_STRICT, IbisCapabilities.CLOSED_WORLD);

    private static final String COMM_ID = "px_comm";

    private final Ibis ibis;

    private final IbisIdentifier[] world;

    private final int nrCPUs;

    private final int logCPUs;

    private final int maxCPUs;

    private final int myCPU;

    private final SendPort[] sps;

    private final ReceivePort[] rps;
    
    private final Collectives<byte []> byteCollectives;
    private final Collectives<short []> shortCollectives;
    private final Collectives<int []> intCollectives;
    private final Collectives<long []> longCollectives;
    private final Collectives<float []> floatCollectives;
    private final Collectives<double []> doubleCollectives;
    
    private final Barrier barrier;
    
    private static PxSystem system;
   
    private static long timeBarrier;
    private static long countBarrier;

    private static long timeReduceValueToRoot0FT;
    private static long countReduceValueToRoot0FT;

    private static long timeReduceArrayToRoot;
    private static long countReduceArrayToRoot;
    private static long dataInReduceArrayToRoot;
    private static long dataOutReduceArrayToRoot;

    private static long timeReduceArrayToAll;
    private static long countReduceArrayToAll;
    private static long dataInReduceArrayToAll;
    private static long dataOutReduceArrayToAll;

    private static long timeScatter;
    private static long countScatter;
    private static long dataInScatter;
    private static long dataOutScatter;

    private static long timeGather;
    private static long countGather;
    private static long dataInGather;
    private static long dataOutGather;

    private static long timeBroadcast;
    private static long countBroadcast;
    private static long dataInBroadcast;
    private static long dataOutBroadcast;

    private static long timeBroadcastValue;
    private static long countBroadcastValue;

    private static long timeBorderExchange;
    private static long countBorderExchange;
    private static long dataInBorderExchange;
    private static long dataOutBorderExchange;

    public static PxSystem init(String name, String size)
        throws Exception {
    
        if (system != null) { 
            throw new Exception("PxSystem allready initialized");
        }
        
        system = new PxSystem(name, size);
        
        return system;
    }
    
    public static boolean initialized() {
        return (system != null);
    }
    
    public static PxSystem get() {
        return system;
    }
    
    private PxSystem(String name, String size) throws Exception { 

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
        
        int tmpLog = (int) (Math.log((double) nrCPUs) / Math.log(2.0));
        int tmpMax = (int) Math.pow(2, tmpLog);

        if (tmpMax < nrCPUs) {
            tmpLog++;
            tmpMax *= 2;
        }

        logCPUs = tmpLog;
        maxCPUs = tmpMax;
        
        // Let each node elect itself as the Ibis with 'myCPU' as rank.
        // Then, obtain Ibis identifiers for all CPUs.

        IbisIdentifier me = ibis.registry().elect(Integer.toString(myCPU));
        world = new IbisIdentifier[nrCPUs];

        for (int i = 0; i < nrCPUs; i++) {
            String rank = Long.toString(i);
            world[i] = ibis.registry().getElectionResult(rank);
        }

        // Initialize Send/ReceivePorts to/from all participants
        sps = new SendPort[nrCPUs];
        rps = new ReceivePort[nrCPUs];
        
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

        // Init collectives
        
        Properties p = System.getProperties();
      
        String type = p.getProperty("jorus.barrier", "Flat");
        String clazz = "jorus.parallel.collectives." + type + "Barrier";
        
        System.out.println("Loading " + type + " barrier");
        
        barrier = (Barrier) Collective.loadImplementation(clazz, this, byte[].class);
        
        byteCollectives = new Collectives<byte[]>(p, this, byte[].class);
        shortCollectives = new Collectives<short[]>(p, this, short[].class);
        intCollectives = new Collectives<int[]>(p, this, int[].class);
        longCollectives = new Collectives<long[]>(p, this, long[].class);
        floatCollectives = new Collectives<float[]>(p, this, float[].class);
        doubleCollectives = new Collectives<double[]>(p, this, double[].class);
    }
    
    /** * Public Methods ********************************************** */
        
    private double getThroughput(long data, long nanos) { 

        double Mbits = (data * 8.0) / 1000000.0;
        double sec = nanos / (1000.0 * 1000.0 * 1000.0);

        return (Mbits / sec);
    }

    public void printStatistics() {
        long totalTime = timeBarrier + timeReduceValueToRoot0FT
        + timeReduceArrayToRoot + timeReduceArrayToAll
        + timeScatter + timeGather + timeBroadcast
        + timeBroadcastValue + timeBorderExchange;

        long totalCount = countBarrier + countReduceValueToRoot0FT
        + countReduceArrayToRoot + countReduceArrayToAll
        + countScatter + countGather + countBroadcast
        + countBroadcastValue + countBorderExchange;

        System.out.printf("Total communication time %.2f usec, count %d\n", (totalTime / 1000.0), totalCount);
        System.out.printf("            barrier time %.2f usec, count %d\n", (timeBarrier / 1000.0), countBarrier);
        System.out.printf("     broadcastValue time %.2f usec, count %d\n", (timeBroadcastValue / 1000.0), countBroadcastValue);
        System.out.printf("          reduceV2R time %.2f usec, count %d\n", (timeReduceValueToRoot0FT / 1000.0), countReduceValueToRoot0FT);
        System.out.printf("          reduceA2R time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeReduceArrayToRoot / 1000.0), 
                countReduceArrayToRoot, 
                dataInReduceArrayToRoot, 
                dataOutReduceArrayToRoot, 
                getThroughput(dataInReduceArrayToRoot + dataOutReduceArrayToRoot, timeReduceArrayToRoot));

        System.out.printf("          reduceA2A time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeReduceArrayToAll / 1000.0), 
                countReduceArrayToAll, 
                dataInReduceArrayToAll, 
                dataOutReduceArrayToAll, 
                getThroughput(dataInReduceArrayToAll + dataOutReduceArrayToAll, timeReduceArrayToAll));

        System.out.printf("            scatter time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeScatter / 1000.0), 
                countScatter, 
                dataInScatter, 
                dataOutScatter, 
                getThroughput(dataInScatter + dataOutScatter, timeScatter));

        System.out.printf("             gather time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeGather / 1000.0), 
                countGather, 
                dataInGather,
                dataOutGather, 
                getThroughput(dataInGather + dataOutGather, timeGather));

        System.out.printf("       broadcastSBT time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeBroadcast / 1000.0), 
                countBroadcast, 
                dataInBroadcast, 
                dataOutBroadcast, 
                getThroughput(dataInBroadcast + dataOutBroadcast, timeBroadcast));

        System.out.printf("     borderExchange time %.2f usec, count %d, dataIn %d bytes, dataOut %d bytes, TP %.2f Mbit/s\n", 
                (timeBorderExchange / 1000.0), 
                countBorderExchange, 
                dataInBorderExchange, 
                dataOutBorderExchange,
                getThroughput(dataInBorderExchange + dataOutBorderExchange, timeBorderExchange));

        timeBarrier = 0;
        timeReduceValueToRoot0FT = 0;
        timeReduceArrayToRoot = 0;
        timeReduceArrayToAll = 0;
        timeScatter = 0;
        timeGather = 0;
        timeBroadcast = 0;
        timeBroadcastValue = 0;
        timeBorderExchange = 0;

        countBarrier = 0;
        countReduceValueToRoot0FT = 0;
        countReduceArrayToRoot = 0;
        countReduceArrayToAll = 0;
        countScatter = 0;
        countGather = 0;
        countBroadcast = 0;
        countBroadcastValue = 0;
        countBorderExchange = 0;

        dataInReduceArrayToRoot = 0;
        dataOutReduceArrayToRoot = 0;

        dataInReduceArrayToAll = 0;
        dataOutReduceArrayToAll = 0;

        dataInScatter = 0;
        dataOutScatter = 0;

        dataInGather = 0;
        dataOutGather = 0;

        dataInBroadcast = 0;
        dataOutBroadcast = 0;

        dataInBorderExchange = 0;
        dataOutBorderExchange = 0;
    }

    public void exitParallelSystem() throws Exception {
        for (int i = 0; i < nrCPUs; i++) {
            if (sps[i] != null)
                sps[i].close();
            if (rps[i] != null)
                rps[i].close(CLOSE_TIMEOUT);
        }
        ibis.end();
    }

    public int myCPU() {
        return myCPU;
    }

    public int nrCPUs() {
        return nrCPUs;
    }
/*
    public Collectives<?> getCollective(Class<?> c) throws Exception { 
        
        // FIXME: ugly -- J
        if (!c.isArray()) { 
            throw new Exception("Illegal Collective type: " + c.getName());
        }
     
        Class component = c.getComponentType();
        
        if (component.equals(byte.class)) { 
            return byteCollectives;
        } else if (component.equals(short.class)) { 
            return shortCollectives;
        } else if (component.equals(int.class)) { 
            return intCollectives;
        } else if (component.equals(long.class)) { 
            return longCollectives;
        } else if (component.equals(float.class)) { 
            return floatCollectives;
        } else if (component.equals(double.class)) { 
            return doubleCollectives;
        } else { 
            throw new Exception("Illegal Collectives type: " + c.getName());
        }
    } 
  */
    
    public void barrier() throws Exception { 
    
        if (nrCPUs == 1) { 
            return;
        }
        
        long start = System.nanoTime();
        
        barrier.barrier();
   
        timeBarrier += System.nanoTime() - start;
        countBarrier++;
    }
    
    public void reduceValueToRoot(double data, CxRedOp op) throws Exception { 
    
        if (nrCPUs == 1) { 
            return;
        }
        
        throw new RuntimeException("Not implemented!");
        // TODO: implement!
    }
    
    public double [] reduceArrayToRoot(double [] data, CxRedOpArray op) throws Exception { 
        
        if (nrCPUs == 1) { 
            return data;
        }
    
        long start = System.nanoTime();
        
        double [] result = doubleCollectives.reduceArrayToRoot.reduceArrayToRoot(data, op);

        timeReduceArrayToRoot += System.nanoTime() - start;
        countReduceArrayToRoot++;
        
        return result;
    }
    
    public double [] reduceArrayToAll(double [] data, CxRedOpArray op) throws Exception { 
        
        if (nrCPUs == 1) { 
            return data;
        }
        
        long start = System.nanoTime();
        
        double [] result = doubleCollectives.reduceArrayToAll.reduceArrayToAll(data, op);
    
        timeReduceArrayToAll += System.nanoTime() - start;
        countReduceArrayToAll++;
        
        return result;
    }
   
    public byte [] reduceArrayToAll(byte [] data, CxRedOpArray op) throws Exception { 
        
        if (nrCPUs == 1) { 
            return data;
        }
        
        long start = System.nanoTime();
        
        byte [] result = byteCollectives.reduceArrayToAll.reduceArrayToAll(data, op);
    
        timeReduceArrayToAll += System.nanoTime() - start;
        countReduceArrayToAll++;
        
        return result;
    }
    
    public void broadcastArray(double [] data) throws Exception {
        
        if (nrCPUs == 1) { 
            return;
        }
        
        long start = System.nanoTime();
        
        doubleCollectives.broadcast.broadcast(data);
    
        timeBroadcast += System.nanoTime() - start;
        countBroadcast++;
    }

    public void broadcastArray(int [] data) throws Exception {
        
        if (nrCPUs == 1) { 
            return;
        }
        
        long start = System.nanoTime();
        
        intCollectives.broadcast.broadcast(data);
    
        // Added -- J
        timeBroadcast += System.nanoTime() - start;
        countBroadcast++;
    
    }
    
    public void broadcast(CxArray2dDoubles a) throws Exception {
        throw new RuntimeException("Not implemented");
    }
    
    public void scatter(CxArray2d a) throws Exception {
        
        if (nrCPUs == 1) { 
            // On 1 CPU we simply create an alias to the same data
            a.setPartialData(a.getWidth(), a.getHeight(), a.getDataReadWrite(), 
                    CxArray2d.VALID, CxArray2d.PARTIAL);
            return;
        }
        
        long start = System.nanoTime();
          
        if (a instanceof CxArray2dBytes) { 
            scatterBytes((CxArray2dBytes) a);
        } else if (a instanceof CxArray2dDoubles) { 
            scatterDoubles((CxArray2dDoubles) a);
        } else { 
            throw new RuntimeException("Not implemented");
        }

        a.setLocalState(CxArray2d.VALID);
        a.setDistType(CxArray2d.PARTIAL);

        // Added -- J
        timeScatter += System.nanoTime() - start;
        countScatter++;
        
    } 
    
    private void scatterDoubles(CxArray2dDoubles a) throws Exception {
        // Here we assume CPU 0 (root) to have a full & valid structure
        // which is scattered to the partial structs of all nodes. East
        // and west borders are also communicated (not north and south).
        
        int globH = a.getHeight();
        int extent = a.getExtent();
        int pWidth = a.getWidth();
        int pHeight = getPartHeight(globH, myCPU);
        int bWidth = a.getBorderWidth();
        int bHeight = a.getBorderHeight();

        int len = (pWidth + bWidth * 2) * (pHeight + bHeight * 2) * extent;
        
        double[] pData = null;
        
        if (a.hasPartialData()) { 
            
            pData = a.getPartialDataReadWrite();
        
            if (pData.length != len) {
                pData = null;
            }
        } 
        
        if (pData == null) { 
            pData = new double[len];
            a.setPartialData(pWidth, pHeight, pData, CxArray2d.NONE, CxArray2d.NONE);
        }

        int xSize = (pWidth + bWidth * 2) * extent;

        int [] offsets = new int[nrCPUs];
        int [] sizes = new int[nrCPUs];
        
        //offsets[0] = xSize * bHeight;
        //sizes[0] = pData.length - 2 * xSize * bHeight;
        
        for (int partner = 0; partner < nrCPUs; partner++) {
            offsets[partner] = xSize * (getLclStartY(globH, partner) + bHeight);
            sizes[partner] = xSize * getPartHeight(globH, partner); 
        }
            
        
        //if (myCPU == 0) {
        /*
            int [] offsets = new int[nrCPUs];
            int [] sizes = new int[nrCPUs];
            
            offsets[0] = xSize * bHeight;
            sizes[0] = pData.length - 2 * xSize * bHeight;
            
            for (int partner = 1; partner < nrCPUs; partner++) {
                offsets[partner] = xSize * (getLclStartY(globH, partner) + bHeight);
                sizes[partner] = xSize * getPartHeight(globH, partner); 
            }
         */
           
       //     doubleCollectives.scatter.scatter(a.getDataReadOnly(), offsets, sizes, 
        //            pData, 0, sizes[0]);
        
      //  } else {
        
      //      int size = xSize * getPartHeight(globH, myCPU);
      //      int offset = xSize * bHeight;
      
            doubleCollectives.scatter.scatter(a.getDataReadOnly(), offsets, sizes, 
                    pData, 0, sizes[myCPU]);
     //   }
    }

    private void scatterBytes(CxArray2dBytes a) throws Exception {
        // Here we assume CPU 0 (root) to have a full & valid structure
        // which is scattered to the partial structs of all nodes. East
        // and west borders are also communicated (not north and south).
        
        int globH = a.getHeight();
        int extent = a.getExtent();
        int pWidth = a.getWidth();
        int pHeight = getPartHeight(globH, myCPU);
        int bWidth = a.getBorderWidth();
        int bHeight = a.getBorderHeight();

        int len = (pWidth + bWidth * 2) * (pHeight + bHeight * 2) * extent;
        
        byte[] pData = null;
        
        if (a.hasPartialData()) { 
            
            pData = a.getPartialDataReadWrite();
        
            if (pData.length != len) {
                pData = null;
            }
        } 
        
        if (pData == null) { 
            pData = new byte[len];
            a.setPartialData(pWidth, pHeight, pData, CxArray2d.NONE, CxArray2d.NONE);
        }

        int xSize = (pWidth + bWidth * 2) * extent;

        int [] offsets = new int[nrCPUs];
        int [] sizes = new int[nrCPUs];
        
        for (int partner = 0; partner < nrCPUs; partner++) {
            offsets[partner] = xSize * (getLclStartY(globH, partner) + bHeight);
            sizes[partner] = xSize * getPartHeight(globH, partner); 
        }
        
        byteCollectives.scatter.scatter(a.getDataReadOnly(), offsets, sizes, 
                pData, 0, sizes[myCPU]);
    }

    
    public void gather(CxArray2dDoubles a) throws Exception {
        throw new RuntimeException("Not implemented");
    }
    
    
    /*
    public void barrierSBT() throws Exception {
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
*/
    
    /*
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
*/
    
    /*   
    public static double[] reduceArrayToRootOFT_Flat(double[] a, CxRedOpArray op)
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
    */

/*
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
*/
    /*
    @SuppressWarnings("unchecked")
    public static double[] reduceArrayToAllOFT_BinomialSimple(double[] a, 
            CxRedOpArray op) throws Exception {

        // Added -- J
        long start = System.nanoTime();

        final double [] tmp = new double[a.length];
        
        int mask = 1;

        for (int i=0; i<logCPUs; i++) {
            
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
*/
    
      
   /* public static double[] reduceArrayToAllOFT_RecursiveHalving(double[] a, 
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
        
         In the non-power-of-two case, all even-numbered
           processes of rank < 2*rem send their data to
           (rank+1). These even-numbered processes no longer
           participate in the algorithm until the very end. The
           remaining processes form a nice power-of-two. 
        
        if (rank < 2*rem) {
            
          //  System.out.println("ALLREDUCE: Adjust processes (PRE)!");
            
            if (rank % 2 == 0) {  even 
                WriteMessage w = sps[rank+1].newMessage();
                w.writeArray(a);
                w.finish();
            
                 temporarily set the rank to -1 so that this
                   process does not pariticipate in recursive
                   doubling 
                newrank = -1;
                
            } else {  odd 
                
                ReadMessage r = rps[rank-1].receive();
                r.readArray(tmp);
                r.finish();
            
                 do the reduction on received data. since the
                   ordering is right, it doesn't matter whether
                   the operation is commutative or not. 
            
                op.doIt(a, tmp);
                
                 change the rank 
                newrank = rank / 2;
            }
            
        } else {  rank >= 2*rem 
        
            newrank = rank - rem;
        }
                
        // We will now perform a reduce using recursive doubling when there is 
        // little data, or recursive halving followed by an allgather when there
        // is enough data. Some machines will be left out here if the number of 
        // machines is not a power of two. 

        if (newrank != -1) {
            
            if (((a.length * 8) <= ALLREDUCE_SHORT_MSG) || (count < pof2)) { 
                 use recursive doubling                 

            //    System.out.println("ALLREDUCE: Using doubling");

                
                int mask = 0x1;
                
                while (mask < pof2) {
                    int newdst = newrank ^ mask;
                    
                     find real rank of dest 
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;
                    
                     Send the most current data, which is in a. Receive
                       into tmp 
                    
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
                    
                     tmp contains data received in this step.
                       a contains data accumulated so far 
                    
                    op.doIt(a, tmp);            
                    
                    mask <<= 1;
                }
            } else {
                 do a reduce-scatter followed by allgather 
                     
                 for the reduce-scatter, calculate the count that
                   each process receives and the displacement within
                   the buffer 
                     
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
                     find real rank of dest 
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

                     Send data from a. Receive into tmp 
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
                    
                     tmp contains data received in this step.
                       a contains data accumulated so far 
                    op.doItRange(a, tmp, disps[recv_idx], recv_cnt);            

                     update send_idx for next iteration 
                    send_idx = recv_idx;
                    mask <<= 1;

                     update last_idx, but not in last iteration
                       because the value is needed in the allgather
                       step below. 
                    
                    if (mask < pof2) { 
                        last_idx = recv_idx + pof2/mask;
                    } 
                }

                 now do the allgather 
                mask >>= 1;
                    
                while (mask > 0) {
                    int newdst = newrank ^ mask;

                     find real rank of dest 
                    int dst = (newdst < rem) ? newdst*2 + 1 : newdst + rem;

                    int send_cnt = 0; 
                    int recv_cnt = 0;

                    if (newrank < newdst) {
                         update last_idx except on first iteration 
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

                     Send data from a. Receive into tmp 
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

         In the non-power-of-two case, all odd-numbered
           processes of rank < 2*rem send the result to
          (rank-1), the ranks who didn't participate above. 
        
        if (rank < 2*rem) {
            
     //       System.out.println("ALLREDUCE: Adjust processes (POST)!");

            
            if (rank % 2 == 1) {  odd 
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
    */
    /*
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
*/
    
    /*
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
*/
    
/*
    public static void borderExchange(double[] a, int width, int height,
            int off, int stride, int ySize) throws Exception {	
        
        if (nrCPUs == 1) {
            return;
        }
        
        borderExchange_Jason(a, width, height, off, stride, ySize);		
    }
*/
    
    public void borderExchange(double[] a, int width, int height,
            int off, int stride, int ySize) throws Exception {

        // Added -- J
        long start = System.nanoTime();

        // Border exchange in vertical direction (top <---> bottom)
        int prevCPU = myCPU - 1;
        int nextCPU = myCPU + 1;

        int xSize = width + stride;
    
//      System.out.println("Border exchange: " + xSize + "x" + ySize + " (" + (xSize*ySize) + ")");

        if ((myCPU & 1) == 0) { 

            if (prevCPU >= 0) {
                WriteMessage w = sps[prevCPU].newMessage();
                w.writeArray(a, off - stride / 2, xSize * ySize);
                w.finish();

       //         dataOutBorderExchange += xSize * ySize * 8;
            }

            if (nextCPU < nrCPUs) {
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

            if (nextCPU < nrCPUs) {
                ReadMessage r = rps[nextCPU].receive();
                r.readArray(a, off - stride / 2 + height * xSize, xSize * ySize);
                r.finish();

         //       dataInBorderExchange += xSize * ySize * 8;        
            }

        } else { 

            if (nextCPU < nrCPUs) {
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

            if (nextCPU < nrCPUs) {
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

    /*
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
*/
    
    public int getPartHeight(int height, int CPUnr) {
        int minLocalH = height / nrCPUs;
        int overflowH = height % nrCPUs;

        if (CPUnr < overflowH) {
            minLocalH++;
        }
        return minLocalH;
    }

    public int getLclStartY(int height, int CPUnr) {
        int minLocalH = height / nrCPUs;
        int overflowH = height % nrCPUs;

        if (CPUnr < overflowH) {
            return (CPUnr * (minLocalH + 1));
        } else {
            return (CPUnr * minLocalH + overflowH);
        }
    }

    /*
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

    */
    
    public ReadMessage receive(int src) throws IOException {
    
        if (rps[src] == null) { 
            // create ?
        }
        
        return rps[src].receive();
    }

    public WriteMessage newMessage(int dest) throws IOException {
    
        if (sps[dest] == null) { 
            // connect ?
        }
        
        return sps[dest].newMessage();
    }
}
