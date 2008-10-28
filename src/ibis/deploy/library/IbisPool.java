package ibis.deploy.library;

public class IbisPool implements Comparable<IbisPool> {

    private String name;

    private boolean closedWorld;

    private int size;

    private int submitted;

    public IbisPool(String name, boolean closedWorld, int size) {
        this.name = name;
        this.closedWorld = closedWorld;
        this.size = size;
    }

    public void addSubmitted(int submitted) {
        synchronized (this) {
            this.submitted += submitted;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isClosedWorld() {
        return closedWorld;
    }

    public int getSize() {
        return size;
    }

    public int compareTo(IbisPool o) {
        return getName().compareTo(o.getName());
    }

    public String toString() {
      if (closedWorld) {
            return name + " (" + submitted + "/" + size + ")";
        } else {
            return name + " (" + submitted + "/-)";
        }
    }

}
