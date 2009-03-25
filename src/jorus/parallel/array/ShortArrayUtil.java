package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public class ShortArrayUtil extends ArrayUtil<short []> {

    @Override
    public short[] clone(short[] array) {
        return array.clone();
    }

    @Override
    public short[] create(int length) {
        return new short[length];
    }

    @Override
    public int getLength(short[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 8;
    }

    @Override
    public void release(short[] array) {
    }
}
