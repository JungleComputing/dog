package jorus.parallel;

import jorus.parallel.comm.ByteArrayCommunicationUtil;
import jorus.parallel.comm.DoubleArrayCommunicationUtil;
import jorus.parallel.comm.FloatArrayCommunicationUtil;
import jorus.parallel.comm.IntArrayCommunicationUtil;
import jorus.parallel.comm.LongArrayCommunicationUtil;
import jorus.parallel.comm.ShortArrayCommunicationUtil;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

public abstract class CommunicationUtil<T> {
    
    protected final int rank;
    protected final PxSystem system;
    
    protected CommunicationUtil(PxSystem system) { 
        this.rank = system.myCPU();
        this.system = system;
    }
  
    public abstract void send(int dest, T data, int off, int len) throws Exception;
    public abstract void receive(int src, T data, int off, int len) throws Exception;
    
    public abstract void exchange(int partner, 
            T out, int offOut, int lenOut, T in, int offIn, int lenIn) throws Exception;
    
    public void receive(int src) throws Exception {
        ReadMessage rm = system.receive(src);
        rm.finish();
    }

    public void send(int dest) throws Exception {
        WriteMessage wm = system.newMessage(dest);
        wm.finish();
    }
        
    public static CommunicationUtil createImplementation(PxSystem system, Class c) throws Exception { 
        
        // FIXME: ugly -- J
        if (!c.isArray()) { 
            throw new Exception("Illegal CommunicationUtil type: " + c.getName());
        }
     
        Class component = c.getComponentType();
        
        if (component.equals(byte.class)) { 
            return new ByteArrayCommunicationUtil(system);
        } else if (component.equals(short.class)) { 
            return new ShortArrayCommunicationUtil(system);
        } else if (component.equals(int.class)) { 
            return new IntArrayCommunicationUtil(system);
        } else if (component.equals(long.class)) { 
            return new LongArrayCommunicationUtil(system);
        } else if (component.equals(float.class)) { 
            return new FloatArrayCommunicationUtil(system);
        } else if (component.equals(double.class)) { 
            return new DoubleArrayCommunicationUtil(system);
        } else { 
            throw new Exception("Illegal CommunicationUtil type: " + c.getName());
        }
    } 

}
