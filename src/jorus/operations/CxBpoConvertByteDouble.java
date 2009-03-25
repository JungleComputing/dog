package jorus.operations;

import jorus.array.CxArray2d;

public class CxBpoConvertByteDouble {

    protected int       w       = 0;
    protected int   h       = 0;
    protected int   off1    = 0;
    protected int   off2    = 0;
    protected int   stride1 = 0;
    protected int   stride2 = 0;
        
    public void init(CxArray2d dst, CxArray2d src, boolean parallel)
    {
        int w1  = parallel ? dst.getPartialWidth() : dst.getWidth();
        int e1  = dst.getExtent();
        int w2  = parallel ? src.getPartialWidth() : src.getWidth();
        int e2  = src.getExtent();
        int bw1 = dst.getBorderWidth();
        int bw2 = src.getBorderWidth();

        w       = w1 * e1;
        h       = parallel ? dst.getPartialHeight() : dst.getHeight();
        off1    = ((w1 + 2*bw1) * dst.getBorderHeight() + bw1) * e1;
        off2    = ((w2 + 2*bw2) * src.getBorderHeight() + bw2) * e2;
        stride1 = bw1 * e1*2;
        stride2 = bw2 * e2*2;
    }

    public void doIt(double [] dst, byte [] src) { 

        for (int j=0; j<h; j++) {

            int s1Ptr = off1 + j*(w+stride1);
            int s2Ptr = off2 + j*(w+stride2);

            for (int i=0; i<w; i++) {
                dst[s1Ptr] = (double) (src[s2Ptr] & 0xFF);
                s1Ptr++;
                s2Ptr++;
            }
        }                               
    }
    
}
