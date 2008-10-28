package ibis.deploy.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.resources.JavaSoftwareDescription;

public class IntentSoftwareDescription extends JavaSoftwareDescription {

    /**
     * 
     */
    private static final long serialVersionUID = -3891102351087903620L;

    public String[] getArguments() {
        // first java options
        List<String> result = new ArrayList<String>();
        for (String javaOption : getJavaOptions()) {
            result.add(javaOption);
        }
        // then -n main
        result.add("-n");
        result.add(getJavaMain());
        // then for each system property -e key value
        for (String key : getJavaSystemProperties().keySet()) {
            result.add("-e");
            result.add(key);
            result.add(getJavaSystemProperties().get(key));
        }

        return result.toArray(new String[result.size()]);
    }

    public void setEnvironment(Map<String, Object> environment) {
        for (String key : environment.keySet()) {
            if (environment.get(key) == null) {
                environment.remove(key);
            }
        }
        if (!environment.containsKey("ANDROID_ASSETS")) {
            environment.put("ANDROID_ASSETS", "/system/app");
        }
        if (!environment.containsKey("ANDROID_BOOTLOGO")) {
            environment.put("ANDROID_BOOTLOGO", "1");
        }
        if (!environment.containsKey("ANDROID_DATA")) {
            environment.put("ANDROID_DATA", "/data");
        }
        if (!environment.containsKey("ANDROID_PROPERTY_WORKSPACE")) {
            environment.put("ANDROID_PROPERTY_WORKSPACE", "9,32768");
        }
        if (!environment.containsKey("ANDROID_ROOT")) {
            environment.put("ANDROID_ROOT", "/system");
        }
        if (!environment.containsKey("BOOTCLASSPATH")) {
            environment
                    .put(
                            "BOOTCLASSPATH",
                            "/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar");
        }
        if (!environment.containsKey("LD_LIBRARY_PATH")) {
            environment.put("LD_LIBRARY_PATH", "/system/lib");
        }
        if (!environment.containsKey("PATH")) {
            environment.put("PATH",
                    "/sbin:/system/sbin:/system/bin:/system/xbin");
        }
        super.setEnvironment(environment);
    }

    @Override
    public Map<String, Object> getEnvironment() {
        Map<String, Object> environment = super.getEnvironment();
        if (!environment.containsKey("ANDROID_ASSETS")) {
            environment.put("ANDROID_ASSETS", "/system/app");
        }
        if (!environment.containsKey("ANDROID_BOOTLOGO")) {
            environment.put("ANDROID_BOOTLOGO", "1");
        }
        if (!environment.containsKey("ANDROID_DATA")) {
            environment.put("ANDROID_DATA", "/data");
        }
        if (!environment.containsKey("ANDROID_PROPERTY_WORKSPACE")) {
            environment.put("ANDROID_PROPERTY_WORKSPACE", "9,32768");
        }
        if (!environment.containsKey("ANDROID_ROOT")) {
            environment.put("ANDROID_ROOT", "/system");
        }
        if (!environment.containsKey("BOOTCLASSPATH")) {
            environment
                    .put(
                            "BOOTCLASSPATH",
                            "/system/framework/core.jar:/system/framework/ext.jar:/system/framework/framework.jar:/system/framework/android.policy.jar:/system/framework/services.jar");
        }
        if (!environment.containsKey("LD_LIBRARY_PATH")) {
            environment.put("LD_LIBRARY_PATH", "/system/lib");
        }
        if (!environment.containsKey("PATH")) {
            environment.put("PATH",
                    "/sbin:/system/sbin:/system/bin:/system/xbin");
        }
        return environment;
    }

}
