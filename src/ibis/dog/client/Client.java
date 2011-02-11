package ibis.dog.client;

import ibis.dog.Communication;
import ibis.dog.FeatureVector;
import ibis.dog.client.Voter.RecognizeResult;
import ibis.dog.database.Database;
import ibis.dog.database.DatabaseInterface;
import ibis.dog.database.Item;
import ibis.imaging4j.Image;
import ibis.ipl.Ibis;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.util.rpc.RPC;
import ibis.video4j.VideoConsumer;

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

	// current image
	private Image input = null;
	private long inputSeqNr = 0;
	private long lastDisplayedFrame = 0;
	private long lastProcessedFrame = 0;

	private final Voter voter;

	// vector that was generated from "newest" frame
	private FeatureVector newestVector = null;
	// timestamp of frame used to generate newest vector
	private long newestVectorTimestamp = 0;

	private boolean done = false;

	private final Statistics statistics;

	// remote or local database
	private final DatabaseInterface database;

	public Client(MessageListener messageListener,
			ServerListener serverListener, boolean includeDatabase,
			StatisticsListener... statisticListeners)
			throws IbisCreationFailedException, IOException {
		this.messageListener = messageListener;
		this.serverListener = serverListener;

		servers = new HashMap<IbisIdentifier, ServerHandler>();
		voter = new Voter();

		logger.debug("Initializing client");

		ibis = Communication.createIbis(Communication.CLIENT_ROLE, this);

		if (includeDatabase) {
			database = new Database(false);
		} else {
			IbisIdentifier databaseIdentifier = ibis.registry()
					.getElectionResult("database");

			database = RPC.createProxy(DatabaseInterface.class,
					databaseIdentifier, Communication.DATABASE_ROLE, ibis);
		}

		statistics = new Statistics(statisticListeners, this);
		
		ibis.registry().enableEvents();

		logger.info("Done initializing client");
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
		// copy image
		try {
			copy = Image.copy(image, null);
		} catch (Exception e) {
			logger.error("could not copy image", e);
			return;
		}

		synchronized (this) {
			this.input = copy;
			statistics.gotFrame();
			inputSeqNr++;
			notifyAll();
		}
	}

	public synchronized Image getDisplayImage() {
		while (lastDisplayedFrame == inputSeqNr || input == null) {
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
		return input;
	}

	public synchronized Image getProcessImage() {
		while (lastProcessedFrame == inputSeqNr || input == null) {
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

		return input;
	}

	// called by serverHandlers to give us a new result item/vector
	public void newResult(IbisIdentifier server, long timestamp,
			FeatureVector vector, Item item) {
		statistics.processedFrame();

		synchronized (this) {
			if (timestamp > newestVectorTimestamp) {
				newestVectorTimestamp = timestamp;
				newestVector = vector;
				notifyAll();
			}
		}

		voter.newResult(server, timestamp, item);

		String serverName = server.location().getLevel(
				server.location().numberOfLevels() - 1);

		if (item == null) {
			log(serverName + " does not recognize this object");
		} else {
			log(serverName + " says this is a " + item.getName());
		}
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

	private synchronized FeatureVector getFeatureVector(long timestamp)
			throws Exception {
		while (!done && this.newestVectorTimestamp < timestamp) {
			if (processingServerCount() <= 0) {
				throw new Exception(
						"Cannot learn object: No servers processing images");
			}

			// FIXME: this check could improve, it only checks if the webcam has
			// ever worked
			if (inputSeqNr == 0) {
				throw new Exception(
						"Cannot learn object: No images available for processing");
			}

			try {
				wait();
			} catch (InterruptedException e) {
				throw new Exception("Error while learning object");
			}
		}

		if (newestVector == null) {
			throw new Exception(
					"Error while learning object: cannot get vector");
		}

		return newestVector;
	}

	public boolean learn(String name, ProgressListener listener) throws Exception {
		logger.info("learning new item " + name);

		if(!listener.progress(0.0)) {
			return false;
		}

		FeatureVector vector = getFeatureVector(System.currentTimeMillis());

		if (!listener.progress(0.5)) {
			return false;
		}

		// create new database item
		Item item = new Item(vector, name, System.getProperty("user.name"),
				null);
		
		if (!listener.progress(0.6)) {
			return false;
		}

		// learn item (either local, or through RPC mechanism)
		database.learn(item);

		listener.progress(1.0);
		
		return true;
	}

	public RecognizeResult recognize(ProgressListener listener)
			throws Exception {
		logger.info("recognizing object");

		synchronized (this) {
			// FIXME: this check could improve, it only checks if the webcam has
			// ever worked
			if (inputSeqNr == 0) {
				throw new Exception(
						"Cannot recognize object: No images available for processing");

			}
		}

		if (database.size() == 0) {
			throw new Exception("Database empty");
		}

		return voter.recognize(listener, this);
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

		if (database != null && (database instanceof Database)) {
			((Database) database).end();
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

			handler = new ServerHandler(joinedIbis, this, ibis, database);

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
		return database.size();
	}

	public void serverStateChanged() {
		serverListener.serverStateChanged();
	}

}
