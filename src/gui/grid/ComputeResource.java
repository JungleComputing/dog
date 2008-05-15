/*
 * Created on Mar 6, 2006
 */
package gui.grid;

import java.util.ArrayList;

public class ComputeResource {
    private String hostname;

    private String accessType;

    private String friendlyName;

    private ArrayList<JobSubmitter> jobList = new ArrayList<JobSubmitter>(); 
    
    private ArrayList<ComputeResourceLoadElement> loadInfo = 
        new ArrayList<ComputeResourceLoadElement>(); 
    
    // location on the map
    private final int x;
    private final int y;

    private int maxCPUs;
    
    private String jobManager;
    private String queue;
    
    /**
     * @param accessType the resource manager to use
     * @param hostname the hostname to contact
     * @param x the x position on the map
     * @param y the y position on the map
     */
    public ComputeResource(String accessType, String hostname,
            String friendlyName, int maxCPUs, String jobManager, String queue, 
            int x, int y) {
        
        this.accessType = accessType;
        this.hostname = hostname;
        this.friendlyName = friendlyName;
        this.maxCPUs = maxCPUs;
        this.jobManager = jobManager;
        this.queue = queue;
        this.x = x;
        this.y = y;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getX() {
        return x;
    }
  
    public int getY() {
        return y;
    }

    public String toString() {
        return friendlyName + ", contact is: " + hostname + " with "
            + accessType;
    }

    public String getJobsInfoSting() {
        String res = friendlyName + " (" + jobList.size() + " nodes): \n";
        for (int i = 0; i < jobList.size(); i++) {
            JobSubmitter j = (JobSubmitter) jobList.get(i);
            String stateString = j.getStateString();
            res += "  job state is: " + stateString + "\n";
        }
        return res;
    }

    public ArrayList<JobSubmitter> getJobList() {
        return jobList;
    }

    public synchronized void addToJobList(JobSubmitter js) {
        long time = System.currentTimeMillis();
        addLoadInfo(time-1);
        jobList.add(js);
        addLoadInfo(time);
    }

    public synchronized void updateJobState(JobSubmitter js) {
        long time = System.currentTimeMillis();
        addLoadInfo(time);
    }

    public synchronized void removeJob(JobSubmitter j) {
        long time = System.currentTimeMillis();
        addLoadInfo(time-1);
        jobList.remove(j);
        addLoadInfo(time);
    }

    public synchronized JobSubmitter removeJob() {
        if(jobList.size() == 0) return null;
        long time = System.currentTimeMillis();
        addLoadInfo(time-1);
        JobSubmitter js = (JobSubmitter) jobList.remove(jobList.size()-1);
        addLoadInfo(time);
        
        return js;
    }

    public int getJobCount() {
        return jobList.size();
    }

    private void addLoadInfo(long time) {

/*        
        int load = 0;
        for(int i=0; i<jobList.size(); i++) {
            JobSubmitter j = (JobSubmitter) jobList.get(i);
            if(j.getJob() != null && j.getJob().getState() == Job.RUNNING) {
                load++;
            }
        }
        
        ComputeResourceLoadElement e = new ComputeResourceLoadElement();
        e.timestamp = time;
        e.load = load;
        loadInfo.add(e);

        removeOldLoadInfo();
*/
    }

    private void removeOldLoadInfo() {
        // don't remove anything.
        /*
        long time = System.currentTimeMillis();

        // remove really old elements
        for (int i = 0; i < loadInfo.size(); i++) {
            ComputeResourceLoadElement elt = (ComputeResourceLoadElement) loadInfo
                .get(i);
            if (elt.timestamp + LOAD_MEMORY*2 < time) {
                loadInfo.remove(i);
                i--;
            }
        }
        */
    }

    public ArrayList<ComputeResourceLoadElement> getLoadInfo() {
        removeOldLoadInfo();
        return (ArrayList<ComputeResourceLoadElement>) loadInfo.clone();
    }
    
    public int getLoadAt(long time) {
        int prevLoad = 0;
        for (int i = 0; i < loadInfo.size(); i++) {
            ComputeResourceLoadElement elt = 
                (ComputeResourceLoadElement) loadInfo.get(i);
            
            if (elt.timestamp > time) {
                return prevLoad;
            }
            prevLoad = elt.load;
        }
        
        return prevLoad;
    }

    public int getMaxCPUs() {
        return maxCPUs;
    }
    
    public int maxLoadInSeries(long beginStamp) {
        removeOldLoadInfo();
        int res = 0;
        for (int i = 0; i < loadInfo.size(); i++) {
            ComputeResourceLoadElement elt = (ComputeResourceLoadElement) loadInfo
                .get(i);
            if (elt.load > res) {
                res = elt.load;
            }
        }
        
        return res;
    }
    
    public synchronized void killAllJobs() {
        for(JobSubmitter js : jobList) { 
            js.kill();
        }
    }

    public String getJobManager() {
        return jobManager;
    }

    public void setJobManager(String jobManager) {
        this.jobManager = jobManager;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
