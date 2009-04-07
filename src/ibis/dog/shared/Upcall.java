package ibis.dog.shared;

import ibis.ipl.IbisIdentifier;

public interface Upcall {
    public void upcall(byte opcode, Object ... objects) throws Exception;
    
    public void gone(IbisIdentifier ibis);
}