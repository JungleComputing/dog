package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public class IntArrayUtil extends ArrayUtil<int []> {

    @Override
    public int[] clone(int[] array) {
        return array.clone();
    }

    @Override
    public int[] create(int length) {
        return new int[length];
    }

    @Override
    public int getLength(int[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 4;
    }

    @Override
    public void release(int[] array) {
    }
}

