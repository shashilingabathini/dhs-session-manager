package org.dhs.plugin;

import com.ibm.ecm.extension.Plugin;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class SessionManagerPlugin  extends Plugin {

    @Override
    public String getId() {
        return "DHSSessionManagerPlugin";
    }

    @Override
    public String getName(Locale locale) {
        return "DHS Session Manager Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getScript() {
        return "DHSSessionManagerPlugin.js";
    }

    @Override
    public String getCSSFileName() {
        return "DHSSessionManagerPlugin.css";
    }

    @Override
    public String getDojoModule() {
        return "dHSSessionManagerPluginDojo";
    }

    @Override
    public String getConfigurationDijitClass() {
        return "dHSSessionManagerPluginDojo.ConfigurationPane";
    }

    @Override
    public void applicationInit(HttpServletRequest request, PluginServiceCallbacks callbacks) throws Exception {
        // try to connect ecm and create a custom object if not existed
        if(callbacks != null) {
           String repoId  =  callbacks.getRepositoryId();
        }
    }
}
