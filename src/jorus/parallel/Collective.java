package jorus.parallel;

import java.lang.reflect.Constructor;

public class Collective<T> {
    
    protected final int rank; 
    protected final int size; 
    protected final int logCPUs; 
    
    protected final PxSystem system;
    
    protected final ArrayUtil<T> util;
    protected final CommunicationUtil<T> comm;
     
  //  private long time; 
  //  private long count; 
    
    @SuppressWarnings("unchecked")
    protected Collective(PxSystem system, Class c) throws Exception {
        
        this.rank = system.myCPU();
        this.size = system.nrCPUs();
        this.logCPUs = (int) (Math.log((double) size) / Math.log(2.0));
        this.system = system;         

        util = ArrayUtil.createImplementation(c);
        comm = CommunicationUtil.createImplementation(system, c);
    }
    
    /*
    protected void addTime(long time) { 
        this.time += time;
        count++;
    }
    
    public long getTime() { 
        return time;
    }
  
    public long getCount() { 
        return count;
    }
    
    public void resetTime() { 
        time = count = 0;
    }
*/
    
    @SuppressWarnings("unchecked")
    public static Collective loadImplementation(String name, PxSystem s, Class c) throws Exception {
        
        try { 
            Class clazz = Class.forName(name);
            Constructor constructor = clazz.getConstructor(PxSystem.class, Class.class);
            return (Collective) constructor.newInstance(s, c); 
        } catch (Exception e) {
            throw new Exception("Failed to load collective: " + name, e);
        }
    }
}
