/*
 * Created on Mar 6, 2006
 */
package ibis.dog.client;

import ibis.deploy.Application;
import ibis.deploy.Cluster;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

public class ComputeResource {

    private final Grid grid;
    private final Cluster cluster;

    private final ArrayList<Job> jobList = new ArrayList<Job>();

    private Color color;

    private int nextID = 0;

    public ComputeResource(Grid grid, Cluster cluster) {
        this.grid = grid;
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

            // String stateString = j.getStateString();
            // res += " job state is: " + stateString + "\n";
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
        if (jobList.size() == 0)
            return null;
        Job js = jobList.remove(jobList.size() - 1);
        return js;
    }

    public synchronized int getJobCount() {
        return jobList.size();
    }

    public synchronized int getJobID() {
        return nextID++;
    }

    public synchronized void killAllJobs() {
        for (Job js : jobList) {
            // js.kill();
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Grid getGrid() {
        return grid;
    }

    public SubJob getSubJob(Application app, String startupScript) {

        SubJob job = new SubJob(app.getName());
        job.setApplication(app);

        job.setGrid(grid);
        job.setCluster(cluster);
        job.setNodes(cluster.getNodes());
        job.setMulticore(cluster.getMulticore());

        // FIXME !!!
        // serverJob.setWrapperExecutable(cluster.getWrapperExecutable());
        // serverJob.setWrapperArguments(cluster.getWrapperArguments());

        if (startupScript != null) {
            job.setWrapperExecutable("/bin/sh");
            job.setWrapperArguments(new File(startupScript).getName());
        }

        return job;
    }

}
