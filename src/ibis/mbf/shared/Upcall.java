package ibis.mbf.shared;

public interface Upcall {
    public void upcall(byte opcode, Object ... objects) throws Exception;
}