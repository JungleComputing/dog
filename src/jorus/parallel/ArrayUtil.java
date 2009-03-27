package jorus.parallel;

import jorus.parallel.array.ByteArrayUtil;
import jorus.parallel.array.DoubleArrayUtil;
import jorus.parallel.array.FloatArrayUtil;
import jorus.parallel.array.IntArrayUtil;
import jorus.parallel.array.LongArrayUtil;
import jorus.parallel.array.ShortArrayUtil;

public abstract class ArrayUtil<T> {
    
    public abstract int getLength(T array);
    public abstract T clone(T array);
    public abstract T create(int length);    
    public abstract void release(T array);
    public abstract int typeSize();
    
    public static ArrayUtil createImplementation(Class c) throws Exception { 
        
        // FIXME: ugly -- J
        if (!c.isArray()) { 
            throw new Exception("Illegal ArrayUtil type: " + c.getName());
        }
     
        Class component = c.getComponentType();
        
        if (component.equals(byte.class)) { 
            return new ByteArrayUtil();
        } else if (component.equals(short.class)) { 
            return new ShortArrayUtil();
        } else if (component.equals(int.class)) { 
            return new IntArrayUtil();
        } else if (component.equals(long.class)) { 
            return new LongArrayUtil();
        } else if (component.equals(float.class)) { 
            return new FloatArrayUtil();
        } else if (component.equals(double.class)) { 
            return new DoubleArrayUtil();
        } else { 
            throw new Exception("Illegal CommunicationUtil type: " + c.getName());
        }
    } 
    
}
