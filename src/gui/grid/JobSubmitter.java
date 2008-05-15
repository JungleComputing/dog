package gui.grid;

class JobSubmitter  {
    
    // TODO: This is a stub!! 
    
    private ComputeResource m;

    private GridRunner g;

    boolean submissionInProgress = true;
        
    boolean done = false;

    
    public JobSubmitter(GridRunner g, ComputeResource m) {
        this.g = g;
        this.m = m;
    }

    public void run() {
    }

    public String getStateString() {
        return "UNKNOWN";
    }

    /*
    protected Job submitGATJob(ComputeResource m) {
        try {
            int id = g.getnewJobId();
            // create a resource description
            File outFile = GAT.createFile(g.getContext(), "any:///stdout_" + id + ".txt");
            File errFile = GAT.createFile(g.getContext(), "any:///stderr_" + id + ".txt");

            SoftwareDescription sd = new SoftwareDescription();
            sd.setLocation(g.getApplication().getCommand());
            
            String[] args = g.getApplication().getParameters();
            for(int i=0; i<args.length; i++) {
                args[i] = args[i].replaceAll("\\$KEY", g.getAppId());
                args[i] = args[i].replaceAll("\\$REGISTRY", g.getRegistry());
                args[i] = args[i].replaceAll("\\$CLUSTER", m.getFriendlyName());
            }
            
            sd.setArguments(args);
            sd.setStdout(outFile);
            sd.setStderr(errFile);

            sd.addAttribute("maxTime", "120"); // two hours
            if(!m.getQueue().equals("DEFAULT")) {
                sd.addAttribute("queue", m.getQueue());
            }

            String[] pre = g.getApplication().getPreStaged();
            for(int i=0; i<pre.length; i++) {
                sd.addPreStagedFile(GAT.createFile(g.getContext(), pre[i]));
            }

            Hashtable attributes = new Hashtable();
            attributes.put("machine.node", m.getHostname());

            ResourceDescription rd = new HardwareResourceDescription(attributes);

            JobDescription jd = new JobDescription(sd, rd);

            Preferences prefs = new Preferences();

            if (!m.getAccessType().equals("any")) {
                prefs.put("ResourceBroker.adaptor.name", m.getAccessType());
            }

            prefs.put("ResourceBroker.jobmanagerContact", m.getJobManager());

            ResourceBroker broker = GAT.createResourceBroker(g.getContext(),
                prefs);
            Job job = broker.submitJob(jd);

            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric metric = md.createMetric();
            job.addMetricListener(this, metric); // register callback for job.status

            return job;

        } catch (Exception e) {
            System.err.println("GridRunner: warning, an error occurred: " + e);
            return null;
        }
    }

    synchronized public void processMetricEvent(MetricValue val) {
        int state = getJobState();

        if (state == Job.STOPPED || state == Job.SUBMISSION_ERROR) {
            done = true;
        }

        m.updateJobState(this);
        
        g.processStateChange(this);
    }

    private int getJobState() {
        int state;

        try {
            state = j.getState();
        } catch (Exception e) {
            System.err.println("warning, could not get job state");
            return -1;
        }
        return state;
    }*/

    /*
    public Job getJob() {
        return j;
    }*/
    
    public void kill() {
    }

    public boolean done() {
        return done;
    }
}
