package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public class LongArrayUtil extends ArrayUtil<long []> {

    @Override
    public long[] clone(long[] array) {
        return array.clone();
    }

    @Override
    public long[] create(int length) {
        return new long[length];
    }

    @Override
    public int getLength(long[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 8;
    }

    @Override
    public void release(long[] array) {
    }
}

