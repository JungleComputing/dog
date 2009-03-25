package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public class ByteArrayUtil extends ArrayUtil<byte []> {

    @Override
    public byte[] clone(byte[] array) {
        return array.clone();
    }

    @Override
    public byte[] create(int length) {
        return new byte[length];
    }

    @Override
    public int getLength(byte[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 1;
    }

    @Override
    public void release(byte[] array) {
    }
}
