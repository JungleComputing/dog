/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra  (fjseins@cs.vu.nl)
 *
 */


package jorus.parallel;

import ibis.ipl.*;
import jorus.array.*;
import jorus.operations.*;
import java.util.Properties;


public class PxSystem
{
	/*** Ibis Capabilities & PortTypes ********************************/

	private static PortType portType =
						new PortType(
							PortType.COMMUNICATION_RELIABLE,
							PortType.SERIALIZATION_DATA,
							PortType.RECEIVE_EXPLICIT,
							PortType.CONNECTION_ONE_TO_ONE);

	private static IbisCapabilities ibisCapabilities =
						new IbisCapabilities(
							IbisCapabilities.ELECTIONS_STRICT,
							IbisCapabilities.CLOSED_WORLD);



	/*** Send & ReceivePorts to/from all particpipants ****************/

	private static int				NR_PORTS = 0;
	private static SendPort[]		sps = null;
	private static ReceivePort[]	rps = null;
	private static final String		COMM_ID = "px_comm";



	/*** GENERAL 'PARALLEL WORLD' INFORMATION *************************/

	private static Ibis				ibis = null;
	private static IbisIdentifier[]	world = null;
	private static int				nrCPUs = -1;
	private static int				logCPUs = -1;
	private static int				maxCPUs = -1;
	private static int				myCPU = -1;
	private static boolean			initialized = false;



	/*** Public Methods ***********************************************/

	public static void initParallelSystem(String name, String size)
	throws Exception
	{
		Properties props = new Properties();
		props.setProperty("ibis.pool.name", name);
		props.setProperty("ibis.pool.size", size);


		// Create Ibis & obtain parallel environment parameters (local)

//		ibis = IbisFactory.createIbis(ibisCapabilities, null, portType);
		ibis = IbisFactory.createIbis(ibisCapabilities,
									  props, true, null, portType);
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

		IbisIdentifier me =
						ibis.registry().elect(Integer.toString(myCPU));
		world = new IbisIdentifier[nrCPUs];
		for (int i=0; i<nrCPUs; i++) {
			String rank = Long.toString(i);
			world[i] = ibis.registry().getElectionResult(rank);
		}


		// Initialize Send/ReceivePorts to/from all participants

		NR_PORTS = nrCPUs;
		sps = new SendPort[NR_PORTS];
		rps = new ReceivePort[NR_PORTS];

		initialized = true;
	}


	public static void exitParallelSystem() throws Exception
	{
		for (int i=0; i<NR_PORTS; i++) {
			if (sps[i] != null) sps[i].close();
			if (rps[i] != null) rps[i].close();
		}
		ibis.end();
		initialized = false;
	}


	public static boolean initialized()
	{
		return initialized;
	}


	public static int myCPU()
	{
		return myCPU;
	}


	public static int nrCPUs()
	{
		return nrCPUs;
	}


	public static void barrierSBT() throws Exception
	{
		int mask = 1;
		for (int i=0; i<logCPUs; i++) {
			int partner = myCPU ^ mask;
			if ((myCPU % mask == 0) && (partner < nrCPUs)) {
				if (myCPU > partner) {
					if (sps[partner] == null) {
						sps[partner] = ibis.createSendPort(portType);
						sps[partner].connect(world[partner],
											  COMM_ID + myCPU);
					}
					WriteMessage w = sps[partner].newMessage();
					w.finish();
				} else {
					if (rps[partner] == null) {
						rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
						rps[partner].enableConnections();
					}
					ReadMessage r = rps[partner].receive();
					r.finish();
				}
			}
			mask <<= 1;
		}
		mask = 1 << (logCPUs-1);
		for (int i=0; i<logCPUs; i++) {
			int partner = myCPU ^ mask;
			if ((myCPU % mask == 0) && (partner < nrCPUs)) {
				if (myCPU < partner) {
					if (sps[partner] == null) {
						sps[partner] = ibis.createSendPort(portType);
						sps[partner].connect(world[partner],
												COMM_ID + myCPU);
					}
					WriteMessage w = sps[partner].newMessage();
					w.finish();
				} else {
					if (rps[partner] == null) {
						rps[partner] = ibis.createReceivePort(portType,
											COMM_ID + partner);
						rps[partner].enableConnections();
					}
					ReadMessage r = rps[partner].receive();
					r.finish();
				}
			}
			mask >>= 1;
		}
	}


