package ibis.dog;

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.RegistryEventHandler;
import ibis.ipl.util.rpc.RPC;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Communication {

    private static final Logger logger = LoggerFactory
            .getLogger(Communication.class);

    public static final int DEFAULT_TIMEOUT = 30000;

    public static final String DATABASE_ROLE = "database";
    public static final String CLIENT_ROLE = "client";
    public static final String SERVER_ROLE = "server";



    private static final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.MALLEABLE, IbisCapabilities.ELECTIONS_STRICT,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);

    public static Ibis createIbis(String role, RegistryEventHandler eventHandler)
            throws IbisCreationFailedException, IOException {

        logger.info("Initializing communication");

        // Create an Ibis
        Ibis ibis = IbisFactory.createIbis(ibisCapabilities, null, true,
                eventHandler, null, role, RPC.rpcPortTypes);
        
        logger.info("Communication initialized");

        return ibis;
    }
}
