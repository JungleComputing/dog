package jorus.parallel;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Collectives<T> {

    private static final Logger logger = LoggerFactory
            .getLogger(Collectives.class);

    protected final Broadcast<T> broadcast;
    protected final Scatter<T> scatter;
    protected final Gather<T> gather;
    protected final ReduceArrayToRoot<T> reduceArrayToRoot;
    protected final ReduceArrayToAll<T> reduceArrayToAll;

    @SuppressWarnings("unchecked")
    public Collectives(Properties p, PxSystem s, Class<T> c) throws Exception {

        logger.debug("Loading collectives for: " + c.getName());

        String type = p.getProperty("jorus.broadcast", "Flat");
        String name = "jorus.parallel.collectives." + type + "Broadcast";
        logger.debug("     using " + type + " broadcast for " + c.getName());
        broadcast = (Broadcast<T>) Collective.loadImplementation(name, s, c);

        type = p.getProperty("jorus.scatter", "Flat");
        name = "jorus.parallel.collectives." + type + "Scatter";
        logger.debug("     using " + type + " scatter for " + c.getName());
        scatter = (Scatter<T>) Collective.loadImplementation(name, s, c);

        type = p.getProperty("jorus.gather", "Flat");
        name = "jorus.parallel.collectives." + type + "Gather";
        logger.debug("     using " + type + " gather for " + c.getName());
        gather = (Gather<T>) Collective.loadImplementation(name, s, c);

        type = p.getProperty("jorus.reduceArrayToRoot", "Flat");
        name = "jorus.parallel.collectives." + type + "ReduceArrayToRoot";
        logger.debug("     using " + type + " reduceArrayToRoot for "
                + c.getName());
        reduceArrayToRoot = (ReduceArrayToRoot<T>) Collective
                .loadImplementation(name, s, c);

        type = p.getProperty("jorus.reduceArrayToAll", "MPICH");
        name = "jorus.parallel.collectives." + type + "ReduceArrayToAll";
        logger.debug("     using " + type + " reduceArrayToAll for "
                + c.getName());
        reduceArrayToAll = (ReduceArrayToAll<T>) Collective.loadImplementation(
                name, s, c);
    }
}
