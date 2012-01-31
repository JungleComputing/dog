package ibis.dog.client;

import ibis.dog.Communication;
import ibis.media.imaging.Image;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.util.rpc.RPC;
import ibis.media.video.VideoConsumer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements VideoConsumer, RegistryEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(Client.class);

	public static final int DEFAULT_TIMEOUT = 5000;

	// Communication object.
	private final Ibis ibis;

	private final MessageListener messageListener;

	private final ServerListener serverListener;

	private final Map<IbisIdentifier, ServerHandler> servers;

	private Map<Integer, Integer> cameras;
	
	// current images
	private Image[] inputs;
	private Image[] displays;
	private long inputSeqNr = 0;
	private long lastDisplayedFrame = 0;
	private long lastProcessedFrame = 0;

	// disparity that was generated from "newest" frame
	private Image newestDisparity = null;
	// disparity of frame used to generate newest vector
	private long newestDisparityTimestamp = 0;

	private boolean done = false;

	private final Statistics statistics;

	public Client(MessageListener messageListener,
			ServerListener serverListener,
			StatisticsListener... statisticListeners)
			throws IbisCreationFailedException, IOException {
		this.messageListener = messageListener;
		this.serverListener = serverListener;

		servers = new HashMap<IbisIdentifier, ServerHandler>();
		cameras = new HashMap<Integer, Integer>();
		
		inputs = new Image[cameras.size()];
		displays = new Image[cameras.size() + 1];

		logger.debug("Initializing client");

		ibis = Communication.createIbis(Communication.CLIENT_ROLE, this);

		statistics = new Statistics(statisticListeners, this);
		
		ibis.registry().enableEvents();

		logger.info("Done initializing client");
	}

	public void setCameras(Map<Integer, Integer> cameraList){
		cameras = cameraList;
		inputs = new Image[cameras.size()];
		displays = new Image[cameras.size() + 1];		
	}
	
	public void log(String message) {
		if (messageListener != null) {
			messageListener.message(message);
		}
		logger.debug(message);
	}

	@Override
	public void gotImage(Image image) {
		if (logger.isTraceEnabled()) {
			logger.trace("got Image, format = " + image.getFormat()
					+ " width = " + image.getWidth() + ", height = "
					+ image.getHeight() + ", size = " + image.getSize());
		}

		Image copy;
		// copy images
		try {
			copy = Image.copy(image, null);
		} catch (Exception e) {
			logger.error("could not copy image", e);
			return;
		}

		synchronized (this) {
			int index = cameras.get(copy.getNumber());
			this.inputs[index] = copy;
			this.displays[index] = copy;
			statistics.gotFrame();
			inputSeqNr++;
			notifyAll();
		}
	}

	public synchronized Image getDisplayImage(int index) {
		while (lastDisplayedFrame == inputSeqNr || displays[index] == null) {
			if (done) {
				return null;
			}
			try {
				wait();
			} catch (InterruptedException e) {
				// IGNORE
			}
		}
		lastDisplayedFrame = inputSeqNr;
		statistics.displayedFrame();
		
		return displays[index];
	}

	public synchronized Image getProcessImage(int index) {
		while (lastProcessedFrame == inputSeqNr || inputs[index] == null) {
			if (done) {
				return null;
			}
			try {
				wait();
			} catch (InterruptedException e) {
				// IGNORE
			}
		}
		lastProcessedFrame = inputSeqNr;

		return inputs[index];
	}

	// called by serverHandlers to give us a new result item/vector
	public void newResult(IbisIdentifier server, long timestamp,
			Image disparity) {
		statistics.processedFrame();

		synchronized (this) {
			if (timestamp > newestDisparityTimestamp) {
				newestDisparityTimestamp = timestamp;
				newestDisparity = disparity;
				displays[displays.length - 1] = disparity;
				notifyAll();
			}
		}

		String serverName = server.location().getLevel(
				server.location().numberOfLevels() - 1);
	}

	synchronized int processingServerCount() {
		int result = 0;
		for (ServerHandler handler : servers.values()) {
			if (handler.isActive() && handler.isEnabled()) {
				result++;
			}
		}
		return result;
	}

	private synchronized Image getDisparity(long timestamp)
			throws Exception {
		while (!done && this.newestDisparityTimestamp < timestamp) {
			if (processingServerCount() <= 0) {
				throw new Exception(
						"No servers processing images");
			}

			// FIXME: this check could improve, it only checks if the webcam has
			// ever worked
			if (inputSeqNr == 0) {
				throw new Exception("No images available for processing");
			}

			try {
				wait();
			} catch (InterruptedException e) {
				throw new Exception("Error while waiting for time-stamp");
			}
		}

		if (newestDisparity == null) {
			throw new Exception("Cannot get disparity");
		}

		return newestDisparity;
	}


	public synchronized void end() {
		done = true;
		for (ServerHandler handler : servers.values()) {
			handler.end();
		}
		servers.clear();

		try {
			ibis.end();
		} catch (Exception e) {
			logger.error("Error on ending Ibis", e);
		}
	}

	synchronized boolean isDone() {
		return done;
	}

	public synchronized ServerHandler[] getServers() {
		return servers.values().toArray(new ServerHandler[0]);
	}

	@Override
	public void joined(IbisIdentifier joinedIbis) {
		if (!joinedIbis.tagAsString().equals(Communication.SERVER_ROLE)) {
			return;
		}

		ServerHandler handler;
		synchronized (this) {
			logger.debug("new server: " + joinedIbis);

			handler = new ServerHandler(joinedIbis, this, ibis);

			servers.put(joinedIbis, handler);
		}
		serverListener.newServer(handler);

	}

	@Override
	public void left(IbisIdentifier leftIbis) {
		if (!leftIbis.tagAsString().equals(Communication.SERVER_ROLE)) {
			return;
		}

		ServerHandler handler;
		synchronized (this) {
			logger.debug("server gone: " + leftIbis);

			handler = servers.remove(leftIbis);

			if (handler != null) {
				handler.end();
			}
		}
		if (handler != null) {
			serverListener.serverGone(handler);
		}
	}

	@Override
	public void died(IbisIdentifier corpse) {
		left(corpse);
	}

	@Override
	public void gotSignal(String signal, IbisIdentifier source) {
		// IGNORE
	}

	@Override
	public void electionResult(String electionName, IbisIdentifier winner) {
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

	public int getDatabaseSize() throws RemoteException {
		return 0;
	}	
	
	public void serverStateChanged() {
		serverListener.serverStateChanged();
	}

}
