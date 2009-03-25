package jorus.parallel.comm;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;
import jorus.parallel.CommunicationUtil;
import jorus.parallel.PxSystem;

public class FloatArrayCommunicationUtil extends CommunicationUtil<float []> {

    public FloatArrayCommunicationUtil(PxSystem system) {
        super(system);
    }

    @Override
    public void exchange(final int partner, 
            final float[] out, final int offOut, final int lenOut,
            final float[] in, final int offIn, final int lenIn) throws Exception {
       
        if (rank > partner) {
            send(partner, out, offOut, lenOut);
            receive(partner, in, offIn, lenIn); 
        } else {
            receive(partner, in, offIn, lenIn); 
            send(partner, out, offOut, lenOut);
        }
    }

    @Override
    public void receive(int src, float[] data, int off, int len) throws Exception {
        ReadMessage rm = system.receive(src);
        rm.readArray(data, off, len);
        rm.finish();
    }

    @Override
    public void send(int dest, float[] data, int off, int len) throws Exception {
        WriteMessage wm = system.newMessage(dest);
        wm.writeArray(data, off, len);
        wm.finish();
    }
}


