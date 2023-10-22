package org.dhs.plugin.util;

import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static Properties configProps = null;
    private static InputStream configFileStream = null;

    /**
     *
     */
    private static void initializeConfigProps() {
        configProps = new Properties();
        try {
            if(configFileStream == null) {
                configFileStream = Configuration.class.getResourceAsStream("/org/dhs/plugin/configuration.properties");
                configProps.load(configFileStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param keyName
     * @return
     */
    public static String getConfigProp(String keyName) {
        if(keyName != null && !keyName.isEmpty()) {
            if(configProps == null)
                initializeConfigProps();
            return configProps.getProperty(keyName);
        }
        return null;
    }
}
