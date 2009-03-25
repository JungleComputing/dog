package jorus.parallel;

public abstract class Broadcast<T> extends Collective<T> {

    protected Broadcast(PxSystem system, Class c) throws Exception {
        super(system, c);
    }

    public abstract void broadcast(T data) throws Exception;
}