	public static double reduceValueToRootOFT(double val, CxRedOp op)
	throws Exception
	{
		double result = val;

		if (myCPU == 0) {
			for (int partner=1; partner<nrCPUs; partner++) {
				if (rps[partner] == null) {
					rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
					rps[partner].enableConnections();
				}
				ReadMessage r = rps[partner].receive();
				double recvVal = r.readDouble();
				r.finish();
				result = (Double) op.doIt(result, recvVal);
			}
		} else {
			if (sps[0] == null) {
				sps[0] = ibis.createSendPort(portType);
				sps[0].connect(world[0], COMM_ID + myCPU);
			}
			WriteMessage w = sps[0].newMessage();
			w.writeDouble(val);
			w.finish();
		}
		return result;
	}


	public static double[]
	reduceArrayToRootOFT(double[] a, CxRedOpArray op) throws Exception
	{
		if (myCPU == 0) {
			double[] recvArray = new double[a.length];
			for (int partner=1; partner<nrCPUs; partner++) {
				if (rps[partner] == null) {
					rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
					rps[partner].enableConnections();
				}
				ReadMessage r = rps[partner].receive();
				r.readArray(recvArray);
				r.finish();
				op.doIt(a, recvArray);
			}
		} else {
			if (sps[0] == null) {
				sps[0] = ibis.createSendPort(portType);
				sps[0].connect(world[0], COMM_ID + myCPU);
			}
			WriteMessage w = sps[0].newMessage();
			w.writeArray(a);
			w.finish();
		}
		return a;
	}


	public static double[]
	reduceArrayToAllOFT(double[] a, CxRedOpArray op) throws Exception
	{
		if (myCPU == 0) {
			double[] recvArray = new double[a.length];
			for (int partner=1; partner<nrCPUs; partner++) {
				if (rps[partner] == null) {
					rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
					rps[partner].enableConnections();
				}
				ReadMessage r = rps[partner].receive();
				r.readArray(recvArray);
				r.finish();
				op.doIt(a, recvArray);
			}
			for (int partner=1; partner<nrCPUs; partner++) {
				if (sps[partner] == null) {
					sps[partner] = ibis.createSendPort(portType);
					sps[partner].connect(world[partner], COMM_ID + 0);
				}
				WriteMessage w = sps[partner].newMessage();
				w.writeArray(a);
				w.finish();
			}
		} else {
			if (sps[0] == null) {
				sps[0] = ibis.createSendPort(portType);
				sps[0].connect(world[0], COMM_ID + myCPU);
			}
			WriteMessage w = sps[0].newMessage();
			w.writeArray(a);
			w.finish();

			if (rps[0] == null) {
				rps[0] = ibis.createReceivePort(portType, COMM_ID + 0);
				rps[0].enableConnections();
			}
			ReadMessage r = rps[0].receive();
			r.readArray(a);
			r.finish();
		}
		return a;
	}


	public static void scatterOFT(CxArray2d a) throws Exception
	{
		if (a instanceof CxArray2dDoubles) {
			doScatterOFT((CxArray2dDoubles)a);
		} else {
System.out.println("ERROR: SCATTER OFT NOT IMPLEMENTED YET!!!");
		}
		a.setLocalState(CxArray2d.VALID);
		a.setDistType(CxArray2d.PARTIAL);
	}


	public static void gatherOFT(CxArray2d a) throws Exception
	{
		if (a instanceof CxArray2dDoubles) {
			doGatherOFT((CxArray2dDoubles)a);
		} else {
System.out.println("ERROR: GATHER OFT NOT IMPLEMENTED YET!!!");
		}
		a.setGlobalState(CxArray2d.VALID);
	}


	public static void broadcastSBT(CxArray2d a) throws Exception
	{
		if (a instanceof CxArray2dDoubles) {
			doBroadcastSBT((CxArray2dDoubles)a);
		} else {
System.out.println("ERROR: BROADCAST SBT NOT IMPLEMENTED YET!!!");
		}
		a.setLocalState(CxArray2d.VALID);
		a.setDistType(CxArray2d.FULL);
	}


