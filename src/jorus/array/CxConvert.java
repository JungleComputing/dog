/*
 *  Copyright (c) 2008, Vrije Universiteit, Amsterdam, The Netherlands.
 *  All rights reserved.
 *
 *  Author(s)
 *  Frank Seinstra	(fjseins@cs.vu.nl)
 *
 */


package jorus.array;


import java.lang.reflect.Array;


public class CxConvert
{
    /*** ...to double[] ***********************************************/

/* EEP -- This seems very inefficient -- J
 
    public static <T> double[] toDoubles(T array)
    {
        double[] newarray = new double[Array.getLength(array)];
        for (int i=0; i<Array.getLength(array); i++) {
            if (array instanceof byte[]) {
                newarray[i] = (double) (Array.getByte(array, i) & 0xFF);
            } else if (array instanceof short[]) {
                newarray[i] = (double) (Array.getShort(array, i));
            } else if (array instanceof int[]) {
                newarray[i] = (double) (Array.getInt(array, i));
            } else if (array instanceof long[]) {
                newarray[i] = (double) (Array.getLong(array, i));
            } else if (array instanceof float[]) {
                newarray[i] = (double) (Array.getFloat(array, i));
            } else {
                return null;
            }
        }
        return newarray;
    }
*/

    public static <T> double[] toDoubles(T array) {
        
        final double [] newarray = new double[Array.getLength(array)];
        
        if (array instanceof byte[]) {
            final byte [] tmp = (byte []) array;
            
            for (int i=0; i<tmp.length; i++) {
                newarray[i] = (double) (tmp[i] & 0xFF);
            }
        } else if (array instanceof short[]) {
            final short [] tmp = (short []) array;
            
            for (int i=0; i<tmp.length; i++) {
                newarray[i] = (double) tmp[i];
            }
       
        } else if (array instanceof int[]) {
            final int [] tmp = (int []) array;
            
            for (int i=0; i<tmp.length; i++) {
                newarray[i] = (double) tmp[i];
            }
     
        } else if (array instanceof long[]) {
        
            final long [] tmp = (long []) array;
            
            for (int i=0; i<tmp.length; i++) {
                newarray[i] = (double) tmp[i];
            }
     
        } else if (array instanceof float[]) {
        
            final float [] tmp = (float []) array;
            
            for (int i=0; i<tmp.length; i++) {
                newarray[i] = (double) tmp[i];
            }
    
        } else {
            return null;
        }
    
        return newarray;
    }

    
}
