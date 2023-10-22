package org.dhs.plugin;

import com.ibm.ecm.extension.Plugin;
import com.ibm.ecm.extension.PluginService;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import org.apache.log4j.Logger;
import org.dhs.plugin.services.ManagerService;
import org.dhs.plugin.util.APIUtil;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class SessionManagerPlugin  extends Plugin {

    private Logger logger = Logger.getLogger(SessionManagerPlugin.class);
    private APIUtil apiUtil = null;

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
    public PluginService[] getServices() {
        return new PluginService[] { new ManagerService()};
    }

    @Override
    public void applicationInit(HttpServletRequest request, PluginServiceCallbacks callbacks) throws Exception {
        // try to connect ecm and create a custom object if not existed
        String methodName = "applicationInit";
        if(logger.isInfoEnabled())
            logger.info(" Entry "+methodName);
        if(callbacks != null) {
            try {

            } catch (Exception e) {
                logger.error(e); // this will be thrown when no session manager is available
            }
        }
        if(logger.isInfoEnabled())
            logger.info(" Exit "+methodName);
    }
}
