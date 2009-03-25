package jorus.parallel.comm;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;
import jorus.parallel.CommunicationUtil;
import jorus.parallel.PxSystem;

public class IntArrayCommunicationUtil extends CommunicationUtil<int []> {

    public IntArrayCommunicationUtil(PxSystem system) {
        super(system);
    }

    @Override
    public void exchange(final int partner, 
            final int[] out, final int offOut, final int lenOut,
            final int[] in, final int offIn, final int lenIn) throws Exception {
       
        if (rank > partner) {
            send(partner, out, offOut, lenOut);
            receive(partner, in, offIn, lenIn); 
        } else {
            receive(partner, in, offIn, lenIn); 
            send(partner, out, offOut, lenOut);
        }
    }

    @Override
    public void receive(int src, int[] data, int off, int len) throws Exception {
        ReadMessage rm = system.receive(src);
        rm.readArray(data, off, len);
        rm.finish();
    }

    @Override
    public void send(int dest, int[] data, int off, int len) throws Exception {
        WriteMessage wm = system.newMessage(dest);
        wm.writeArray(data, off, len);
        wm.finish();
    }
}