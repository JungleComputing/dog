package ibis.mbf.shared;

import java.io.Serializable;

public class FeatureVector implements Serializable {
   
    // Generated by eclipse
    private static final long serialVersionUID = -5273998909448194335L;
    
    public final int invariants;
    public final int receptiveFields;
    
    public final double [] vector;    
    
    public FeatureVector(int invariants, int receptiveFields) { 
        this.invariants = invariants;
        this.receptiveFields = receptiveFields;
        vector = new double[invariants*receptiveFields*2];
    }
}
