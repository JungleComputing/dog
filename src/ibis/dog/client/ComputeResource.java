/*
 * Created on Mar 6, 2006
 */
package ibis.dog.client;

import ibis.deploy.Cluster;
import ibis.deploy.Job;

import java.awt.Color;
import java.util.ArrayList;

public class ComputeResource {

    private final Cluster cluster; 
    
    private Color color;
    
    private ArrayList<Job> jobList = new ArrayList<Job>(); 
    
    public ComputeResource(Cluster cluster) { 
        this.cluster = cluster;
    }

    public String getFriendlyName() {
        return cluster.getName();
    }

    public int getX() {
        return cluster.getLocationX();
    }
  
    public int getY() {
        return cluster.getLocationY();
    }

    public String toString() {
        return cluster.getName();
    }

    public String getJobsInfoSting() {
        String res = cluster.getName() + " (" + jobList.size() + " nodes): \n";
        for (Job j : jobList) {
            
            //String stateString = j.getStateString();
            //res += "  job state is: " + stateString + "\n";
        }
        return res;
    }

    public ArrayList<Job> getJobList() {
        return jobList;
    }

    public synchronized void addToJobList(Job js) {
        long time = System.currentTimeMillis();
        jobList.add(js);
    }

    public synchronized void updateJobState(Job js) {
        long time = System.currentTimeMillis();
    }

    public synchronized void removeJob(Job j) {
        long time = System.currentTimeMillis();
        jobList.remove(j);
    }

    public synchronized Job removeJob() {
        if (jobList.size() == 0) return null;
        Job js = jobList.remove(jobList.size()-1);
        return js;
    }

    public int getJobCount() {
        return jobList.size();
    }
        
    public synchronized void killAllJobs() {
        for(Job js : jobList) { 
 //           js.kill();
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
