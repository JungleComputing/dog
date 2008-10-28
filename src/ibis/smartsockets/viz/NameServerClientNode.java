package ibis.smartsockets.viz;

import ibis.smartsockets.hub.servicelink.ClientInfo;
import ibis.smartsockets.viz.ClientNode;
import ibis.smartsockets.viz.HubNode;
import android.graphics.Color;

import com.touchgraph.graphlayout.Node;

public class NameServerClientNode extends ClientNode {

    public NameServerClientNode(ClientInfo info, HubNode hub) {

        super(info.getClientAddress().toString(), hub);

        setType(Node.TYPE_CIRCLE);

        String adr = info.getClientAddress().toString();

        // System.out.println("Adding NameServer " + adr);

        setMouseOverText(new String[] { "Nameserver:", adr });

        setBackColor(Color.parseColor("#808080"));
        setNodeBorderColor(Color.parseColor("#545454"));
        setLabel("N");
    }
}
