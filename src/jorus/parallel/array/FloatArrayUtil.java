package jorus.parallel.array;

import jorus.parallel.ArrayUtil;

public class FloatArrayUtil extends ArrayUtil<float []> {

    @Override
    public float[] clone(float[] array) {
        return array.clone();
    }

    @Override
    public float[] create(int length) {
        return new float[length];
    }

    @Override
    public int getLength(float[] array) {
        return array.length;
    }

    @Override
    public int typeSize() {
        return 4;
    }

    @Override
    public void release(float[] array) {
    }
}
