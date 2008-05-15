/*
 * Created on Mar 6, 2006
 */
package gui.grid;

import java.io.File;
import java.util.ArrayList;

public class Grid {
    
    private final ArrayList<ComputeResource> resources = 
        new ArrayList<ComputeResource>();

    private final String mapFileName;

    private final String gridName;

    private Grid(String gridName, String mapFileName) {
        this.mapFileName = mapFileName;
        this.gridName = gridName;
    }

    public String getGridName() {
        return gridName;
    }

    public void addComputeResource(ComputeResource a) {
        resources.add(a);
    }

    public File getMapFile() {
        return new File(mapFileName);
    }

    public ArrayList<ComputeResource> getComputeResources() {
        return resources;
    }
    
    public int getJobCount() {
        int res = 0;

        for (ComputeResource c : resources) {
            res += c.getJobCount();
        }

        return res;
    }

    public void removeJob(JobSubmitter j) {
        for (ComputeResource c : resources) { 
            c.removeJob(j);
        }
    }

    public String getJobsInfoSting() {
        String res = "";
        
        for (ComputeResource c : resources) { 
            res += c.getJobsInfoSting() + "\n";
        }
        
        return res;
    }
    
    public int getMaxCPUs() {
        int res = 0;
        
        for (ComputeResource c : resources) {
            res += c.getMaxCPUs();
        }
        
        return res;
    }
    
    public int maxLoadInSeries(long beginStamp) {
        int res = 0;
        
        for (ComputeResource c : resources) {
            res += c.maxLoadInSeries(beginStamp);
        }
        
        return res;
    }

    public void killAllJobs() {
        for (ComputeResource c : resources) { 
            c.killAllJobs();
        }        
    }
    
    // TODO: replace with IbisDeploy grid name ?
    public static Grid loadGrid(String filename) {
        Input in = new Input(filename);
        String gridName = in.readString();
        in.readln();
        String mapfile = in.readString();
        in.readln();
        Grid g = new Grid(gridName, mapfile);

        while (!in.eof()) {
            // VU ssh fs0.das2.cs.vu.nl 200 235
            String friendly = in.readWord();
            int maxCPUs = in.readInt();
            String access = in.readWord();
            String machine = in.readWord();
            String jobmanager = in.readWord();
            String queue = in.readWord();
            int x = in.readInt();
            int y = in.readInt();
            in.readln();

            ComputeResource r = new ComputeResource(access, machine, friendly, 
                    maxCPUs, jobmanager, queue, x, y);
            
            g.addComputeResource(r);
        }

        return g;
    }
}
