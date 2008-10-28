package ibis.deploy.library;

import ibis.util.TypedProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;

public class IbisDeployer extends Deployer {

    class JobListener implements MetricListener {

        Server server;

        public JobListener(Server server) {
            this.server = server;
        }

        public void processMetricEvent(MetricEvent event) {
            if (event.getValue() == JobState.STOPPED) {
                try {
                    server.stopServer();
                } catch (GATInvocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

    }

    protected String getJavaClassPath(String[] filenames,
            boolean recursivePrefix, boolean toWindows) {
        String classpath = "";
        if (filenames != null) {
            for (String filename : filenames) {
                classpath += getFiles(new java.io.File(filename), "", ".jar",
                        recursivePrefix);
            }
        }
        if (toWindows) {
            classpath = classpath.replace('/', '\\');
            classpath = classpath.replace(':', ';');
        }
        return classpath;
    }

    protected String getJavaClassPath(Map<File, File> files,
            boolean recursivePrefix, boolean toWindows) {
        String classpath = "";
        if (files != null) {
            for (File file : files.keySet()) {
                // if (files.get(file) != null) {
                // classpath += files.get(file).getPath() + "/";
                // }
                classpath += getFiles(new java.io.File(file.getPath()), "",
                        ".jar", recursivePrefix);
            }
        }
        if (toWindows) {
            classpath = classpath.replace('/', '\\');
            classpath = classpath.replace(':', ';');
        }
        return classpath;
    }

    private String getFiles(java.io.File file, String prefix, String postfix,
            boolean recursivePrefix) {
        String result = "";
        if (file.isDirectory()) {
            for (java.io.File childfile : file.listFiles()) {
                String resolvedPrefix = "";
                if (recursivePrefix) {
                    resolvedPrefix = prefix + file.getName() + "/";
                }
                result += getFiles(childfile, resolvedPrefix, postfix,
                        recursivePrefix);
            }
        } else if (file.getName().endsWith(postfix)) {
            result += prefix + file.getName() + ":";
        }
        return result;
    }

    public Job deploy(Application application, int processCount,
            Cluster cluster, int resourceCount, IbisPool pool,
            String serverAddress, String hubAddress, MetricListener listener)
            throws Exception, GATInvocationException, IOException {
        GATContext context = new GATContext();
        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), cluster.getPassword());
            // securityContext.addNote("adaptors", "commandlinessh,sshtrilead");
            context.addSecurityContext(securityContext);
        }
        context.addPreference("file.chmod", "0755");
        if (cluster.getBrokerAdaptors() != null) {
            context.addPreference("resourcebroker.adaptor.name", cluster
                    .getBrokerAdaptors());
        }
        if (cluster.getFileAdaptors() != null) {
            context.addPreference("file.adaptor.name", cluster
                    .getFileAdaptors());
        }

        JavaSoftwareDescription sd = (JavaSoftwareDescription) application
                .getSoftwareDescription(context);

        // add ibis specific things to sd
        if (sd.getExecutable() == null || sd.getExecutable().equals("java")) {
            sd.setExecutable(cluster.getJavaPath());
        }
        String[] files = new String[sd.getPreStaged().size()];
        int i = 0;
        for (File file : sd.getPreStaged().keySet()) {
            files[i++] = file.getPath();
        }
        if (sd.getJavaClassPath() == null) {
            sd.setJavaClassPath(getJavaClassPath(sd.getPreStaged(), true,
                    cluster.isWindows()));
        }
        Map<String, String> systemProperties = new HashMap<String, String>();
        if (sd.getJavaSystemProperties() != null) {
            systemProperties.putAll(sd.getJavaSystemProperties());
        }
        systemProperties.put("ibis.server.address", serverAddress);
        systemProperties.put("ibis.server.hub.addresses", hubAddress);
        systemProperties.put("ibis.pool.name", pool.getName());
        if (pool.isClosedWorld()) {
            systemProperties.put("ibis.pool.size", "" + pool.getSize());
        }

        // systemProperties.put("log4j.configuration", "file:"
        // + application.getLog4jPropertiesLocation());
        sd.setJavaSystemProperties(systemProperties);

        JobDescription jd = null;

        if (cluster.getWrapperScript() != null
                && cluster.getWrapperExecutable() != null) {
            SoftwareDescription wrapperSd = new SoftwareDescription();
            if (sd.getAttributes() != null) {
                wrapperSd.setAttributes(sd.getAttributes());
            }
            if (sd.getEnvironment() != null) {
                wrapperSd.setEnvironment(sd.getEnvironment());
            }
            if (sd.getPreStaged() != null) {
                for (File src : sd.getPreStaged().keySet()) {
                    wrapperSd.addPreStagedFile(src, sd.getPreStaged().get(src));
                }
            }
            File script = GAT.createFile(context, cluster.getWrapperScript());
            wrapperSd.addPreStagedFile(script);
            if (sd.getPostStaged() != null) {
                for (File src : sd.getPostStaged().keySet()) {
                    wrapperSd.addPostStagedFile(src, sd.getPostStaged()
                            .get(src));
                }
            }
            wrapperSd.setStderr(sd.getStderr());
            wrapperSd.setStdout(sd.getStdout());
            wrapperSd.setExecutable(cluster.getWrapperExecutable());
            List<String> argumentList = new ArrayList<String>();
            argumentList.add(script.getName());
            argumentList.add("" + resourceCount);
            argumentList.add("" + processCount);
            argumentList.add(sd.getExecutable());
            if (sd.getArguments() != null) {
                for (String arg : sd.getArguments()) {
                    argumentList.add(arg);
                }
            }
            wrapperSd.setArguments(argumentList.toArray(new String[argumentList
                    .size()]));
            jd = new JobDescription(wrapperSd);
            jd.setProcessCount(1);
            jd.setResourceCount(1);
        } else {
            jd = new JobDescription(sd);
            jd.setProcessCount(processCount);
            jd.setResourceCount(resourceCount);
        }

        System.out.println("Application sd: " + sd);

        ResourceBroker broker = GAT.createResourceBroker(context, cluster
                .getBroker());
         Job result = broker.submitJob(jd, listener, "job.status");
        return result;
//        return null;
    }

    /**
     * -g gridfile -a applicationfile
     * 
     * @param args
     */
    public void init(String[] args) {
        int i = 0;
        while (i + 1 < args.length) {
            if (args[i].equals("-g")) {
                try {
                    addGrid(new IbisBasedGrid(args[i + 1]));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (args[i].equals("-a")) {
                try {
                    addApplicationGroup(new IbisBasedApplicationGroup(
                            args[i + 1]));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // invalid argument
            }
            i += 2;
        }
    }

    /**
     * Adds a grid to this deployer, the grid will be loaded from the given
     * properties.
     * 
     * @param properties
     *                the properties containing the grid
     * @return the added grid
     */
    public Grid addGrid(TypedProperties properties) throws Exception {
        properties.expandSystemVariables();
        return addGrid(new IbisBasedGrid(properties));
    }

    /**
     * Adds an application group to this deployer, the application group will be
     * loaded from the given properties.
     * 
     * @param properties
     *                the properties containing the application group
     * @return the added application group
     */
    public ApplicationGroup addApplicationGroup(TypedProperties properties)
            throws Exception {
        properties.expandSystemVariables();
        return addApplicationGroup(new IbisBasedApplicationGroup(properties));
    }
}
