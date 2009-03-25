package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public final class DoubleArrayUtil extends ArrayUtil<double []> {

    private double [] cache; 
    
    @Override
    public double[] clone(double[] array) {
        return array.clone();
    }

    @Override
    public double[] create(int length) {

        double [] tmp = cache; 

        if (tmp == null || tmp.length != length) { 
            tmp = new double[length];
        } 
        
        return tmp;
    }

    @Override
    public int getLength(double[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 8;
    }

    @Override
    public void release(double[] array) {
        cache = array;
    }
}