	public static void borderExchange(double[] a, int width, int height,
									  int off, int stride, int ySize)
	throws Exception
	{
		// Border exchange in vertical direction (top <---> bottom)

		int part1 = myCPU-1;
		int part2 = myCPU+1;
		int xSize = width+stride;


		// Send to first partner and receive from second partner

		if (part1 >= 0) {
			if (sps[part1] == null) {
				sps[part1] = ibis.createSendPort(portType);
				sps[part1].connect(world[part1], COMM_ID + myCPU);
			}
			WriteMessage w = sps[part1].newMessage();
			w.writeArray(a, off - stride/2, xSize*ySize);
			w.finish();
		}
		if (part2 < PxSystem.nrCPUs()) {
			if (rps[part2] == null) {
				rps[part2] = ibis.createReceivePort(portType,
												COMM_ID + part2);
				rps[part2].enableConnections();
			}
			ReadMessage r = rps[part2].receive();
			r.readArray(a, off - stride/2 + height*xSize, xSize*ySize);
			r.finish();

		// Send to second partner and receive from first partner

			if (sps[part2] == null) {
				sps[part2] = ibis.createSendPort(portType);
				sps[part2].connect(world[part2], COMM_ID + myCPU);
			}
			WriteMessage w = sps[part2].newMessage();
			w.writeArray(a, off - stride/2 + (height-ySize)*xSize,
														xSize*ySize);
			w.finish();
		}
		if (part1 >= 0) {
			if (rps[part1] == null) {
				rps[part1] = ibis.createReceivePort(portType,
												COMM_ID + part1);
				rps[part1].enableConnections();
			}
			ReadMessage r = rps[part1].receive();
			r.readArray(a, 0, xSize*ySize);
			r.finish();
		}
	}


	private static int getPartHeight(int height, int CPUnr)
	{
		int minLocalH = height/nrCPUs;
		int overflowH = height%nrCPUs;

		if (CPUnr < overflowH) {
			minLocalH++;
		}
		return minLocalH;
	}


	public static int getLclStartY(int height, int CPUnr)
	{
		int minLocalH = height/nrCPUs;
		int overflowH = height%nrCPUs;

		if (CPUnr < overflowH) {
			return (CPUnr * (minLocalH+1));
		} else {
			return (CPUnr * minLocalH + overflowH);
		}
	}


	private static void doScatterOFT(CxArray2dDoubles a)
	throws Exception
	{
		// Here we assume CPU 0 (root) to have a full & valid structure
		// which is scattered to the partial structs of all nodes. East
		// and west borders are also communicated (not north and south).

		int globH   = a.getHeight();
		int extent  = a.getExtent();
		int pWidth  = a.getWidth();
		int pHeight = getPartHeight(globH, myCPU);
		int bWidth  = a.getBorderWidth();
		int bHeight = a.getBorderHeight();
		double[] pData = new double[(pWidth + bWidth*2) *
									(pHeight + bHeight*2) * extent];
		a.setPartialData(pWidth, pHeight, pData,
						 CxArray2d.NONE, CxArray2d.NONE);

		int xSize = (pWidth + bWidth*2) * extent;
		if (myCPU == 0) {
			for (int partner=1; partner<nrCPUs; partner++) {
				int ySize  = getPartHeight(globH, partner);
				int offset = xSize *
							 (getLclStartY(globH, partner) + bHeight);
				if (sps[partner] == null) {
					sps[partner] = ibis.createSendPort(portType);
					sps[partner].connect(world[partner], COMM_ID + 0);
				}
				WriteMessage w = sps[partner].newMessage();
				w.writeArray(a.getData(), offset, xSize*ySize);
				w.finish();
			}
			int start = xSize * bHeight;
			System.arraycopy(a.getData(), start, pData,
										  start, pData.length-2*start);

		} else {
			int ySize = getPartHeight(globH, myCPU);
			int offset = xSize * bHeight;
			if (rps[0] == null) {
				rps[0] = ibis.createReceivePort(portType, COMM_ID + 0);
				rps[0].enableConnections();
			}
			ReadMessage r = rps[0].receive();
			r.readArray(a.getPartialData(), offset, xSize*ySize);
			r.finish();
		}
	}


