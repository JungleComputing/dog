package ibis.dog.shared;

public class ServerDescription extends MachineDescription {

    // Generated by eclipse
    private static final long serialVersionUID = -1425751845250064011L;
    
    private String name;
    
    public ServerDescription(MachineDescription md, String name) { 
        super(md);
        this.name = name;
    }
    
    public String getName() { 
        return name;
    }
}