	private static void doGatherOFT(CxArray2dDoubles a)
	throws Exception
	{
		// Here we assume all nodes to have a full yet invalid global
		// structure and a valid partial structure, which is gathered
		// to the global structure of CPU 0; east and west borders are
		// also communicated (not north and south).

		int globH   = a.getHeight();
		int extent  = a.getExtent();
		int pWidth  = a.getWidth();
		int bWidth  = a.getBorderWidth();
		int bHeight = a.getBorderHeight();

		int xSize = (pWidth + bWidth*2) * extent;
		if (myCPU == 0) {
			for (int partner=1; partner<nrCPUs; partner++) {
				int ySize  = getPartHeight(globH, partner);
				int offset = xSize *
							 (getLclStartY(globH, partner) + bHeight);
				if (rps[partner] == null) {
					rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
					rps[partner].enableConnections();
				}
				ReadMessage r = rps[partner].receive();
				r.readArray(a.getData(), offset, xSize*ySize);
				r.finish();
			}
			int start = xSize * bHeight;
			System.arraycopy(a.getPartialData(), start, a.getData(),
							 start, a.getPartialData().length-2*start);

		} else {
			int ySize = getPartHeight(globH, myCPU);
			int offset = xSize * bHeight;
			if (sps[0] == null) {
				sps[0] = ibis.createSendPort(portType);
				sps[0].connect(world[0], COMM_ID + myCPU);
			}
			WriteMessage w = sps[0].newMessage();
			w.writeArray(a.getPartialData(), offset, xSize*ySize);
			w.finish();
		}
	}


	private static void doBroadcastSBT(CxArray2dDoubles a)
	throws Exception
	{
		// Here we assume CPU 0 (root) to have a full & valid structure
		// which is broadcast to the partial structs of all nodes; east
		// and west borders are also communicated (not north and south).

		int globW = a.getWidth();
		int globH = a.getHeight();

		double[] pData = a.getData().clone();
		a.setPartialData(globW, globH, pData,
						 CxArray2d.NONE, CxArray2d.NONE);

		int xSize  = (globW + a.getBorderWidth()*2) * a.getExtent();
		int length = xSize * globH;
		int offset = xSize * a.getBorderHeight();

		int mask = 1 << (logCPUs-1);
		for (int i=0; i<logCPUs; i++) {
			int partner = myCPU ^ mask;
			if ((myCPU % mask == 0) && (partner < nrCPUs)) {
				if (myCPU < partner) {
					if (sps[partner] == null) {
						sps[partner] = ibis.createSendPort(portType);
						sps[partner].connect(world[partner],
													COMM_ID + myCPU);
					}
					WriteMessage w = sps[partner].newMessage();
					w.writeArray(a.getPartialData(), offset, length);
					w.finish();
				} else {
					if (rps[partner] == null) {
						rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
						rps[partner].enableConnections();
					}
					ReadMessage r = rps[partner].receive();
					r.readArray(a.getPartialData(), offset, length);
					r.finish();
				}
			}
			mask >>= 1;
		}
	}


	public static int broadcastValue(int value)
	throws Exception
	{
		int mask = 1 << (logCPUs-1);
		for (int i=0; i<logCPUs; i++) {
			int partner = myCPU ^ mask;
			if ((myCPU % mask == 0) && (partner < nrCPUs)) {
				if (myCPU < partner) {
					if (sps[partner] == null) {
						sps[partner] = ibis.createSendPort(portType);
						sps[partner].connect(world[partner],
													COMM_ID + myCPU);
					}
					WriteMessage w = sps[partner].newMessage();
					w.writeInt(value);
					w.finish();
				} else {
					if (rps[partner] == null) {
						rps[partner] = ibis.createReceivePort(portType,
												COMM_ID + partner);
						rps[partner].enableConnections();
					}
					ReadMessage r = rps[partner].receive();
					value = r.readInt();
					r.finish();
				}
			}
			mask >>= 1;
		}
		return value;
	}
}
